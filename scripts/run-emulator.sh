#!/usr/bin/env bash
# Launch the Android Emulator with a visible window (not headless).
# Default AVD: Pixel_3a (Flatpak Android Studio keeps AVDs under ANDROID_AVD_HOME).
#
# Low-end hosts: less RAM, fewer vCPUs, no audio. GPU default is swiftshader_indirect
# except on Fedora (lite mode defaults to host — avoids common SwiftShader SIGSEGV).
#
# If the WINDOW FLASHES THEN CLOSES (or exit 139 = SIGSEGV):
#   Log may show: "cannnot unmap ptr ... protected range" — common on Fedora + emulator.
#   Try in order:
#   1) EMULATOR_GPU=host ./scripts/run-emulator.sh     (skip SwiftShader/Vulkan; often avoids the crash)
#   2) EMULATOR_GPU=angle_indirect ./scripts/run-emulator.sh
#   3) SELinux executable heap (many Fedora users need this for qemu/SwiftShader):
#        sudo setsebool -P selinuxuser_execheap 1
#      See: https://github.com/flathub/com.google.AndroidStudio#avd-emulator
#   4) sudo ausearch -m avc -ts recent | tail -40
#   Cold boot default: -no-snapshot-load unless OPENASCEND_EMULATOR_USE_SNAPSHOT=1
#
# Usage:
#   ./scripts/run-emulator.sh
#   ./scripts/run-emulator.sh -verbose
#
# Stronger machine (use host GPU, more RAM/cores):
#   OPENASCEND_EMULATOR_HIGH_PERF=1 ./scripts/run-emulator.sh
#
# Tweaks:
#   EMULATOR_MEMORY=1536 EMULATOR_CORES=1 ./scripts/run-emulator.sh
#   EMULATOR_GPU=host ./scripts/run-emulator.sh
#   ANDROID_AVD_NAME=Pixel_8a ./scripts/run-emulator.sh
#
# Set OPENASCEND_EMULATOR_SKIP_LOCK_CLEANUP=1 to never remove AVD *.lock files.
# On native Wayland, set OPENASCEND_EMULATOR_NATIVE_WAYLAND=1 to skip forcing X11.
set -euo pipefail
SDK="${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"
AVD_HOME="${ANDROID_AVD_HOME:-$HOME/.var/app/com.google.AndroidStudio/config/.android/avd}"
NAME="${ANDROID_AVD_NAME:-Pixel_3a}"
LOG="${OPENASCEND_EMULATOR_LOG:-${XDG_CACHE_HOME:-$HOME/.cache}/openascend/emulator-last.log}"

export ANDROID_AVD_HOME="$AVD_HOME"
export PATH="$SDK/emulator:$SDK/platform-tools:$PATH"

mkdir -p "$(dirname "$LOG")"

# Wayland session: Emulator UI is Qt; without X11/XWayland it often never shows a window on Fedora/GNOME.
if [[ "${OPENASCEND_EMULATOR_NATIVE_WAYLAND:-0}" != "1" ]] && [[ "${XDG_SESSION_TYPE:-}" == "wayland" ]]; then
  export QT_QPA_PLATFORM="${QT_QPA_PLATFORM:-xcb}"
fi

# After `emu kill` / crash, stale locks block startup ("multiple emulators"). Clear only if SDK qemu is not running.
AVD_DIR="$AVD_HOME/${NAME}.avd"
if [[ "${OPENASCEND_EMULATOR_SKIP_LOCK_CLEANUP:-0}" != "1" ]] && [[ -d "$AVD_DIR" ]] &&
  ! pgrep -u "$(id -un)" -f 'emulator/qemu/linux-x86_64/qemu-system' >/dev/null 2>&1; then
  rm -f "$AVD_DIR/hardware-qemu.ini.lock" "$AVD_DIR/multiinstance.lock" 2>/dev/null || true
fi

