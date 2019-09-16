
# 概述


gradle :jksoa-dtx:jksoa-dtx-demo:jksoa-dtx-order:appRun -x test


## 配置

2. META-INF/aop.xml

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

3. 数据库配置 -- dataSources.yaml

```
# 默认库, 放tcc_transaction表
default:
  # 主库
  master:
    driverClass: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1/test?useUnicode=true&characterEncoding=utf-8
    username: root
    password: root
  # 多个从库, 可省略
  slaves:
# 订单库, 放商品表与业务订单表
dtx_ord:
  # 主库
  master:
    driverClass: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1/dtx_ord?useUnicode=true&characterEncoding=utf-8
    username: root
    password: root
  # 多个从库, 可省略
  slaves:
# 优惠券库, 放优惠券表
dtx_cpn:
  # 主库
  master:
    driverClass: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1/dtx_cpn?useUnicode=true&characterEncoding=utf-8
    username: root
    password: root
  # 多个从库, 可省略
  slaves:
# 支付库, 放支付账号与支付订单表
dtx_pay:
  # 主库
  master:
    driverClass: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1/dtx_pay?useUnicode=true&characterEncoding=utf-8
    username: root
    password: root
  # 多个从库, 可省略
  slaves:
```

4. db配置 -- db.yaml

```
columnUnderlineDbs: default,dtx_ord,dtx_cpn,dtx_pay
```

5. rpc拦截器配置
rpc-client.yaml
```
interceptors: # 拦截器
    - net.jkcode.jksoa.dtx.tcc.interceptor.RpcClientTccInterceptor
```

rpc-server.yaml
```
interceptors: # 拦截器
    - net.jkcode.jksoa.dtx.tcc.interceptor.RpcServerTccInterceptor
```

6. dtx-tcc.yaml

```
# tcc事务配置
dbName: default
rertySeconds: 20 # 重试的时间间隔, 单位秒
maxRetryCount: 3 # 最大的重试次数
```

当Tcc事务异常后，恢复Job将会定期恢复事务, 根据配置项`rertySeconds`的时间间隔来定时重试, 重试次数超过`maxRetryCount`停止重试

# 运行
添加jvm参数
```
-javaagent:/home/shi/.gradle/caches/modules-2/files-2.1/org.aspectj/aspectjweaver/1.8.12/87be4d5a1c68004a247a62c011fa63da786965fb/aspectjweaver-1.8.12.jar
```

Try: 尝试执行业务
    完成所有业务检查（一致性）
    预留必须业务资源（准隔离性）

Confirm: 确认执行业务
    真正执行业务
    不作任何业务检查
    只使用Try阶段预留的业务资源
    Confirm操作满足幂等性

Cancel: 取消执行业务
    释放Try阶段预留的业务资源
    Cancel操作满足幂等性


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

有2个rpc服务作为参与者
1. ICouponService, 优惠券服务, 用于冻结优惠券/消费优惠券
2. IPayAccountService, 支付账号服务, 用于消费用户余额

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

jksoa-dtx-tcc 通过aspectj来对有`@TccMethod`注解的方法为切点, 对该方法实现织入tcc事务的逻辑.

```
package net.jkcode.jksoa.dtx.tcc

import net.jkcode.jksoa.dtx.tcc.invocation.JoinPointInvocation
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut

/**
 * tcc方法的切入处理
 *
 * @author shijianhang<772910474@qq.com>
 * @date 2019-9-8 6:04 PM
 */
@Aspect
class TccMethodAspect {

    /**
     * 切入点: 有@Compensable注解的方法
     */
    @Pointcut("@annotation(net.jkcode.jksoa.dtx.tcc.TccMethod) && execution(* *(..))")
    public fun tccMethodExecute() {
    }

    /**
     * 切入逻辑
     */
    @Around("tccMethodExecute()")
    public fun interceptTccMethod(pjp: ProceedingJoinPoint): Any? {
        val inv = JoinPointInvocation(pjp)
        return TccTransactionManager.current().interceptTccMethod(inv)
    }
}
```

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


