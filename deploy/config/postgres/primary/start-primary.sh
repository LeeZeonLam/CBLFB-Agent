#!/bin/sh
set -e

REPLICATION_USER="${REPLICATION_USER:-replicator}"
REPLICATION_PASSWORD="${REPLICATION_PASSWORD:-replicator_pass}"

rm -f /tmp/replication-user-ready

docker-entrypoint.sh postgres -c config_file=/etc/postgresql/postgresql.conf -c hba_file=/etc/postgresql/pg_hba.conf &
POSTGRES_PID=$!

term_handler() {
  kill -TERM "$POSTGRES_PID" 2>/dev/null || true
  wait "$POSTGRES_PID" || true
  exit 0
}

trap term_handler INT TERM

MAX_RETRIES=60
RETRY_COUNT=0
until pg_isready -h localhost -p 5432 -U "${POSTGRES_USER}" -d "${POSTGRES_DB}" >/dev/null 2>&1; do
  RETRY_COUNT=$((RETRY_COUNT + 1))
  if [ "$RETRY_COUNT" -ge "$MAX_RETRIES" ]; then
    wait "$POSTGRES_PID"
    exit 1
  fi
  sleep 1
done

psql -v ON_ERROR_STOP=1 --username "${POSTGRES_USER}" --dbname "${POSTGRES_DB}" <<-EOSQL
DO \$\$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = '${REPLICATION_USER}') THEN
    EXECUTE format('CREATE USER %I WITH REPLICATION ENCRYPTED PASSWORD %L', '${REPLICATION_USER}', '${REPLICATION_PASSWORD}');
  ELSE
    EXECUTE format('ALTER USER %I WITH REPLICATION ENCRYPTED PASSWORD %L', '${REPLICATION_USER}', '${REPLICATION_PASSWORD}');
  END IF;
END
\$\$;
EOSQL

touch /tmp/replication-user-ready

wait "$POSTGRES_PID"
