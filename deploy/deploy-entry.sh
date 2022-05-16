# 部署入口脚本, 接收2个参数: 1 子命令 2 版本
#!/bin/sh
usage () {
  echo "deploy.sh [-h|--help] [command] [version]"
  echo "Commands:
  image      build docker image, and push to remote resposity
  scp      build jar or war, and scp to remote server
  compose      use docker-compose to deploy container
  swarm      use docker swarm to deploy service
"
}

# 获得子命令
if [ $# -lt 1 ] ; then
  echo "Miss command"
  exit 1;
fi

# 获得版本
if [ $# -lt 2 ] ; then
  echo "Miss version"
  exit 1;
fi
# 导出环境变量，这样 docker-compose.yml 中可以引用该变量
export tag=$2

# 解析子命令
while [ $# -gt 0 ]; do
  case "$1" in
    -h | --help | -help )
      usage
      exit 0 ;;
    image )
      cmd=deliver-image.sh
      shift ;; # 变量的个数($#)减一
    scp )
      cmd=deploy-scp.sh
      shift ;;
    compose )
      cmd=deploy-compose.sh
      shift ;;
    swarm )
      cmd=deploy-swarm.sh
      shift ;;
    * )
      break ;;
  esac
done

# 执行子命令
if [ -n "$cmd" ]; then
  dir_work=`pwd` # 项目目录
  dir_shell=$(cd "$(dirname "$0")"; pwd) # 脚本目录
  cd $dir_work # 恢复项目目录

  # 执行子命令
  exec $dir_shell/$cmd
fi