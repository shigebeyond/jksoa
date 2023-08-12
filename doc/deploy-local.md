# 本地部署
假定你使用的构建工具是gradle
## gradle配置
```groovy
dependencies{
    // 填写依赖的包
}

// 复制jar
task copyLib(type: Copy) {
    into "${buildDir}/app/libs"
    from configurations.runtime // 所有依赖的jar
    from "${buildDir}/libs/${project.name}-${project.version}.jar"
}

// 复制start.sh
task copyStartSh(type: Copy) {
    into "${buildDir}/app"
    from "start-rpcserver.sh"
}

// 复制配置文件
task copyConf(type: Copy) {
    into "${buildDir}/app/conf"
    // 需要的配置文件
    def props = ["rpc-server.yaml", "logback.xml", "kafka-consumer.yaml", "kafka-producer.yaml"]
    // 项目 resources 目录, 来收集配置文件
    for(src in project.sourceSets.main.resources){
        def match = props.any{ p -> src.name.endsWith(p) }
        if(match){
            //println(src)
            from src
        }
    }
}

if (project.hasProperty("all")){
    build.finalizedBy(copyLib)
    build.dependsOn(copyStartSh)
    build.dependsOn(copyConf)
}
```

## 构建
1. 如果仅仅构建的话:
```sh
gradle build -x test -Pall
```

2. 如果有单元测试的话:
直接使用
```sh
gradle build -Pall
```

构建好的文件放在 `build/app`，目录结构如下
```
build/app/
├── conf 配置文件
├── libs 依赖的jar
├── logs 日志
└── start-rpcserver.sh 启动脚本
```

## 部署与启动
直接将目录 `build/app` 上传到服务器上，并执行`start-rpcserver.sh` 来启动 
