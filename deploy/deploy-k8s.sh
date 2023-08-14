# 持续部署：部署到k8s集群
# 例子： deploy.sh k8s 1.0
#!/bin/sh
# 需要父脚本提供pro/tag/ns变量

if [ ! -f "k8s.yml" ]; then
  echo "缺少文件k8s.yml"
  exit 1;
fi

echo "部署k8s应用 $ns-$pro"
echo "1 生成k8s资源文件"
echo "K8sBoot k8s.yml -o k8sboot -d \"ns=$ns&tag=$tag\""
K8sBoot k8s.yml -o k8sboot -d "ns=$ns&tag=$tag"
sleep 3
echo "2 应用k8s资源文件"
# kubectl apply --record=true -f k8sboot