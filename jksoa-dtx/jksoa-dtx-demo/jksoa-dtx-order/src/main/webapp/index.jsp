<%@ page language="java" import="java.util.*,net.jkcode.jkmvc.http.HttpRequest,net.jkcode.jksoa.dtx.demo.order.OrderModel" pageEncoding="UTF-8"%>
<% HttpRequest req = HttpRequest.current(); %>

<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8">
<title>入口列表</title>
</head>
<body>
<a href="<%= req.absoluteUrl("welcome/initData") %>" class="btn btn-warn">初始化数据</a>
<br/>
<a href="<%= req.absoluteUrl("user/index") %>" class="btn btn-default">用户列表</a>
<br/>
<a href="<%= req.absoluteUrl("product/index") %>" class="btn btn-primary">商品列表</a>
<br/>
<a href="<%= req.absoluteUrl("order/index") %>" class="btn btn-info">订单列表</a>
<br/>
</body>
</html>