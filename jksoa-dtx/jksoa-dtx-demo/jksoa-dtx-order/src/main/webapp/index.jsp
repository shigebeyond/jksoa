<%@ page language="java" import="java.util.*,net.jkcode.jkmvc.common.Formatter,net.jkcode.jkmvc.http.HttpRequest,net.jkcode.jksoa.dtx.demo.order.entity.UserEntity" pageEncoding="UTF-8"%>
<% HttpRequest req = HttpRequest.current(); %>

<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>dtx-demo(tcc)</title>
  <!-- 最新版本的 Bootstrap 核心 CSS 文件 -->
  <link rel="stylesheet" href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
  <!-- 可选的 Bootstrap 主题文件（一般不用引入） -->
  <link rel="stylesheet" href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">
  <style type="text/css">
  </style>
</head>
<body>
    <div class="container">
        <p>本示例用于演示分布式事务, 主要是针对tcc</p>

        <h3>1. <a href="<%= req.absoluteUrl("welcome/initData") %>">初始化数据</a></h3>
        <p>主要是初始化3个库: </p>
        <p>1 dtx_ord -- 订单库, 有商品表/订单表 </p>
        <p>2 dtx_cpn -- 优惠券库, 有优惠券表</p>
        <p>3 dtx_pay -- 支付库, 有支付账号表/支付订单表</p>

        <h3>2. <a href="<%= req.absoluteUrl("user/index") %>">用户列表</a></h3>
        <p>显示用户编号/用户名/拥有的余额/拥有的优惠券, 只有2个用户: 1 买家 2 卖家</p>
        <p><img src="img/user-list.png"  alt="user-list" /></p>

        <h3>3. <a href="<%= req.absoluteUrl("product/index") %>">商品列表</a></h3>
        <p>显示商品名/价格/库存, 可选中某商品来进行购买</p>
        <p><img src="img/product-list.png"  alt="product-list" /></p>

        <h3>4. 购买页面</h3>
        <p>可选择要购买的数量与抵扣的优惠券, 点击"购买"按钮生成待支付的订单</p>
        <p>生成订单的方法, 是一个tcc方法, 需要同时扣库存/冻结优惠券/创建订单</p>
        <p><img src="img/buy.png"  alt="buy" /></p>

        <h3>5. <a href="<%= req.absoluteUrl("order/index") %>">订单列表</a></h3>
        <p>可选中某个待支付的订单进行支付</p>
        <p><img src="img/order-list.png"  alt="order-list" /></p>

        <h3>6. 支付页面</h3>
        <p>显示订单的总金额, 优惠券抵扣的金额, 还有要支付的金额</p>
        <p><img src="img/selectPay.png"  alt="selectPay" /></p>

        <p>6.1 "余额支付"按钮使用余额来支付</p>

        <p>用余额支付订单的方法, 是一个tcc方法, 需要消费优惠券/给买家扣钱/给卖家加钱/更新订单状态为已支付</p>

        <p>6.2 "充值支付(模拟支付成功通知)"按钮模拟支付成功通知</p>

        <p>通知处理是使用mq实现来分布式事务, 因为支付成功的通知是一定要处理, 而且能确定该事务必定能提交, 因此直接使用mq来确保事务执行与提交.</p>
    </div>

    <!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
    <script src="https://cdn.jsdelivr.net/npm/jquery@1.12.4/dist/jquery.min.js"></script>
    <script src="https://cdn.bootcss.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
    <script>
    function goto(url) {
        window.mainIframe.location.href = url;
    }
  </script>
</body>
</html>