#!/bin/sh
# 设置生产环境
sed -i 's/env=dev/env=pro/g' gradle.properties

# 安装到本地库
#gradle install -x test
gradle :jksoa-common:install -x test
gradle :jksoa-rpc:jksoa-rpc-registry:install -x test
gradle :jksoa-rpc:jksoa-rpc-client:install -x test
gradle :jksoa-rpc:jksoa-rpc-server:install -x test
gradle :jksoa-mq:jksoa-mq-common:install -x test
gradle :jksoa-mq:jksoa-mq-registry:install -x test
gradle :jksoa-mq:jksoa-mq-client:install -x test
gradle :jksoa-mq:jksoa-mq-broker:install -x test
gradle :jksoa-tracer:jksoa-tracer-common:install -x test
gradle :jksoa-tracer:jksoa-tracer-agent:install -x test
gradle :jksoa-tracer:jksoa-tracer-collector:install -x test
gradle :jksoa-dtx:jksoa-dtx-mq:install -x test
gradle :jksoa-dtx:jksoa-dtx-tcc:install -x test

# 恢复开发环境
sed -i 's/env=pro/env=dev/g' gradle.properties
