#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/.env.local"

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "Missing ${ENV_FILE}. Create it with database and JWT credentials."
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

cd "${ROOT_DIR}"

chmod +x "${ROOT_DIR}/scripts/watch-compile.sh"
"${ROOT_DIR}/scripts/watch-compile.sh" &
WATCHER_PID=$!
trap 'kill "${WATCHER_PID}" 2>/dev/null || true' EXIT INT TERM

echo "Hot reload enabled: save Java files to recompile and restart the API."
mvn spring-boot:run -Dspring-boot.run.profiles=local
