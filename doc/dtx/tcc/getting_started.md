
# 概述

TCC服务是由try/confirm/cancel3个模块组成, 属于两阶段提交, try阶段执行成功则执行confirm, 否则执行cancel

tcc3模块介绍

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

一个tcc事务由多个参与者来参与完成, 每个参与者可依赖于db的本地事务来保证参与者处理的一致性
关于参与者, 在 jksoa-dtx-tcc 框架中, 一个参与者就是 `@TccMethod` 注解的方法的一次调用
一个 `@TccMethod` 注解的方法里面, 可能调用了其他  `@TccMethod` 注解的方法, 这些方法调用则作用当前tcc事务的参与者
在try阶段, 会记录这些调用过的参与者
在confirm/cancel阶段, 会调用之前记录的参与者对应的confirm/cancel方法, 以便达到最终一致性



故障恢复机制

一个TCC事务框架，需要保证全局事务完整的提交/回滚的。
它需要能够协调多个参与者及其分支事务，保证它们按全局事务的完成方向各自完成自己的分支事务, 从而达到全局的最终一致性.

但是现实场景下, 经常会发生故障，如服务器宕机、重启、网络故障等, 这些故障会导致事务中断, 从而导致部分分支事务完成提交/回滚, 而其他没有完成, 进而导致全局事务的不一致.

而 jksoa-dtx-tcc  的故障恢复功能, 是针对confirm/cancel阶段的参与者及其分支事务提交/回滚失败的场景, 直接失败重试, 直到超过重试次数, 或重试成功从而导致最终一致性


rpc服务仅需要暴露try方法
TCC服务与普通的服务一样，只需要暴露一个接口，也就是它的Try业务。
Confirm/Cancel业务逻辑，只是因为全局事务提交/回滚的需要才提供的，因此Confirm/Cancel业务只需要被TCC事务框架发现即可，不需要被调用它的其他业务服务所感知。
换句话说，业务系统的其他服务在需要调用TCC服务时，根本不需要知道它是否为TCC型服务。



因为，TCC服务能被其他业务服务调用的也仅仅是其Try业务，Confirm/Cancel业务是不能被其他业务服务直接调用的

### Confirm/Cancel方法的幂等性

在TCC事务模型中，Confirm/Cancel业务可能会被重复调用, 此时需要业务端来实现Confirm/Cancel方法的幂等性

示例说明:

jksoa-dtx-tcc 使用了 jksoa-rpc 作为底层的rpc框架

示例功能
1. 用户列表, 显示用户编号/用户名/拥有的余额/拥有的优惠券, 只有2个用户: 1 买家 2 卖家
2. 商品列表, 显示商品名/价格/库存, 可选中某商品来进行购买
3. 购买页面, 可选择要购买的数量与抵扣的优惠券, 点击"购买"按钮生成待支付的订单
生成订单的方法, 是一个tcc方法, 需要同时扣库存/冻结优惠券/创建订单
4. 订单列表, 可选中某个待支付的订单进行支付
5. 支付页面, 显示订单的总金额, 优惠券抵扣的金额, 还有要支付的金额, 点击"余额支付"按钮使用余额来支付
用余额支付订单的方法, 是一个tcc方法, 需要消费优惠券/给买家扣钱/给卖家加钱/更新订单状态为已支付

参与者
1. 1个本地服务: 
OrderService, 订单服务, 用于生成订单与支付订单
2. 2个rpc服务:
2.1 ICouponService, 优惠券服务, 用于冻结优惠券/消费优惠券
2.2 IPayAccountService, 支付账号服务, 用于消费用户余额

生成订单的流程
1. try阶段
1.1 冻结优惠券, 即设置该优惠券为冻结状态
1.2 扣库存
1.3 创建订单, 订单状态为草稿
以上几步均为参与者, 任何一步出错, 都会将自动调用参与者对应的cancel方法, 否则调用参与者的confirm方法

2. confirm阶段
2.2 设置订单状态为待支付

