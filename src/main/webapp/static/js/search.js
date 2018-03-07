var phoneNumReg = /^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1}))+\d{8})$/;

/**
 * 单个号码查询
 */
function areaSearch() {
	var phoneNum = $("#phoneNum").val().trim().toString();

	if (phoneNum == null || phoneNum.length <= 0) {
		icon.hidden("phoneNum");
		detail.clear();
		return false;
	}

	if (!phoneNumReg.test(phoneNum)) {
		icon.err("phoneNum");
		detail.clear();
		detail.writePhoneNumErr();
		return false;
	}

	var obj = new Object();
	obj.phoneNum = phoneNum;

	$.ajax({
		url: "search/singleSearch",
		type: "POST",
		async: true,// 异步
		cache: false,
		data: JSON.stringify(obj),
		dataType: "json",
		contentType: "application/json; charset=utf-8",
		processData: false,// 不需要对数据做处理
		success: function (result) {
			icon.ok("phoneNum");
			detail.clear();
			detail.writeSingle(result.data.area);
		},
		error: function (data) {
			icon.hidden("phoneNum");
			alert("查询出错,请稍后重试!");
		}
	})
}

/**
 * 批量号码查询
 */
function excelImport() {
	$("#file").click();
}

/**
 * 文件上传
 */
function fileUpload() {
	$.ajax({
		url: "search/multipleSearch",
		type: "POST",
		async: true,// 异步
		cache: false,
		data: new FormData($('#searchForm')[0]),
		dataType: "json",
		processData: false,// 不需要对数据做处理
		contentType: false
	}).done(function (res) {
		detail.clear();
		$(".detail textarea").attr("text-align", "left");
		for (var key in res) {
			$(".detail textarea").append(key + "：" + res[key]).append("\r\n");
			$(".detail textarea").append("批量查询完成");
		}
		// detail.writeMultiple(JSON.stringify(res));

		// 下载查询结果文件
		window.open("/upload/" + res.resultExcel);
	}).fail(function (res) {
		alert("上传文件失败~")
	})
}

var detail = {

	clear: function clear() {
		$(".detail textarea").text("");
	},

	writeSingle: function writeSingle(data) {
		$(".detail textarea").append(data);
	},

	writeMultiple: function writeMultiple(data) {
		var jsonObj = JSON.parse(data);
		for (var key in jsonObj) {
			$(".detail textarea").append(jsonObj[key].toString()).append("\r\n");
		}
	},

	writePhoneNumErr: function writePhoneNumErr() {
		$(".detail textarea").append("手机号长度11位，以13/14/15/16/17/18/19开头\r\n请输入正确的手机号码");
	}

}

var icon = {
	ok: function ok(i) {
		$("#" + i + "_icon_ok").removeAttr("hidden");
		$("#" + i + "_icon_err").attr("hidden", "hidden");
	},
	err: function err(i) {
		$("#" + i + "_icon_err").removeAttr("hidden");
		$("#" + i + "_icon_ok").attr("hidden", "hidden");
	},
	hidden: function hidden(i) {
		$("#" + i + "_icon_ok").attr("hidden", "hidden");
		$("#" + i + "_icon_err").attr("hidden", "hidden");
	}
}