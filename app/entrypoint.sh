#!/bin/sh
# Crash diagnostics (RFC-0002) go to one folder per start: java is always PID 1 in the container, so the JVM's default
# names (java_pid1.hprof) collide after a restart and the second heap dump is skipped.
dir=/diagnostics/$(date -u +%Y%m%dT%H%M%SZ)
mkdir -p "$dir"
# exec keeps java as PID 1, so it receives the container's SIGTERM and the JFR recording is dumped on exit.
exec java \
    "-XX:StartFlightRecording=name=continuous,settings=default,maxage=10m,dumponexit=true,filename=$dir/" \
    "-XX:FlightRecorderOptions=dumppath=$dir" \
    -XX:+HeapDumpOnOutOfMemoryError \
    "-XX:HeapDumpPath=$dir" \
    -XX:+CrashOnOutOfMemoryError \
    "-XX:ErrorFile=$dir/hs_err.log" \
    "$@"
