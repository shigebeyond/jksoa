
# 示例

## 场景
在第三方支付场景中的组合支付，用户在电商网站购物后，要同时使用余额和优惠券支付该笔订单，而余额系统和优惠券系统分别是不同的应用系统，支付系统在调用这两个系统进行支付时，就需要保证余额扣减和优惠券使用要么同时成功，要么同时失败。

## 功能
1. 用户列表, 显示用户编号/用户名/拥有的余额/拥有的优惠券, 只有2个用户: 1 买家 2 卖家

![user-list](https://github.com/shigebeyond/jksoa/blob/master/jksoa-dtx/jksoa-dtx-demo/jksoa-dtx-order/src/main/webapp/img/user-list.png)

2. 商品列表, 显示商品名/价格/库存, 可选中某商品来进行购买

![product-list](https://github.com/shigebeyond/jksoa/blob/master/jksoa-dtx/jksoa-dtx-demo/jksoa-dtx-order/src/main/webapp/img/product-list.png)

3. 购买页面, 可选择要购买的数量与抵扣的优惠券, 点击"购买"按钮生成待支付的订单

生成订单的方法, 是一个tcc方法, 需要同时扣库存/冻结优惠券/创建订单

![buy](https://github.com/shigebeyond/jksoa/blob/master/jksoa-dtx/jksoa-dtx-demo/jksoa-dtx-order/src/main/webapp/img/buy.png)

4. 订单列表, 可选中某个待支付的订单进行支付

![order-list](https://github.com/shigebeyond/jksoa/blob/master/jksoa-dtx/jksoa-dtx-demo/jksoa-dtx-order/src/main/webapp/img/order-list.png)

5. 支付页面, 显示订单的总金额, 优惠券抵扣的金额, 还有要支付的金额

![selectPay](https://github.com/shigebeyond/jksoa/blob/master/jksoa-dtx/jksoa-dtx-demo/jksoa-dtx-order/src/main/webapp/img/selectPay.png)

5.1 "余额支付"按钮使用余额来支付

用余额支付订单的方法, 是一个tcc方法, 需要消费优惠券/给买家扣钱/给卖家加钱/更新订单状态为已支付

5.2 "充值支付(模拟支付成功通知)"按钮模拟支付成功通知

通知处理是使用mq实现来分布式事务, 因为支付成功的通知是一定要处理, 而且能确定该事务必定能提交, 因此直接使用mq来确保事务执行与提交.

## 参与者
1. 1个本地服务: 

OrderService, 订单服务, 用于生成订单与支付订单

2. 2个rpc服务:

2.1 ICouponService, 优惠券服务, 用于冻结优惠券/消费优惠券

2.2 IPayAccountService, 支付账号服务, 用于消费用户余额

3. 4个数据库

详见 dataSources.yaml / db.yaml

```
test -- tcc事务库, 有 tcc_transaction 表
dtx_ord -- 订单库, 有商品表/订单表
dtx_cpn -- 优惠券库, 有优惠券表
dtx_pay -- 支付库, 有支付账号表/支付订单表
```

## 生成订单的流程
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

## 支付订单的流程
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

## 运行

1. 启动优惠券服务

在工程 `jksoa-dtx/jksoa-dtx-demo/jksoa-dtx-coupon` 启动主类 `RpcServerLauncher` 

2. 启动优惠券服务

在工程 `jksoa-dtx/jksoa-dtx-demo/jksoa-dtx-pay` 启动主类 `RpcServerLauncher`

3. 启动订单web系统
在工程 `jksoa-dtx/jksoa-dtx-demo/jksoa-dtx-order` 启动主类 `JettyServerLauncher`

你也可以通过gradle命令行启动:

`gradle :jksoa-dtx:jksoa-dtx-demo:jksoa-dtx-order:appRun -x test`
