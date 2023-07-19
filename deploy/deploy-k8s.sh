# 持续部署：部署到k8s集群

#!/bin/sh
# 需要父脚本提供pro/tag/ns变量

if [ ! -f "k8s.yml" ]; then
  echo "缺少文件k8s.yml"
  exit 1;
fi

echo "部署k8s服务 $ns-$pro"
echo "1 生成k8s资源文件"
K8sBoot k8s.yml -d ns=$ns&tag=$tag -o k8sboot
echo "2 应用k8s资源文件"
kubectl apply --record=true -f k8sboot