<%@ page language="java" import="java.util.*,net.jkcode.jkmvc.common.Formatter,net.jkcode.jkmvc.http.HttpRequest,net.jkcode.jksoa.dtx.demo.order.entity.UserEntity" pageEncoding="UTF-8"%>
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
    <header class="navbar navbar-static-top bs-docs-nav" id="top">
        <div class="container">
            <div class="navbar-header">
            <button class="navbar-toggle collapsed" type="button" data-toggle="collapse" data-target="#bs-navbar" aria-controls="bs-navbar" aria-expanded="false">
                <span class="sr-only">Toggle navigation</span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <a href="/" class="navbar-brand">dtx-demo</a>
            </div>
            <nav id="bs-navbar" class="collapse navbar-collapse">
                <ul class="nav navbar-nav">
                    <li>
                        <a href="#" onclick="goto('<%= req.absoluteUrl("welcome/initData") %>');">初始化数据</a>
                    </li>
                    <li>
                        <a href="#" onclick="goto('<%= req.absoluteUrl("user/index") %>');" class="btn btn-default">用户列表</a>
                    </li>
                    <li>
                        <a href="#" onclick="goto('<%= req.absoluteUrl("product/index") %>');" class="btn btn-primary">商品列表</a>
                    </li>
                    <li>
                        <a href="#" onclick="goto('<%= req.absoluteUrl("order/index") %>');" class="btn btn-info">订单列表</a>
                    </li>
                </ul>
            </nav>
        </div>
    </header>

    <iframe id="mainIframe" name="mainIframe" src="/main.html" frameborder="0" scrolling="auto" ></iframe>

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