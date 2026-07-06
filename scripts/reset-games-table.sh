#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/.env.local"

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "Missing ${ENV_FILE}"
  exit 1
fi

set -a
# shellcheck disable=SC1090
source "${ENV_FILE}"
set +a

docker exec -i nastolka-postgres psql -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" \
  < "${ROOT_DIR}/scripts/reset-games-table.sql"

echo "games table reset. Old columns (available, title, min_players, etc.) removed."
