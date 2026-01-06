# FBA LogiAI 生产级基础设施部署指南

本文档详细说明如何使用 Docker 容器化部署 FBA LogiAI 系统所需的基础设施，包括 PostgreSQL, Redis 和 Milvus 向量数据库。

## 1. 部署架构概览

我们将使用 Docker Compose 编排以下服务：

*   **PostgreSQL 15**: 关系型数据库，用于存储业务数据（订单、活动、库存等）。
*   **Redis 7**: 高性能缓存与消息队列。
*   **Milvus 2.3+ (Standalone)**: 生产级向量数据库，包含以下组件：
    *   **Milvus Server**: 核心服务。
    *   **Etcd**: 元数据存储与服务发现。
    *   **MinIO**: 对象存储（用于持久化向量索引和日志）。

## 2. 前置准备

*   **操作系统**: Linux (推荐 Ubuntu 22.04) / macOS / Windows WSL2
*   **Docker**: 版本 >= 20.10
*   **Docker Compose**: 版本 >= 2.0
*   **硬件要求**: 至少 8GB RAM (Milvus 对内存要求较高)

## 3. 部署步骤

### 3.1 目录结构准备

确保已创建 `deploy` 目录并包含 `docker-compose-infra.yml` 文件。

```bash
cd deploy
```

### 3.2 启动服务

使用以下命令一键启动所有基础设施服务：

```bash
docker-compose -f docker-compose-infra.yml up -d
```

### 3.3 验证服务状态

查看容器运行状态：

```bash
docker-compose -f docker-compose-infra.yml ps
```

预期输出：
*   `fba-postgres`: Up (healthy)
*   `fba-redis`: Up (healthy)
*   `milvus-standalone`: Up (healthy)
*   `milvus-etcd`: Up
*   `milvus-minio`: Up (healthy)

### 3.4 访问管理界面

*   **MinIO Console** (Milvus 存储后台):
    *   地址: http://localhost:9001
    *   账号: `minioadmin`
    *   密码: `minioadmin`

*   **Milvus**:
    *   gRPC 端口: `19530` (供后端应用连接)

## 4. 连接配置说明

在您的应用程序 (`.env` 或环境变量) 中，请使用以下配置连接到这些服务：

### PostgreSQL
*   **Host**: `localhost` (本地开发) 或 `postgres` (容器网络内)
*   **Port**: `5432`
*   **User**: `fba_admin`
*   **Password**: `fba_secret_pass`
*   **Database**: `fba_logi_db`

### Redis
*   **Host**: `localhost` (本地开发) 或 `redis` (容器网络内)
*   **Port**: `6379`
*   **Password**: `fba_redis_pass`

### Milvus
*   **URI**: `http://localhost:19530` (本地开发) 或 `http://milvus-standalone:19530` (容器网络内)
*   **Token**: (默认无，除非开启了鉴权)

## 5. 数据持久化与备份

所有数据均挂载在 `deploy/volumes` 目录下，请定期备份此目录：

*   `deploy/volumes/postgres`: 业务数据库文件
*   `deploy/volumes/redis`: Redis AOF/RDB 文件
*   `deploy/volumes/milvus`: 向量索引数据
*   `deploy/volumes/rustfs`: RustFS 对象存储数据
*   `deploy/volumes/etcd`: Etcd 元数据

## 6. 常见运维命令

**查看日志**:
```bash
docker-compose -f docker-compose-infra.yml logs -f milvus-standalone
```

**停止服务**:
```bash
docker-compose -f docker-compose-infra.yml down
```

**彻底清理 (警告：会删除所有数据)**:
```bash
docker-compose -f docker-compose-infra.yml down -v
rm -rf volumes/
```
