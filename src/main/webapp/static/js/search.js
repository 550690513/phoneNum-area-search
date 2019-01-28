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

    $.ajax({
        url: "search/singleSearch.do?phoneNum=" + phoneNum,
        type: "GET",
        async: false,// 同步
        cache: false,// 不缓存
        contentType: "application/x-www-form-urlencoded; charset=utf-8",// 发送信息至服务器时内容编码类型。
        // data: {"phoneNum": phoneNum},// 发送到服务器的数据。将自动转换为请求字符串格式。必须为 Key/Value 格式。
        // processData: false,// 不需要对数据做处理
        dataType: "json",// 预期服务器返回的数据类型。如果不指定，jQuery 将自动根据 HTTP 包 MIME 信息来智能判断。
        success: function (result) {
            icon.ok("phoneNum");
            detail.clear();
            if (result.msg == "success") {
                detail.writeSingle(result.data.area);
            } else {
                alert("查询出错,请稍后重试!");
            }
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

    detail.clear();
    detail.write("正在查询中...");

    $.ajax({
        url: "search/batchSearch.do",
        type: "POST",
        async: true,// 异步
        cache: false,// 不缓存
        contentType: false,// 发送信息至服务器时内容编码类型。
        data: new FormData($('#searchForm')[0]),
        processData: false,// 不需要对数据做处理。
        dataType: "json"// 预期服务器返回的数据类型。如果不指定，jQuery 将自动根据 HTTP 包 MIME 信息来智能判断。
    }).done(function (res) {
        detail.clear();
        $(".detail textarea").attr("text-align", "left");
        $(".detail textarea").append("批量查询完成！\n\n请下载：" + res.resFile);
        /*for (var key in res) {
            $(".detail textarea").append(key + "：" + res[key]).append("\r\n");
        }*/
        // detail.writeBatch(JSON.stringify(res));

        // 下载查询结果文件
        window.open("/upload/" + res.resFile);
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

    writeBatch: function writeBatch(data) {
        var jsonObj = JSON.parse(data);
        for (var key in jsonObj) {
            $(".detail textarea").append(jsonObj[key].toString()).append("\r\n");
        }
    },

    writePhoneNumErr: function writePhoneNumErr() {
        $(".detail textarea").append("手机号长度11位，以13/14/15/16/17/18/19开头\r\n请输入正确的手机号码");
    },

    write: function write(msg) {
        $(".detail textarea").append(msg);
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