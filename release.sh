#!/usr/bin/env bash
set -euo pipefail

# ── Config ────────────────────────────────────────────────
IMAGE_NAME="home-rental-service"
RELEASE_DIR="release"

# ── Version: git short hash + date, or a passed-in label ──
VERSION="${1:-$(git -C "$(dirname "$0")" rev-parse --short HEAD 2>/dev/null || echo "dev")-$(date +%Y%m%d%H%M)}"
FULL_TAG="${IMAGE_NAME}:${VERSION}"

echo "==> Building image  ${FULL_TAG}"
docker build \
  --tag "${FULL_TAG}" \
  --tag "${IMAGE_NAME}:latest" \
  --file "$(dirname "$0")/Dockerfile" \
  "$(dirname "$0")"

# ── Prepare release directory ─────────────────────────────
rm -rf "${RELEASE_DIR}"
mkdir -p "${RELEASE_DIR}"

echo "==> Exporting image to ${RELEASE_DIR}/${IMAGE_NAME}-${VERSION}.tar"
docker save "${FULL_TAG}" -o "${RELEASE_DIR}/${IMAGE_NAME}-${VERSION}.tar"

# ── Write the mobile-dev docker-compose ──────────────────
cat > "${RELEASE_DIR}/docker-compose.yml" <<COMPOSE
services:

  app:
    image: ${FULL_TAG}
    container_name: home-rental-app
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: local
      SERVER_PORT: 8080
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/home_rental_db
      SPRING_DATASOURCE_USERNAME: appuser
      SPRING_DATASOURCE_PASSWORD: changeme
      JWT_SECRET: xbBPqZAxi5mEfhkrQGm1z2MvNOQcYkkaisktXr15CAk=
    depends_on:
      db:
        condition: service_healthy
    networks:
      - home-rental-net
    restart: on-failure

  db:
    image: postgres:16-alpine
    container_name: home-rental-db
    environment:
      POSTGRES_DB: home_rental_db
      POSTGRES_USER: appuser
      POSTGRES_PASSWORD: changeme
    volumes:
      - home-rental-db-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U appuser -d home_rental_db"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 10s
    networks:
      - home-rental-net
    restart: unless-stopped

volumes:
  home-rental-db-data:

networks:
  home-rental-net:
    driver: bridge
COMPOSE

# ── Write quick-start instructions ───────────────────────
cat > "${RELEASE_DIR}/README.txt" <<README
Home Rental Service  —  Local backend for mobile dev
=====================================================

1. Load the image into Docker (one time per release):

     docker load -i ${IMAGE_NAME}-${VERSION}.tar

2. Start the backend + database:

     docker compose up -d

   The API is available at http://localhost:8080
   Health check:  GET http://localhost:8080/health

3. Stop everything:

     docker compose down

4. To wipe the database and start fresh:

     docker compose down -v
README

# ── Bundle everything into a single archive ───────────────
ARCHIVE="${IMAGE_NAME}-${VERSION}.tar.gz"
tar -czf "${ARCHIVE}" -C "${RELEASE_DIR}" .

echo ""
echo "==> Done!"
echo "    Archive : ${ARCHIVE}"
echo "    Contents: image tar + docker-compose.yml + README.txt"
echo ""
echo "    Send '${ARCHIVE}' to the mobile dev."
echo "    They extract it, run:  docker load -i ${IMAGE_NAME}-${VERSION}.tar && docker compose up -d"
