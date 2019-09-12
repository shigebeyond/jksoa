<%@ page language="java" import="java.util.*,net.jkcode.jkmvc.http.HttpRequest,net.jkcode.jksoa.dtx.demo.product.ProductModel" pageEncoding="UTF-8"%>
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
    <div class="panel-heading">商品列表</div>
    <div class="panel-body">
       <!-- Table -->
      <table class="table">
        <thead>
          <tr>
            <th>商品编号</th>
            <th>商品名</th>
            <th>卖家编号</th>
            <th>卖家名</th>
            <th>价格</th>
            <th>总库存</th>
            <th>剩余库存</th>
            <th>操作:</th>
          </tr>
        </thead>
        <tbody>
          <%  List<ProductModel> pds = (List<ProductModel>)request.getAttribute("pds");
              for (Iterator<ProductModel> it = pds.iterator(); it.hasNext();) {
               ProductModel pd = it.next(); %>
              <tr>
                <th scope="row"><%= pd.getId() %></th>
                <td><%= pd.getName() %></td>
                <td><%= pd.getSellerUid() %></td>
                <td><%= pd.getSellerUname() %></td>
                <td><%= pd.getName() %></td>
                <td><%= pd.getPrice() %></td>
                <td><%= pd.getQuantity() %></td>
                <td><%= pd.getRemainQuantity() %></td>
                <td>
                    <a href="<%= req.absoluteUrl("product/buy/" + pd.getId()) %>" class="btn btn-default">购买</a>
                 </td>
              </tr>
           <% } %>
        </tbody>
      </table>
    </div>
  </div>
  <!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
  <script src="https://cdn.bootcss.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
</body>
</html>