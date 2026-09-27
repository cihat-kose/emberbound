#!/bin/sh
set -eu
cd "$(CDPATH='' cd -- "$(dirname -- "$0")" && pwd)"
sh ./mvnw -q -DskipTests package
if [ -n "${JAVA_HOME:-}" ]; then
    exec "$JAVA_HOME/bin/java" -jar target/emberbound.jar "$@"
fi
exec java -jar target/emberbound.jar "$@"
