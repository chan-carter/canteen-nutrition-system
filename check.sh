#!/usr/bin/env bash
# 提交前本地跑一遍，内容和 .github/workflows/ci.yml 相同：后端测试 + 前端构建
set -euo pipefail
cd "$(dirname "$0")"

echo "==> 后端单元测试 (canteen-nutrition)"
mvn -q -B -f canteen-nutrition/pom.xml test

echo "==> 前端生产构建 (canteen-nutrition-web)"
if [ ! -d canteen-nutrition-web/node_modules ]; then
  npm --prefix canteen-nutrition-web ci
fi
npm --prefix canteen-nutrition-web run build

echo "==> 通过：测试与构建均无报错"
