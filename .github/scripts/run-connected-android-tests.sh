#!/usr/bin/env bash
# Run instrumented tests with boot settle + adb retry. Used by android-emulator-runner
# (each workflow "script" line is a separate shell; keep logic in this file).
set -euo pipefail

wait_for_boot() {
  adb wait-for-device
  local boot=""
  for _ in $(seq 1 90); do
    boot="$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r' || true)"
    if [[ "$boot" == "1" ]]; then
      return 0
    fi
    sleep 2
  done
  echo "sys.boot_completed did not become 1 within ~3 minutes" >&2
  return 1
}

run_gradle_tests() {
  ./gradlew connectedDebugAndroidTest --stacktrace
}

wait_for_boot
# Package manager can still be busy right after boot_completed flips.
sleep 5

if run_gradle_tests; then
  exit 0
fi

echo "::warning::connectedDebugAndroidTest failed; restarting adb and retrying once"
adb kill-server 2>/dev/null || true
sleep 2
adb start-server
adb wait-for-device
wait_for_boot
sleep 5
run_gradle_tests
