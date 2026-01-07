#!/bin/bash

# 检查是否为 root
if [ "$EUID" -ne 0 ]; then
  echo "请使用 sudo 运行此脚本"
  exit 1
fi

echo "正在配置 Docker 镜像加速器..."

mkdir -p /etc/docker

# 写入阿里云、网易云等国内镜像源
# 注意：由于国内 Docker 环境变化频繁，这些源可能随时失效，建议使用自己的阿里云加速器地址
cat > /etc/docker/daemon.json <<EOF
{
  "registry-mirrors": [
    "https://docker.m.daocloud.io",
    "https://huecker.io",
    "https://dockerhub.timeweb.cloud",
    "https://noohub.ru"
  ]
}
EOF

echo "配置完成，正在重启 Docker..."
systemctl daemon-reload
systemctl restart docker

echo "Docker 重启完毕。请重新尝试运行 deploy.sh"
