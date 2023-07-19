#!/bin/sh
# 设置生产环境
sed -i 's/env=dev/env=pro/g' gradle.properties

# 安装到本地库
#gradle install -x test
gradle :jksoa-common:install -x test
gradle :jksoa-rpc:jksoa-rpc-client:install -x test
gradle :jksoa-rpc:jksoa-rpc-server:install -x test
gradle :jksoa-rpc:jksoa-rpc-k8s-discovery:install -x test
gradle :jksoa-tracer:jksoa-tracer-jaeger:install -x test
gradle :jksoa-dtx:jksoa-dtx-mq:install -x test
gradle :jksoa-dtx:jksoa-dtx-tcc:install -x test

# 恢复开发环境
sed -i 's/env=pro/env=dev/g' gradle.properties
