
# 概述

TCC服务是由try/confirm/cancel3个模块组成, 属于两阶段提交, try阶段执行成功则执行confirm, 否则执行cancel

## 特性
1. 简单, 易用, 轻量, 易扩展；
2. 一个注解即可声明tcc服务, 基于apectj织入tcc事务逻辑, 对开发者透明, 方便易用;
3. 不同于其他市面上的开源实现, 我做到了真正的异步非阻塞, 基于jksoa-rpc框架来实现参与者的异步非阻塞的rpc, 可应用在tcc事务的try/confirm/cancel阶段, 同时根据异步结果来确定tcc事务的提交或回滚;
4. 使用`ScopedTransferableThreadLocal`/`AllRequestScopedTransferableThreadLocal`有作用域的可传递的 ThreadLocal 版本, 来实现同一个tcc事务中发起方与异步rpc参与方之间的事务上下文的传递;
5. 故障恢复, 目前仅限于confirm/cancel阶段, 失败重试.

## 背景

随着网站的访问量与数据量的增加, 我们不可避免的引入数据库拆分和服务拆分, 从而引出了新的分布式事务的问题:
`如何在跨数据库、跨服务的环境下, 保证数据操作的一致性?`

跨应用的业务操作原子性要求，其实是比较常见的。比如在第三方支付场景中的组合支付，用户在电商网站购物后，要同时使用余额和优惠券支付该笔订单，而余额系统和优惠券系统分别是不同的应用系统，支付系统在调用这两个系统进行支付时，就需要保证余额扣减和优惠券使用要么同时成功，要么同时失败。

常规的XA事务(2PC，2 Phase Commit, 两阶段提交), 在数据库层实现的两阶段提交, 有中断的风险, 同时性能上也不尽如人意.

而tcc是在服务层实现的两阶段提交来, 再加上它的故障恢复机制, 从而实现跨服务的事务一致性.

## tcc3模块介绍

1. try: 尝试执行业务

完成所有业务检查（一致性）

预留必须业务资源（准隔离性）

2. confirm: 确认执行业务

真正执行业务

不作任何业务检查

只使用try阶段预留的业务资源

confirm操作满足幂等性

3. cancel: 取消执行业务

释放try阶段预留的业务资源

cancel操作满足幂等性

## tcc的参与者
一个tcc事务由多个参与者来参与完成, 每个参与者可依赖于db的本地事务来保证参与者处理的一致性

关于参与者, 在 jksoa-dtx-tcc 框架中, 一个参与者就是 `@TccMethod` 注解的方法的一次调用

在一个 `@TccMethod` 注解的方法里面, 可能调用了其他  `@TccMethod` 注解的方法, 这些方法调用则作用当前tcc事务的参与者

在try阶段, 会记录这些调用过的参与者

在confirm/cancel阶段, 会调用之前记录的参与者对应的confirm/cancel方法, 以便达到最终一致性

## 故障恢复机制

一个TCC事务框架，需要保证全局事务完整的提交/回滚的。

它需要能够协调多个参与者及其分支事务，保证它们按全局事务的完成方向各自完成自己的分支事务, 从而达到全局的最终一致性.

但是现实场景下, 经常会发生故障，如服务器宕机、重启、网络故障等, 这些故障会导致事务中断, 从而导致部分分支事务完成提交/回滚, 而其他没有完成, 进而导致全局事务的不一致.

而 jksoa-dtx-tcc  的故障恢复功能, 是针对confirm/cancel阶段的参与者及其分支事务提交/回滚失败的场景, 直接失败重试, 直到超过重试次数, 或重试成功从而导致最终一致性

## rpc服务仅需要暴露try方法
TCC服务与普通的服务一样，只需要暴露一个接口，也就是它的try方法。

至于confirm/cancel方法, 你也可以暴露, 但是不建议这么干.

虽然接口只暴露了try方法, 但是server端却是真正有confirm/cancel方法, 但这些方法不需要暴露给client, client只需要调用try方法即可, 框架在server端根据事务状态自行调用server端的confirm/cancel方法, 不需要client端手动调用, 因此对client 端是透明的

## Confirm/Cancel方法的幂等性

在TCC事务模型中，confirm/cancel业务可能会被重复调用, 此时需要业务端来实现confirm/cancel方法的幂等性

# 快速入门

可参考demo子工程 jksoa-dtx/jksoa-dtx-demo 与 [demo说明](demo.md)

## 发布Tcc服务

发布一个Tcc服务方法，可被远程调用并参与到Tcc事务中，发布Tcc服务方法有下面3个约束：

1. 在服务方法上加上`@TccMethod`注解
2. 服务方法的参数都须能序列化(实现`Serializable`接口)
jksoa-dtx-tcc 在执行服务过程中会将Tcc服务的上下文持久化，包括所有参数，内部默认实现为将入参使用fast serializer的序列化机制序列化为为byte流，所以需要实现`Serializable`接口.
3. try方法、confirm方法和cancel方法入参类型须一样


### 方法级注解 @TccMethod

关于注解 `@TccMethod` 的描述直接看源码:

