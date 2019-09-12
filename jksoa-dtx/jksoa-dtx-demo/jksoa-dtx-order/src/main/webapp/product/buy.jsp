<%@ page language="java" import="net.jkcode.jkmvc.http.HttpRequest,net.jkcode.jksoa.dtx.demo.product.ProductModel" pageEncoding="UTF-8"%>
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
    <% ProductModel pd = (ProductModel) req.getAttribute("pd"); %>
    <form class="panel-body" action="<%= req.absoluteUrl("order/make/" + pd.getId()) %>" method="post">
      <div class="form-group">
        <label for="id">商品编号</label>
        <span><%= pd.getId() %></span>
      </div>
      <div class="form-group">
        <label for="name">商品名</label>
        <span><%= pd.getName() %></span>
      </div>
      <div class="form-group">
        <label for="price">价格</label>
        <span><%= pd.getPrice() %></span>
      </div>
      <div class="form-group">
        <label for="quantity">购买数量</label>
        <input type="text" class="form-control" id="quantity" placeholder="quantity" name="quantity" value="1">
      </div>
      <div class="form-group">
        <label for="couponId">优惠券</label>
        <select name="couponId">
            <%  List<CouponEntity> coupons = (List<CouponEntity>)request.getAttribute("coupons");
              for (Iterator<CouponEntity> it = coupons.iterator(); it.hasNext();) {
               CouponEntity coupon = it.next(); %>
              <option value="<%= coupon.getId() %>"><%= coupon %></option>
          <% } %>
        </select>
      </div>
      <button type="submit" class="btn btn-default">Submit</button>
    </form>

  </div>
  <!-- 最新的 Bootstrap 核心 JavaScript 文件 -->
  <script src="https://cdn.bootcss.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
</body>
</html>