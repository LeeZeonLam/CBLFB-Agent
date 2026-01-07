#!/bin/bash

# 确保脚本使用 bash 运行
if [ -z "$BASH_VERSION" ]; then
    echo "警告: 检测到当前使用 sh/dash 运行，正在切换到 bash..."
    exec bash "$0" "$@"
fi

# 定义颜色
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 获取脚本所在目录的绝对路径
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
PROJECT_ROOT="$SCRIPT_DIR/.."

echo -e "${GREEN}================================================${NC}"
echo -e "${GREEN}       FBA LogiAI 部署脚本 (Server版)       ${NC}"
echo -e "${GREEN}================================================${NC}"

# 0. 尝试修复 PATH (适配多种环境)
# macOS Docker Desktop
if [ -d "/Applications/Docker.app/Contents/Resources/bin" ]; then
    export PATH="$PATH:/Applications/Docker.app/Contents/Resources/bin"
fi
# Linux Snap
if [ -d "/snap/bin" ]; then
    export PATH="$PATH:/snap/bin"
fi
# 确保常见路径在 PATH 中 (防止 sudo 重置 PATH 导致找不到命令)
for path in /usr/local/bin /usr/bin /bin /usr/local/sbin /usr/sbin /sbin; do
    if [ -d "$path" ] && [[ ":$PATH:" != *":$path:"* ]]; then
        export PATH="$PATH:$path"
    fi
done

# 1. 检查必要工具
echo -e "${YELLOW}[1/5] 检查环境依赖...${NC}"
if ! command -v docker &> /dev/null; then
    echo -e "${RED}错误: 未检测到 docker 命令。${NC}"
    echo -e "${YELLOW}调试信息: 当前 PATH=$PATH${NC}"
    echo -e "${YELLOW}尝试: 如果你已安装 Docker，请检查它是否在系统 PATH 中。${NC}"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    # 尝试检查 docker compose (v2)
    if ! docker compose version &> /dev/null; then
        echo -e "${RED}错误: 未检测到 Docker Compose，请先安装。${NC}"
        exit 1
    else
        DOCKER_COMPOSE_CMD="docker compose"
    fi
else
    DOCKER_COMPOSE_CMD="docker-compose"
fi
echo -e "${GREEN}环境依赖检查通过。使用命令: $DOCKER_COMPOSE_CMD${NC}"

# 2. 准备环境变量
echo -e "${YELLOW}[2/5] 检查环境变量配置...${NC}"
if [ ! -f "$SCRIPT_DIR/.env" ]; then
    echo -e "${YELLOW}未找到 .env 文件，正在从 .env.example 复制...${NC}"
    if [ -f "$SCRIPT_DIR/.env.example" ]; then
        cp "$SCRIPT_DIR/.env.example" "$SCRIPT_DIR/.env"
        echo -e "${GREEN}已创建 .env 文件。请稍后根据需要修改其中的敏感配置。${NC}"
    else
        echo -e "${RED}错误: 未找到 .env.example 模板文件。${NC}"
        exit 1
    fi
else
    echo -e "${GREEN}.env 文件已存在。${NC}"
fi

# 3. 初始化目录结构
echo -e "${YELLOW}[3/5] 初始化数据卷目录...${NC}"
VOLUMES_DIR="$SCRIPT_DIR/volumes"
DIRS=(
    "postgres/primary"
    "postgres/replica"
    "redis/primary"
    "redis/replica1"
    "redis/replica2"
    "milvus/etcd"
    "milvus/rustfs"
    "milvus/milvus"
    "kafka/data"
    "elasticsearch"
    "prometheus"
    "grafana"
    "loki"
    "alertmanager"
    "logs"
)

for dir in "${DIRS[@]}"; do
    TARGET_DIR="$VOLUMES_DIR/$dir"
    if [ ! -d "$TARGET_DIR" ]; then
        echo "创建目录: $TARGET_DIR"
        mkdir -p "$TARGET_DIR"
    fi
done

# 4. 设置权限 (关键步骤)
echo -e "${YELLOW}[4/5] 设置特殊容器目录权限...${NC}"
# Elasticsearch (通常需要 uid 1000)
chmod 777 "$VOLUMES_DIR/elasticsearch"
# Kafka (Bitnami 镜像通常使用 uid 1001)
chmod 777 "$VOLUMES_DIR/kafka/data"
# Prometheus/Grafana (通常需要写入权限)
chmod 777 "$VOLUMES_DIR/prometheus"
chmod 777 "$VOLUMES_DIR/grafana"
# Logs
chmod 777 "$VOLUMES_DIR/logs"

