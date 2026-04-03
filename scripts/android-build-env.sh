#!/usr/bin/env bash
# Fedora (and similar): system Java may be a JRE-only OpenJDK 21 without javac.
# Use the Temurin JDK 17 installed under ~/.jdks (see setup notes in repo docs / summary).
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
JDK="${JAVA_HOME_OVERRIDE:-$HOME/.jdks/jdk-17.0.18+8}"
if [[ ! -x "$JDK/bin/javac" ]]; then
  echo "Expected a full JDK at: $JDK (with bin/javac). Install Temurin 17 or set JAVA_HOME_OVERRIDE." >&2
  exit 1
fi
export JAVA_HOME="$JDK"
export PATH="$JAVA_HOME/bin:$PATH"
cd "$ROOT"
exec "$@"
