#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

compile() {
  echo "[$(date +%H:%M:%S)] Recompiling..."
  if mvn compile -q -DskipTests; then
    echo "[$(date +%H:%M:%S)] Compile OK"
  else
    echo "[$(date +%H:%M:%S)] Compile FAILED" >&2
  fi
}

echo "Watching src/ for changes (Ctrl+C to stop)..."

if command -v fswatch >/dev/null 2>&1; then
  fswatch -o -r src/main | while read -r _; do
    compile
  done
else
  echo "Tip: install fswatch for faster reloads (brew install fswatch)"
  LAST_HASH=""
  while true; do
    HASH="$(find src/main -name '*.java' -type f -exec stat -f '%m %N' {} + 2>/dev/null | md5 -q)"
    if [[ "${HASH}" != "${LAST_HASH}" ]]; then
      LAST_HASH="${HASH}"
      compile
    fi
    sleep 2
  done
fi