# 赋予 Shell 脚本执行权限
chmod +x "$SCRIPT_DIR/config/postgres/primary/"*.sh 2>/dev/null
chmod +x "$SCRIPT_DIR/config/postgres/replica/"*.sh 2>/dev/null

echo -e "${GREEN}目录权限设置完成。${NC}"

# 5. 交互菜单
show_menu() {
    echo -e "${YELLOW}[5/5] 请选择操作:${NC}"
    echo -e "${GREEN}--- 启动服务 ---${NC}"
    echo "1) 启动基础设施 (MySQL, Redis, Kafka, Nacos, Milvus)"
    echo "2) 启动可观测性组件 (Prometheus, Grafana, SkyWalking, ELK)"
    echo "3) 启动微服务 (Gateway, Chat, Data, RAG)"
    echo "4) 启动所有服务 (All-in-One)"
    echo -e "${RED}--- 停止服务 ---${NC}"
    echo "5) 停止基础设施"
    echo "6) 停止可观测性组件"
    echo "7) 停止微服务"
    echo "8) 停止所有服务"
    echo -e "${NC}--- 其他 ---${NC}"
    echo "0) 退出"
}

show_menu
read -p "请输入选项 [0-8]: " choice

case $choice in
    1)
        echo -e "${GREEN}正在启动基础设施...${NC}"
        $DOCKER_COMPOSE_CMD -f "$SCRIPT_DIR/docker-compose-infra.yml" --env-file "$SCRIPT_DIR/.env" up -d
        ;;
    2)
        echo -e "${GREEN}正在启动可观测性组件...${NC}"
        $DOCKER_COMPOSE_CMD -f "$SCRIPT_DIR/docker-compose-observability.yml" --env-file "$SCRIPT_DIR/.env" up -d
        ;;
    3)
        echo -e "${GREEN}正在启动微服务...${NC}"
        $DOCKER_COMPOSE_CMD -f "$SCRIPT_DIR/docker-compose-services.yml" --env-file "$SCRIPT_DIR/.env" up -d
        ;;
    4)
        echo -e "${GREEN}正在启动所有服务...${NC}"
        $DOCKER_COMPOSE_CMD -f "$SCRIPT_DIR/docker-compose-infra.yml" \
                           -f "$SCRIPT_DIR/docker-compose-observability.yml" \
                           -f "$SCRIPT_DIR/docker-compose-services.yml" \
                           --env-file "$SCRIPT_DIR/.env" up -d
        ;;
    5)
        echo -e "${YELLOW}正在停止基础设施...${NC}"
        $DOCKER_COMPOSE_CMD -f "$SCRIPT_DIR/docker-compose-infra.yml" --env-file "$SCRIPT_DIR/.env" down
        ;;
    6)
        echo -e "${YELLOW}正在停止可观测性组件...${NC}"
        $DOCKER_COMPOSE_CMD -f "$SCRIPT_DIR/docker-compose-observability.yml" --env-file "$SCRIPT_DIR/.env" down
        ;;
    7)
        echo -e "${YELLOW}正在停止微服务...${NC}"
        $DOCKER_COMPOSE_CMD -f "$SCRIPT_DIR/docker-compose-services.yml" --env-file "$SCRIPT_DIR/.env" down
        ;;
    8)
        echo -e "${YELLOW}正在停止所有服务...${NC}"
        $DOCKER_COMPOSE_CMD -f "$SCRIPT_DIR/docker-compose-infra.yml" \
                           -f "$SCRIPT_DIR/docker-compose-observability.yml" \
                           -f "$SCRIPT_DIR/docker-compose-services.yml" \
                           --env-file "$SCRIPT_DIR/.env" down
        ;;
    0)
        echo "退出。"
        exit 0
        ;;
    *)
        echo -e "${RED}无效选项${NC}"
        exit 1
        ;;
esac

if [ $? -eq 0 ]; then
    echo -e "${GREEN}操作执行成功！${NC}"
    echo -e "提示: 如果是首次启动，请等待几分钟让数据库和中间件完成初始化。"
else
    echo -e "${RED}操作执行失败，请检查错误日志。${NC}"
fi
