package com.cheung.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cheung.utils.ExcelUtil;
import com.cheung.utils.HttpRequestUtil;
import com.cheung.utils.ParseUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Cheung
 * @version 1.0.0
 * @date 2018/2/6
 */
@Controller
@RequestMapping(value = "search")
// @RequestMapping(value = "page/search")
public class SearchController {

	@Value("${urlIP138}")
	private String URL_IP138;
	@Value("${urlTaoBao}")
	private String URL_TAOBAO;

	public static void main(String[] args) {
		// String url = "C:/Users/Administrator/Desktop/批量查询模板.xlsx";
		String url = "C:/Users/Administrator/Desktop/新增用户.xls";
		try {
			ExcelUtil.getAreaByPhoneNum_multiple(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 单个号码查询
	 *
	 * @param paramJson
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/singleSearch", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	@ResponseBody
	public String singleSearch(@RequestBody String paramJson, HttpServletRequest request, HttpServletResponse response) {

		JSONObject parseObj = JSON.parseObject(paramJson);
		String phoneNum = parseObj.getString("phoneNum");

		JSONObject resultObj = new JSONObject();
		JSONObject dataObj = new JSONObject();
		try {
			request.setCharacterEncoding("UTF-8");
			response.setContentType("text/html; charset=UTF-8");

			/**
			 * 方式一:请求ip138接口获取数据(通过解析html获取)
			 */
			String url = String.format(URL_IP138, phoneNum);
			Document doc = Jsoup.connect(url).get();
			Elements els = doc.getElementsByClass("tdc2");
			String area = els.get(1).text();

			/**
			 * 方式二:请求淘宝api获取数据(通过解析json获取)
			 */
			/*String str = HttpRequestUtil.sendGet(URL_TAOBAO, "tel=" + phoneNum);
			JSONObject obj = ParseUtil.parseTBStr(str);
			String area = obj.getString("carrier");*/

			dataObj.put("phoneNum", phoneNum);
			dataObj.put("area", area);
			resultObj.put("data", dataObj);
			resultObj.put("code", "200");
			resultObj.put("msg", "success");
		} catch (Exception e) {
			resultObj.put("msg", "fail");
			e.printStackTrace();
		}
		return resultObj.toJSONString();
	}


	/**
	 * 批量号码查询
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/multipleSearch", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	@ResponseBody
	public String multipleSearch(HttpServletRequest request, HttpServletResponse response) {

		HashMap<String, Object> map = new LinkedHashMap<String, Object>(7);
		try {
			request.setCharacterEncoding("utf-8");
			response.setContentType("text/html;charset=utf-8");

			// 设置文件上传路径
			String realPath = request.getSession().getServletContext().getRealPath(File.separator);
			String dirPath = realPath + "upload";
			File dirFile = new File(dirPath);
			// 判断文件夹是否存在
			if (!dirFile.exists()) {
				// 如果没有就创建
				dirFile.mkdirs();
			}

			// 定义一个文件上传工厂
			FileItemFactory factory = new DiskFileItemFactory();
			// 负责专门处理上传文件的数据
			ServletFileUpload upload = new ServletFileUpload(factory);
			List<FileItem> itemList = upload.parseRequest(request);
			if (null != itemList) {
				Iterator<FileItem> iterator = itemList.iterator();
				while (iterator.hasNext()) {
					FileItem fileItem = iterator.next();
					// 判断文件上传的形式
					if (fileItem.isFormField()) {
						// 普通表单域提交
						continue;
					} else {
						// 文件上传表单域提交
						/**
						 * 解决文件名可能重复,造成文件覆盖的问题
						 */
						String orignialName = fileItem.getName();// 原文件名
						System.out.println("接收到文件: " + orignialName);
						String suffix = orignialName.substring(orignialName.lastIndexOf("."), orignialName.length());// 后缀名
						// 保存的文件名
						String resultName = System.currentTimeMillis() + "-" + orignialName.substring(0, orignialName.lastIndexOf(".")) + suffix;

						File resultFile = new File(dirFile, resultName);// 保存
						fileItem.write(resultFile);// 写

						map.put("orignialName", orignialName);// 原文件名
						map.put("orignialsize", fileItem.getSize());// 原文件大小
						// map.put("resultName", resultName);// 系统保存文件名
						String resultPath = dirPath + File.separator + resultName;// 系统中文件路径
						// map.put("resultPath", resultPath);

						// 开始执行
						String resultExcel = ExcelUtil.getAreaByPhoneNum_multiple(resultPath);
						map.put("resultExcel", resultExcel);
					}

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println(JSON.toJSONString(map));

		return JSON.toJSONString(map);
	}




}
