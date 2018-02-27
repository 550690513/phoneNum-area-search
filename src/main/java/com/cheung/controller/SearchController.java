package com.cheung.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Cheung on 2018/2/6.
 */
@Controller
@RequestMapping(value = "page/search")
public class SearchController {

	public static final String URL = "http://www.ip138.com:8080/search.asp?action=mobile&mobile=%s";

	/*public static void main(String[] args) {
		String url = "C:/Users/Administrator/Desktop/phoneNum2/y1.xls";
		try {
			ExcelUtils.getAreaByPhoneNum_multiple(url);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/

	@RequestMapping(value = "/singleSearch", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	@ResponseBody
	public String search(@RequestBody String paramJson, HttpServletRequest request, HttpServletResponse response) {

		JSONObject parseObj = JSON.parseObject(paramJson);
		String phoneNum = parseObj.getString("phoneNum");

		JSONObject resultObj = new JSONObject();
		JSONObject dataObj = new JSONObject();
		try {
			request.setCharacterEncoding("UTF-8");
			response.setContentType("text/html; charset=UTF-8");

			String url = String.format(URL, phoneNum);
			Document doc = Jsoup.connect(url).get();
			Elements els = doc.getElementsByClass("tdc2");
			String area = els.get(1).text();
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


	@RequestMapping(value = "/multipleSearch", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	@ResponseBody
	public String excelImport(HttpServletRequest request, HttpServletResponse response) {

		HashMap<String, Object> map = new HashMap<String, Object>();
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
					if (fileItem.isFormField()) {// 普通表单域提交
						continue;
					} else {// 文件上传表单域提交
						/**
						 * 解决文件名可能重复,造成文件覆盖的问题
						 */
						String name = fileItem.getName();// 原文件名
						System.out.println("接收到文件: " + name);
						String suffix = name.substring(name.lastIndexOf("."), name.length());// 后缀名
						String resultName = name.substring(0, name.lastIndexOf(".")) + "-" + new Date().getTime() + suffix;// 保存的文件名

						File resultFile = new File(dirFile, resultName);// 保存
						fileItem.write(resultFile);// 写

						map.put("name", name);// 原文件名
						map.put("resultName", resultName);// 系统保存的文件名
						map.put("size", fileItem.getSize());// 文件大小
						map.put("url", dirPath + resultName);// 系统中的文件路径

						// 开始执行
						// ExcelUtils.getAreaByPhoneNum_multiple(resultName);
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