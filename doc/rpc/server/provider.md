
# Provider -- 服务提供者

`Provider`主要是对外暴露服务, 以供别人调用.

暴露服务主要工作是： 扫描加载Provider的服务

# 扫描加载Provider的服务

## 入口
由server启动流程, 我们可知: 在启动server后, 会调用 `ProviderLoader.load()` 来扫描加载Provider服务

## ProviderLoader.load()
他的实现是扫描指定的包, 找到满足以下3个条件的服务类:
1. 类名以`Service`为后缀
2. 普通类, 非接口, 非抽象类
3. 其实现的接口必须声明注解 `@RemoteService`

## 扫描包的配置
在`rpc-server.yaml` 中的属性 `servicePackages`, 例子如下
```
servicePackages: # service类所在的包路径
    - net.jkcode.jksoa.rpc.example # 示例服务
```