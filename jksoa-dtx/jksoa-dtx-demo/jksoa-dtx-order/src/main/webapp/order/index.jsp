<%@ page language="java" import="java.util.*,net.jkcode.jkmvc.common.Formatter,net.jkcode.jkmvc.http.HttpRequest,net.jkcode.jksoa.dtx.demo.order.OrderModel" pageEncoding="UTF-8"%>
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
</head>
<body>
  <div class="panel panel-default">
    <!-- Default panel contents -->
    <div class="panel-heading">订单列表</div>
    <div class="panel-body">
       <!-- Table -->
      <table class="table">
        <thead>
          <tr>
            <th>订单编号</th>
            <th>卖家编号</th>
            <th>卖家名</th>
            <th>优惠券编号</th>
            <th>优惠券抵扣金额</th>
            <th>要支付的金额</th>
            <th>总金额</th>
            <th>状态</th>
            <th>创建时间</th>
            <th>支付时间</th>
            <th>操作:</th>
          </tr>
        </thead>
        <tbody>
          <%  List<OrderModel> orders = (List<OrderModel>)request.getAttribute("orders");
              for (Iterator<OrderModel> it = orders.iterator(); it.hasNext();) {
               OrderModel order = it.next(); %>
              <tr>
                <th scope="row"><%= order.getId() %></th>
                <td><%= order.getSellerUid() %></td>
                <td><%= order.getSellerUname() %></td>
                <td><%= order.getCouponId() %></td>
                <td><%= Formatter.formateCents(order.getCouponMoney()) %></td>
                <td><%= Formatter.formateCents(order.getPayMoney()) %></td>
                <td><%= Formatter.formateCents(order.getTotalMoney()) %></td>
                <td><%= order.getStatusDesc() %></td>
                <td><%= Formatter.formateTimestamp(order.getCreated()) %></td>
                <td><%= Formatter.formateTimestamp(order.getPayTime()) %></td>
                <td>
                    <button type="button" class="btn btn-lg btn-danger" data-toggle="popover" data-placement="left" title="商品明细" data-content="<%= order.getItemsDesc() %>">商品明细</button>
                    <% if(order.getStatus() == 1){ %>
                    <a href="<%= req.absoluteUrl("order/selectPay/" + order.getId()) %>" class="btn btn-default">支付</a>
                    <% } %>
                 </td>
              </tr>
           <% } %>
        </tbody>
      </table>
    </div>
  </div>
  <!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
  <script src="https://cdn.jsdelivr.net/npm/jquery@1.12.4/dist/jquery.min.js"></script>
  <script src="https://cdn.bootcss.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
  <script>
  $(function () {
    $('[data-toggle="popover"]').popover({'html':true});
  })
  </script>
</body>
</html>