对于使用jksoa框架封装的服务部署, 可参考以下部署脚本

## 封装部署脚本
参考 [jksoa-rpc/jksoa-rpc-server/deploy.sh](../jksoa-rpc/jksoa-rpc-server/deploy.sh)

```
#!/bin/sh

# 项目目录
cd `dirname $0`

# 导出环境变量，以便在子脚本中引用: 项目名
export pro=rpcserver # 服务名
export stack=jksoa # stack名, 即swarm进群名, 包含多个互通的服务

# 调用部署入口脚本, 接收2个参数: 1 子命令 2 版本
../../deploy/deploy-entry.sh $*
```

## 部署脚本执行
### 1. 持续集成
1.1 如果仅仅构建的话:
```
gradle build -x test -Pall
```
 
1.2 如果有单元测试的话: 
直接使用
```
gradle build -Pall
```

1.3 如果要加上自动化测试: 
先搭建服务: 编译打包+上传到测试服上+启动rpc server
```
./deploy.sh scp 1.9.0 
```

再使用自动化测试工具(如HttpRunnerManager等)来测试

扩展: 使用jenkins来跑脚本

### 2. 持续交付
打包docker镜像, 并上传到私有仓库 192.168.0.182:5000 中
```
./deploy.sh image 1.9.0 
```

### 3. 持续部署
部署到k8s集群中
```
./deploy.sh k8s 1.9.0 
```