```
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TccMethod(
        public val confirmMethod: String = "", // 确认方法, 如果为空字符串, 则使用原方法
        public val cancelMethod: String = "", // 取消方法, 如果为空字符串, 则使用原方法
        public val bizType: String = "", // 业务类型, 如果为空则取 JkApp.name
        public val bizIdParamField: String = "" // 业务主体编号所在的参数字段表达式, 如 0.name, 表示取第0个参数的name字段值作为业务主体编号
)
```

识别tcc的3个方法:
1. try阶段方法为`@TccMethod`注解的方法
2. confirm阶段方法为`@TccMethod`中属性`confirmMethod`指定的方法
3. cancel阶段方法为`@TccMethod`中属性`cancelMethod`指定的方法

### 本地Tcc服务 vs 远程Tcc服务
如果在本地tcc服务中, 调用远程Tcc服务，则 jksoa-dtx-tcc 框架会将远程Tcc服务作为参与者, 加入到到本地Tcc事务中

不同的是远程Tcc服务方法声明的`confirmMethod`属性与`cancelMethod`属性为空字符串

### 在demo中优惠券服务中的发布服务

1. 远程服务

```
    /**
     * 消费优惠券 -- try
     * @param uid 用户编号
     * @param id 优惠券编号
     * @param bizOrderId 业务订单编号
     * @return
     */
    @TccMethod("", "", "coupon.spendCoupon", "2")
    fun spendCoupon(uid: Int, id: Int, bizOrderId: Long): CompletableFuture<Boolean>
```

2. 本地服务

```
    /**
     * 消费优惠券 -- try
     *   只是检查状态是否冻结
     *
     * @param uid 用户编号
     * @param id 优惠券编号
     * @param bizOrderId 业务订单编号
     * @return
     */
    @TccMethod("confirmSpendCoupon", "cancelSpendCoupon", "coupon.spendCoupon", "2")
    override fun spendCoupon(uid: Int, id: Int, bizOrderId: Long): CompletableFuture<Boolean> {
        ...
    }

    /**
     * 消费优惠券 -- confirm
     *    修改状态为已消费
     *
     * @param uid 用户编号
     * @param id 优惠券编号
     * @param bizOrderId 业务订单编号
     * @return
     */
    public fun confirmSpendCoupon(uid: Int, id: Int, bizOrderId: Long): CompletableFuture<Boolean> {
        ...
    }

    /**
     * 消费优惠券 -- cancel
     *    修改状态为未消费
     *
     * @param uid 用户编号
     * @param id 优惠券编号
     * @param bizOrderId 业务订单编号
     * @return
     */
    public fun cancelSpendCoupon(uid: Int, id: Int, bizOrderId: Long): CompletableFuture<Boolean> {
      ...
    }
```

## 配置

### tcc事务配置 tx-tcc.yaml

```
# tcc事务配置
dbName: default
retrySeconds: 20 # 重试的时间间隔, 单位秒
maxRetryCount: 3 # 最大的重试次数
recoverTimerSeconds: !!java.lang.Long 60 # 定时恢复(重试)的时间间隔, 为0则不启动定时恢复, 你可以在其他应用中启动
```

说明:
1. `dbName`: tcc事务存储的数据库名: 引用的是 `dataSources.yaml` 中配置的数据库名

2. `retrySeconds`: 重试的时间间隔: 当Tcc事务异常后，恢复Job将会定期恢复事务, 根据配置项`rertySeconds`的时间间隔来定时重试

3. `maxRetryCount`: 最大的重试次数: 重试次数超过`maxRetryCount`停止重试

4. `recoverTimerSeconds`: 定时恢复(重试)的时间间隔, 为0则不启动定时恢复, 你可以在其他应用中启动, 启动`net.jkcode.jksoa.dtx.tcc.TccTransactionRecovery` 主类即可

### rpc拦截器配置

rpc-client.yaml

```
interceptors: # 拦截器
    - net.jkcode.jksoa.dtx.tcc.interceptor.RpcClientTccInterceptor # 添加tcc事务参与者+传递tcc事务信息
```

rpc-server.yaml
```
interceptors: # 拦截器
    - net.jkcode.jksoa.dtx.tcc.interceptor.RpcServerTccInterceptor # 接收tcc事务信息
```

### aspectj配置

META-INF/aop.xml

```
<aspectj>
    <!-- weave in just this aspect -->
    <aspects>
        <aspect name="net.jkcode.jksoa.dtx.tcc.TccMethodAspect"/>
    </aspects>

    <!-- only weave classes in our application-specific packages -->
    <!--<weaver>-->
    <weaver options="-verbose -showWeaveInfo">
        <include within="net.jkcode.jksoa.dtx.demo..*"/>
    </weaver>

</aspectj>
```

## 运行

启动java程序时, 需要添加vm参数, 来应用aspectj的javaagent

```
-javaagent:/home/shi/.gradle/caches/modules-2/files-2.1/org.aspectj/aspectjweaver/1.8.12/87be4d5a1c68004a247a62c011fa63da786965fb/aspectjweaver-1.8.12.jar
```
