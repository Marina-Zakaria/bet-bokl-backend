#!/usr/bin/env bash
# Single-command launcher for home-rental-service
# Usage:  ./start.sh
set -euo pipefail

cd "$(dirname "$0")"

echo "==> Starting home-rental-service (build + up)..."
docker compose up --build
