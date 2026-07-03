#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/.env.local"

require_env() {
  local missing=()
  for var in POSTGRES_DB POSTGRES_USER POSTGRES_PASSWORD POSTGRES_HOST POSTGRES_PORT SERVER_PORT APP_JWT_SECRET; do
    if [[ -z "${!var:-}" ]]; then
      missing+=("${var}")
    fi
  done
  if ((${#missing[@]} > 0)); then
    echo "Missing required variables in ${ENV_FILE}: ${missing[*]}"
    exit 1
  fi
}

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "Missing ${ENV_FILE}. Create it with database and JWT credentials."
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

require_env

"${ROOT_DIR}/scripts/start-db.sh"

cd "${ROOT_DIR}"

chmod +x "${ROOT_DIR}/scripts/watch-compile.sh"
"${ROOT_DIR}/scripts/watch-compile.sh" &
WATCHER_PID=$!
trap 'kill "${WATCHER_PID}" 2>/dev/null || true' EXIT INT TERM

echo "Hot reload enabled: save Java files to recompile and restart the API."
mvn spring-boot:run -Dspring-boot.run.profiles=local