3. cancel阶段
3.1 解除冻结优惠券, 即设置该优惠券为未消费状态
3.2 加库存
3.3 删除订单

支付订单的流程
1. try阶段
1.1 更新订单状态为支付中
1.2 优惠券不做处理
1.3 消费余额, 即买家扣钱
以上几步均为参与者, 任何一步出错, 都会将自动调用参与者对应的cancel方法, 否则调用参与者的confirm方法

2. confirm阶段
2.1 更新订单状态为已支付
2.2 消费优惠券, 即设置该优惠券为已消费状态
2.3 卖家加钱

3. cancel阶段
3.1 更新订单状态为支付失败
3.2 释放优惠券, 即设置该优惠券为未消费状态
2.3 买家加钱


# 特性
1. 简单, 易用, 轻量, 易扩展；
2. 基于rpc实现, 拥有rpc的一切特性: 多序列化/均衡负载/注册中心/快速扩容/服务高可用/失败转移...；
3. 高性能存储：基于lsm-tree存储, 同时是异步批量刷盘, 从而达到高性能写.
4. 消息消费支持串行与并行;
5. 延时消息: 只用于失败重试;
6. 消费支持失败重试: 如果consumer消费消息失败, 反馈给broker后broker会新增延迟消息, 以便延迟重发给consumer重新执行;

## 背景

# 快速入门

## 发布Tcc服务

发布一个Tcc服务方法，可被远程调用并参与到Tcc事务中，发布Tcc服务方法有下面3个约束：

1. 在服务方法上加上`@TccMethod`注解
2. 服务方法的参数都须能序列化(实现`Serializable`接口)
jksoa-dtx-tcc 在执行服务过程中会将Tcc服务的上下文持久化，包括所有参数，内部默认实现为将入参使用fast serializer的序列化机制序列化为为byte流，所以需要实现`Serializable`接口.
3. try方法、confirm方法和cancel方法入参类型须一样


## 注解 @TccMethod

关于注解 `@TccMethod` 的描述直接看源码:

```
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class TccMethod(
        public val confirmMethod: String = "", // 确认方法, 如果为空字符串, 则使用原方法
        public val cancelMethod: String = "", // 取消方法, 如果为空字符串, 则使用原方法
        public val bizType: String = "", // 业务类型, 如果为空则取 Application.name
        public val bizIdParamField: String = "", // 业务主体编号所在的参数字段表达式, 如 0.name, 表示取第0个参数的name字段值作为业务主体编号
        public val cancelOnResultFutureException: Boolean = false // 如果方法的返回类型是 CompletableFuture 且 CompletableFuture 完成时发生异常, 控制是否回滚, 仅对根事务有效
)
```

识别tcc的3个方法:
1. try阶段方法为`@TccMethod`注解的方法
2. confirm阶段方法为`@TccMethod`中属性`confirmMethod`指定的方法
3. cancel阶段方法为`@TccMethod`中属性`cancelMethod`指定的方法


本地Tcc服务 + 调用远程Tcc服务
调用远程Tcc服务，将远程Tcc服务参与到本地Tcc事务中，本地的服务方法也需要声明为Tcc服务
不同的是远程Tcc服务中的`confirmMethod`属性与`cancelMethod`属性为空字符串

在demo中优惠券服务中的发布服务


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

### tcc事务配置
dtx-tcc.yaml

```
# tcc事务配置
dbName: default
rertySeconds: 20 # 重试的时间间隔, 单位秒
maxRetryCount: 3 # 最大的重试次数
```

说明:
1. `dbName`: tcc事务存储的数据库名: 引用的是 `dataSources.yaml` 中配置的数据库名
2. `rertySeconds`: 重试的时间间隔: 当Tcc事务异常后，恢复Job将会定期恢复事务, 根据配置项`rertySeconds`的时间间隔来定时重试
3. `maxRetryCount`: 最大的重试次数: 重试次数超过`maxRetryCount`停止重试

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


