#!/usr/bin/env bash
set -euo pipefail

BACKUP_DIR="${BACKUP_DIR:-/var/backups/vegetable-market}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"

: "${PGHOST:=127.0.0.1}"
: "${PGPORT:=5432}"
: "${PGDATABASE:=vegetable_db}"
: "${PGUSER:=veguser}"

mkdir -p "$BACKUP_DIR"
chmod 700 "$BACKUP_DIR"

TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
BACKUP_FILE="$BACKUP_DIR/${PGDATABASE}_${TIMESTAMP}.dump"

pg_dump \
  --format=custom \
  --no-owner \
  --no-privileges \
  --file="$BACKUP_FILE" \
  "$PGDATABASE"

chmod 600 "$BACKUP_FILE"
find "$BACKUP_DIR" -type f -name "${PGDATABASE}_*.dump" -mtime "+$RETENTION_DAYS" -delete

echo "Backup created: $BACKUP_FILE"
