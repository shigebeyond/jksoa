<%@ page language="java" import="net.jkcode.jkmvc.common.Formatter,net.jkcode.jkmvc.http.HttpRequest,net.jkcode.jksoa.dtx.demo.order.OrderModel" pageEncoding="UTF-8"%>
<% HttpRequest req = HttpRequest.current(); %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>todolist</title>
  <!-- 最新版本的 Bootstrap 核心 CSS 文件 -->
  <link rel="stylesheet" href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
  <!-- 可选的 Bootstrap 主题文件（一般不用引入） -->
  <link rel="stylesheet" href="https://cdn.bootcss.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">
</head>
<body>
  <div class="panel panel-default">
    <!-- Default panel contents -->
    <div class="panel-heading">购买商品</div>

    <!-- Form -->
    <%
    OrderModel order = (OrderModel) req.getAttribute("order");
    int balance = (int) req.getAttribute("balance");
    %>
    <form class="panel-body" action="<%= req.absoluteUrl("order/balancePay/" + order.getId()) %>" method="post">
      <div class="form-group">
        <label for="">总金额</label>
        <span><%= Formatter.formateCents(order.getTotalMoney()) %></span>
      </div>
      <div class="form-group">
        <label for="">优惠券编号</label>
        <span><%= order.getCouponId() %></span>
      </div>
      <div class="form-group">
        <label for="">优惠券抵扣金额</label>
        <span><%= Formatter.formateCents(order.getCouponMoney()) %></span>
      </div>
      <div class="form-group">
        <label for="">要支付的金额</label>
        <span><%= Formatter.formateCents(order.getPayMoney()) %></span>
      </div>
      <div class="form-group">
        <label for="">我的余额</label>
        <span><%= Formatter.formateCents(balance) %></span>
      </div>
      <button type="submit" class="btn btn-default">余额支付</button>
      <a href="<%= req.absoluteUrl("order/rechargePayNotify/" + order.getId()) %>" class="btn btn-primary">充值支付(模拟支付成功通知)</a>
    </form>

  </div>
  <!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
  <script src="https://cdn.bootcss.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
</body>
</html>