#!/bin/sh
set -e

REPLICATOR_PASSWORD="${PGPASSWORD:-replicator_pass}"
REPLICATOR_USER="${PGUSER:-replicator}"
MAX_RETRIES=30
RETRY_COUNT=0

echo "等待主节点就绪..."

# 等待主节点接受连接
until PGPASSWORD="$REPLICATOR_PASSWORD" pg_isready -h pg-primary -p 5432 -U "$REPLICATOR_USER"; do
  RETRY_COUNT=$((RETRY_COUNT + 1))
  if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
    echo "错误: 主节点连接超时"
    exit 1
  fi
  echo "等待主节点就绪... ($RETRY_COUNT/$MAX_RETRIES)"
  sleep 2
done

echo "主节点已就绪"

# 如果数据目录为空，从主节点同步
if [ -z "$(ls -A /var/lib/postgresql/data 2>/dev/null)" ]; then
  echo "从主节点同步数据..."

  # 重试机制：等待 replicator 用户创建完成
  RETRY_COUNT=0
  while :; do
    OUTPUT=$(PGPASSWORD="$REPLICATOR_PASSWORD" pg_basebackup -h pg-primary -D /var/lib/postgresql/data -U "$REPLICATOR_USER" -vP -R -X stream -C -S replica_slot 2>&1) && break
    echo "$OUTPUT" | grep -qi "already exists" \
      && PGPASSWORD="$REPLICATOR_PASSWORD" pg_basebackup -h pg-primary -D /var/lib/postgresql/data -U "$REPLICATOR_USER" -vP -R -X stream -S replica_slot \
      && break

    RETRY_COUNT=$((RETRY_COUNT + 1))
    if [ $RETRY_COUNT -ge $MAX_RETRIES ]; then
      echo "错误: 无法从主节点同步数据，请检查 replicator 用户是否已创建"
      echo "$OUTPUT"
      exit 1
    fi
    echo "等待 replicator 用户就绪... ($RETRY_COUNT/$MAX_RETRIES)"
    sleep 3
  done

  # 设置权限
  chmod 700 /var/lib/postgresql/data
  echo "数据同步完成"
fi

# 启动 PostgreSQL
echo "启动 PostgreSQL 从节点..."
exec docker-entrypoint.sh postgres
