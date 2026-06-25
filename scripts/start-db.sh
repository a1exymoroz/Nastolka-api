#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ROOT_DIR}/.env.local"

if [[ ! -f "${ENV_FILE}" ]]; then
  echo "Missing ${ENV_FILE}. Create it with database and JWT credentials."
  exit 1
fi

cd "${ROOT_DIR}"

if command -v docker-compose >/dev/null 2>&1; then
  docker-compose --env-file "${ENV_FILE}" up -d
elif docker compose version >/dev/null 2>&1; then
  docker compose --env-file "${ENV_FILE}" up -d
else
  echo "Docker Compose is not installed."
  exit 1
fi

echo "PostgreSQL is starting. Check status with: docker-compose ps"
