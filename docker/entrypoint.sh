#!/bin/sh
set -eu

if [ -z "${SPRING_DATASOURCE_URL:-}" ] && [ -n "${DATABASE_URL:-}" ]; then
  db_url="${DATABASE_URL#postgresql://}"
  db_url="${db_url#*@}"
  export SPRING_DATASOURCE_URL="jdbc:postgresql://${db_url}"
fi

exec java -jar /app/app.jar
