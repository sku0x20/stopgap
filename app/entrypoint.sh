#!/bin/sh
# Migrates, then starts the app; "migrate" only migrates, for a separate step or init container. dbmate reads
# DATABASE_URL, which may carry a schema-owning user apart from the app's DB_POSTGRES_* one.
set -e
dbmate --migrations-dir /app/stopgap/db/migrations --no-dump-schema --wait --migration-lock up
if [ "$1" = "migrate" ]; then
    exit 0
fi
# Crash diagnostics (RFC-0002) go to one folder per start: java is always PID 1 in the container, so the JVM's default
# names (java_pid1.hprof) collide after a restart and the second heap dump is skipped.
dir=/diagnostics/$(date -u +%Y%m%dT%H%M%SZ)
mkdir -p "$dir"
# exec keeps java as PID 1, so it receives the container's SIGTERM and the JFR recording is dumped on exit.
# otel.semconv-stability.opt-in is a system property: the JDBC library reads it directly, not from otel.properties.
exec java \
    "-XX:StartFlightRecording=name=continuous,settings=default,maxage=10m,dumponexit=true,filename=$dir/" \
    "-XX:FlightRecorderOptions=dumppath=$dir" \
    -XX:+HeapDumpOnOutOfMemoryError \
    "-XX:HeapDumpPath=$dir" \
    -XX:+CrashOnOutOfMemoryError \
    "-XX:ErrorFile=$dir/hs_err.log" \
    -javaagent:otel/opentelemetry-javaagent.jar \
    -Dotel.javaagent.configuration-file=otel/otel.properties \
    -Dotel.javaagent.extensions=otel/extensions \
    -Dpyroscope.configuration.file=otel/pyroscope.properties \
    -Dotel.semconv-stability.opt-in=database \
    -jar stopgap-app.jar \
    "$@"
