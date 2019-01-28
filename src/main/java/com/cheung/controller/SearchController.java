package com.cheung.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cheung.model.SearchModel;
import com.cheung.service.SearchService;
import com.cheung.utils.ExcelUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.*;

/**
 * 查询 controller
 *
 * @author Cheung
 * @version 1.0.2
 * @date 2018/2/6
 */
@RestController
@RequestMapping("search")
public class SearchController {

	@Autowired
	private SearchService searchService;


	/**
	 * 单个查询
	 *
	 * @param phoneNum
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/singleSearch", method = RequestMethod.GET, produces = "application/json; charset=UTF-8")
	public String singleSearch(@RequestParam(value = "phoneNum", required = true) String phoneNum,
	                           HttpServletRequest request,
	                           HttpServletResponse response) {

		JSONObject resObj = new JSONObject();
		try {
			request.setCharacterEncoding("UTF-8");
			response.setContentType("text/html; charset=UTF-8");

			// 查询
			SearchModel searchModel = searchService.singleSearch(phoneNum);

			resObj.put("data", searchModel);
			resObj.put("code", "200");
			resObj.put("msg", "success");
		} catch (Exception e) {
			resObj.put("msg", "fail");
			e.printStackTrace();
		}
		return resObj.toJSONString();
	}

	/**
	 * 批量查询
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@RequestMapping(value = "/batchSearch", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
	public String batchSearch(HttpServletRequest request, HttpServletResponse response) {
		HashMap<String, Object> resMap = new LinkedHashMap<String, Object>(7);
		try {
			request.setCharacterEncoding("utf-8");
			response.setContentType("text/html;charset=utf-8");

			// 初始化file
			String filePath = ExcelUtil.init(request);
			// 获取数据
			Map<Integer, Map<Integer, String>> fileData = ExcelUtil.getFileData(filePath);
			// 查询
			Map<Integer, Map<Integer, SearchModel>> searchData = searchService.batchSearch(fileData);
			// 反写数据到结果文件
			String resFile = ExcelUtil.writeData(filePath, searchData);
			resMap.put("resFile", resFile);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return JSON.toJSONString(resMap);
	}

	/**
	 * 批量查询
	 *
	 * @param request
	 * @param response
	 * @return
	 */
	@Deprecated
	@RequestMapping(value = "/multipleSearch", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
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

						/*
						 * 解决文件名可能重复,造成文件覆盖的问题
						 */
						String oriFile = fileItem.getName();// 原文件名
						System.out.println("接收到文件: " + oriFile);
						String suffix = oriFile.substring(oriFile.lastIndexOf("."), oriFile.length());// 后缀名
						// 保存的文件名
						String resName = System.currentTimeMillis() + "-" + oriFile.substring(0, oriFile.lastIndexOf(".")) + suffix;

						File file = new File(dirFile, resName);// 保存
						fileItem.write(file);// 写

						String resPath = dirPath + File.separator + resName;// 保存的文件的路径

						// 批量查询
						String resFile = ExcelUtil.multipleSearch(resPath);

						map.put("oriFile", oriFile);
						map.put("oriSize", fileItem.getSize());
						map.put("resFile", resFile);
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
