<%--
  Created by IntelliJ IDEA.
  User: Cheung
  Date: 2018/2/6
  Time: 10:05
  To change this template use File | Settings | File Templates.
--%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="common.jsp" %>

<!doctype html>
<html lang="en">
<head>
	<meta charset="UTF-8">
	<meta name="Generator" content="号码归属地查询">
	<meta name="Author" content="Cheung">
	<meta name="Keywords" content="号码归属地查询">
	<meta name="Description" content="https://github.com/550690513">
	<title>号码归属地查询</title>

	<link type="text/css" rel="stylesheet" href="../../static/css/search.css">
	<script type="text/javascript" src="../../static/js/jquery-1.8.0.min.js"></script>
	<script type="text/javascript" src="../../static/js/search.js"></script>

</head>
<body>

<!-- head start -->
<div class="head">
	<div class="center-m9">
		<span>号码归属地查询系统</span>
	</div>
</div>
<!-- head end -->

<!-- content start -->
<div class="content">
	<div class="tools center-m9">
		<form id="searchForm" method="post" enctype="multipart/form-data">
			<table>
				<tbody>
				<tr>
					<td>单个号码查询：</td>
					<td>
						<input id="phoneNum" class="radius" type="text" name="phoneNum" placeholder="请输入手机号码"
							   onkeyup="areaSearch()">
						<img id="phoneNum_icon_ok" class="icon_img" src="../../static/images/icon_ok.png" hidden="hidden">
						<img id="phoneNum_icon_err" class="icon_img" src="../../static/images/icon_err.png" hidden="hidden">
					</td>
				</tr>
				<tr>
					<td>批量号码查询：</td>
					<td>
						<input id="btn" class="btn radius" type="button" name="btn" value="+ 上传文件"
							   onclick="excelImport()">
						<label id="excelName" hidden="hidden"></label>
					</td>
				</tr>
				<tr>
					<td><input id="file" type="file" name="file" style="display: none" onchange="fileUpload()"></td>
				</tr>
				</tbody>
			</table>
		</form>
	</div>

	<div class="detail center-m9">
		<textarea class="radius" readonly>Drag Excel-file here to import</textarea>
	</div>

</div>
<!-- content end -->


</body>
</html>

