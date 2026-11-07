#!/bin/sh
# Migrates, then starts the app; "migrate" only migrates, for a separate step or init container. dbmate reads
# DB_MIGRATE_URL, which may carry a schema-owning user apart from the app's DB_POSTGRES_* one.
set -e
dbmate --env DB_MIGRATE_URL --migrations-dir /app/stopgap/db/migrations --no-dump-schema --wait --migration-lock up
if [ "$1" = "migrate" ]; then
    exit 0
fi
# exec makes the app PID 1, so it receives the container's SIGTERM.
exec /app/stopgap/stopgap-app "$@"
