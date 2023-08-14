## k8s.yml (K8sBoot语法)
按照 [k8s部署](./deploy-k8s.md) 的说明，其中k8s部署主要看 `k8s.yml`，它是用 [K8sBoot](https://github.com/shigebeyond/K8sBoot) 来解析并生成最终的k8s资源文件

```yaml
- ns: $ns # 命名空间
- app(rpcserver): # 应用
  - containers:
      rpcserver:
        # 这是 deliver-image.sh 打包的镜像，并上传到私有仓库192.168.0.182
        image: 192.168.0.182:5000/rpcserver:${tag}
        env: # 以dict方式设置环境变量
          TZ: Asia/Shanghai
          # 引用pod信息
          POD_NAME: ${ref_pod_field(metadata.name)}
          POD_NAMESPACE: ${ref_pod_field(metadata.namespace)}
          POD_IP: ${ref_pod_field(status.podIP)}
        ports: # 端口映射
          - 9080:9080 # 服务端口:容器端口
      visualizer:
        image: dockersamples/visualizer
        ports: # 端口映射
          - 8081:8080 # 服务端口:容器端口
  - deploy: 20 # 副本数
```

## 最终生成的k8s资源定义文件
生成了2个文件

1. rpcserver-deploy.yml
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  labels: &id001
    app: rpcserver
  name: rpcserver
  namespace: default
spec:
  replicas: 20
  selector:
    matchLabels: *id001
  template:
    metadata:
      labels: *id001
    spec:
      containers:
      - env:
        - name: TZ
          value: Asia/Shanghai
        - name: POD_NAME
          valueFrom:
            fieldRef:
              fieldPath: metadata.name
        - name: POD_NAMESPACE
          valueFrom:
            fieldRef:
              fieldPath: metadata.namespace
        - name: POD_IP
          valueFrom:
            fieldRef:
              fieldPath: status.podIP
        image: 192.168.0.182:5000/rpcserver:3.0.0
        imagePullPolicy: IfNotPresent
        name: rpcserver
        ports:
        - containerPort: 9080
      - image: dockersamples/visualizer
        imagePullPolicy: IfNotPresent
        name: visualizer
        ports:
        - containerPort: 8080
      restartPolicy: Always
      volumes: []
```

2. rpcserver-svc.yml
```yaml
apiVersion: v1
kind: Service
metadata:
  annotations:
    kube-router.io/service.scheduler: lc # 依赖于 kube-router的lc算法
  labels: &id001
    app: rpcserver
  name: rpcserver
  namespace: default
spec:
  ports:
  - name: p9080
    port: 9080
    protocol: TCP
    targetPort: 9080
  - name: p8080
    port: 8081
    protocol: TCP
    targetPort: 8080
  selector: *id001
  type: ClusterIP
status:
  loadBalancer: {}
```