if [[ "${OPENASCEND_EMULATOR_HIGH_PERF:-0}" == "1" ]]; then
  MEMORY="${EMULATOR_MEMORY:-4096}"
  CORES="${EMULATOR_CORES:-4}"
  GPU="${EMULATOR_GPU:-host}"
  AUDIO=()
else
  MEMORY="${EMULATOR_MEMORY:-2048}"
  CORES="${EMULATOR_CORES:-2}"
  # Fedora: default SwiftShader/Vulkan often hits SIGSEGV + "protected range" in logs; host GLES is usually stable.
  if [[ -f /etc/fedora-release ]]; then
    GPU="${EMULATOR_GPU:-host}"
  else
    GPU="${EMULATOR_GPU:-swiftshader_indirect}"
  fi
  AUDIO=(-no-audio)
fi

EMU_ARGS=(
  -avd "$NAME"
  -memory "$MEMORY"
  -cores "$CORES"
  -gpu "$GPU"
  -no-boot-anim
  "${AUDIO[@]}"
)

# Snapshot restore often crashes after a GPU/renderer change ("window flashes then dies"). Cold boot unless opted in.
if [[ "${OPENASCEND_EMULATOR_USE_SNAPSHOT:-0}" != "1" ]]; then
  want_snap=0
  for a in "$@"; do
    case "$a" in
      *snapshot*) want_snap=1 ;;
    esac
  done
  if [[ "$want_snap" -eq 0 ]]; then
    EMU_ARGS+=(-no-snapshot-load)
  fi
fi

{
  echo ""
  echo "==== OpenAscend emulator $(date -Iseconds) ===="
  echo "AVD=$NAME RAM=${MEMORY}MB cores=$CORES gpu=$GPU QT_QPA_PLATFORM=${QT_QPA_PLATFORM:-default}"
  echo "cmd: $SDK/emulator/emulator ${EMU_ARGS[*]} $*"
} >>"$LOG"

echo "OpenAscend: starting emulator — AVD=$NAME (logging to $LOG)" >&2
if [[ -f /etc/fedora-release ]] && [[ "${OPENASCEND_EMULATOR_SKIP_FEDORA_HINT:-0}" != "1" ]]; then
  echo "OpenAscend: Fedora: if the window closes after a few seconds, check SELinux: sudo ausearch -m avc -ts recent | tail -30" >&2
fi

set +e
"$SDK/emulator/emulator" "${EMU_ARGS[@]}" "$@" 2>&1 | tee -a "$LOG"
code=${PIPESTATUS[0]}
set -e

if [[ "$code" -ne 0 ]] && [[ "$code" -ne 130 ]]; then
  echo "" >&2
  echo "OpenAscend: emulator exited with status $code. Last 45 lines of $LOG:" >&2
  tail -45 "$LOG" >&2
  echo "" >&2
  if [[ "$code" -eq 139 ]] || grep -q 'protected range' "$LOG" 2>/dev/null; then
    echo "OpenAscend: This looks like the known Fedora + Android Emulator crash (SIGSEGV / “protected range”)." >&2
    echo "OpenAscend: Try first (no SELinux change):  EMULATOR_GPU=host ./scripts/run-emulator.sh" >&2
    echo "OpenAscend: Then:  EMULATOR_GPU=angle_indirect ./scripts/run-emulator.sh" >&2
    echo "OpenAscend: If still crashing, SELinux executable heap (security tradeoff; common fix):" >&2
    echo "  sudo setsebool -P selinuxuser_execheap 1" >&2
    echo "OpenAscend: Details: https://github.com/flathub/com.google.AndroidStudio#avd-emulator" >&2
  elif [[ -f /etc/fedora-release ]]; then
    echo "OpenAscend: Fedora: check SELinux denials: sudo ausearch -m avc -ts recent | tail -40" >&2
    echo "OpenAscend: Optional: sudo setsebool -P selinuxuser_execheap 1   or   EMULATOR_GPU=host ./scripts/run-emulator.sh" >&2
  fi
fi

exit "$code"
