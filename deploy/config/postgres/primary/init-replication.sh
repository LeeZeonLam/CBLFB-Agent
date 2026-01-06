#!/bin/sh
set -e

# 创建复制用户（幂等操作，支持重复执行）
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    DO \$\$
    BEGIN
        IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'replicator') THEN
            CREATE USER replicator WITH REPLICATION ENCRYPTED PASSWORD 'replicator_pass';
            RAISE NOTICE '复制用户 replicator 创建完成';
        ELSE
            ALTER USER replicator WITH PASSWORD 'replicator_pass';
            RAISE NOTICE '复制用户 replicator 已存在，密码已更新';
        END IF;
    END
    \$\$;
EOSQL

echo "复制用户初始化完成"
