#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/.env.local"

compose() {
  if command -v docker-compose >/dev/null 2>&1; then
    docker-compose --env-file "${ENV_FILE}" "$@"
  elif docker compose version >/dev/null 2>&1; then
    docker compose --env-file "${ENV_FILE}" "$@"
  else
    echo "Docker Compose is not installed."
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

if ! docker info >/dev/null 2>&1; then
  echo "Docker is not running. Start Docker (or Colima) and try again."
  exit 1
fi

cd "${ROOT_DIR}"
compose up -d

POSTGRES_HOST="${POSTGRES_HOST:-localhost}"
POSTGRES_PORT="${POSTGRES_PORT:-5433}"
ADMINER_PORT="${ADMINER_PORT:-8091}"

echo "Waiting for PostgreSQL at ${POSTGRES_HOST}:${POSTGRES_PORT}..."
for _ in $(seq 1 30); do
  if compose ps postgres 2>/dev/null | grep -q "(healthy)"; then
    echo "PostgreSQL is ready on port ${POSTGRES_PORT}."
    echo "Adminer (browser): http://localhost:${ADMINER_PORT}"
    exit 0
  fi
  sleep 1
done

echo "PostgreSQL did not become healthy in time. Check: compose ps"
compose ps
exit 1
