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
</head>
<body>
  <div class="panel panel-default">
    <!-- Default panel contents -->
    <div class="panel-heading">用户列表</div>
    <div class="panel-body">
       <!-- Table -->
      <table class="table">
        <thead>
          <tr>
            <th>用户编号</th>
            <th>用户名</th>
            <th>余额</th>
            <th>优惠券</th>
            <th>操作:</th>
          </tr>
        </thead>
        <tbody>
          <%  List<UserEntity> users = (List<UserEntity>)request.getAttribute("users");
              for (Iterator<UserEntity> it = users.iterator(); it.hasNext();) {
               UserEntity user = it.next(); %>
              <tr>
                <th scope="row"><%= user.getUid() %></th>
                <td><%= user.getUname() %></td>
                <td><%= Formatter.formateCents(user.getBalance()) %></td>
                <td><button type="button" class="btn btn-lg btn-danger" data-toggle="popover" title="优惠券明细" data-content="<%= user.getCouponsDesc() %>"><%= user.getCoupons().size() %>张优惠券</button></td>
                <td>

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