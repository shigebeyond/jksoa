# 应用与环境等配置
考虑到2种部署情况: 本地部署 vs k8s部署

## 本地部署: 应用名与命名空间读jkapp.yaml配置文件
jkapp.yaml
```yaml
# k8s命名空间, 可能包含环境(pro/dev/test)+版本, 仅本地部署时有效, 当k8s部署并设置了环境变量POD_NAMESPACE, 则此配置项无效
namespace: dev
# k8s应用名, 仅本地部署时有效, 当k8s部署并设置了环境变量APP_NAME, 则此配置项无效
name: jkapp
# 是否应用协程, 与useSttl不兼容, 如果useFiber=true, 则会强制使得 useSttl=false
useFiber: false
# 是否应用可传递ScopedTransferableThreadLocal, 影响 1. CompletableFuture 的线程池 2. 公共线程池 3. IRequestScope#sttlWrap()
useSttl: true
```

## k8s部署: 应用名与命名空间优先读环境变量
参考 [K8sBoot](https://github.com/shigebeyond/K8sBoot) 框架的k8s资源定义
```yaml
- app(xxx):
    - containers:
        xxx: # 定义多个容器, dict形式, 键是容器名, 值是容器配置
          image: nginx # 镜像
          env: # 以dict方式设置环境变量
            APP_NAME: $app # 引用app名
            POD_NAMESPACE: ${ref_pod_field(metadata.namespace)} # 引用k8s命名空间
    - deploy: 1
```