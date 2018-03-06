package com.cheung.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * @author Cheung
 * @version 1.0.0
 * @date 2018/3/2
 */
@Controller
@RequestMapping(value = "page/download")
public class DownloadController {

	/*@RequestMapping(value = "download", method = RequestMethod.POST)
	@ResponseBody
	public void download(@RequestBody String fileName, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException {
		try {
			JSONObject obj = JSON.parseObject(fileName);
			fileName = obj.getString("fileName");

			File file = new File(fileName);
			// 如果文件不存在
			if (!file.exists()) {
				throw new RuntimeException("文件不存在");
			}
			// 设置响应头，控制浏览器下载该文件
			response.setHeader("content-disposition", "attachment;fileName=" + URLEncoder.encode(fileName, "UTF-8"));
			response.setCharacterEncoding("UTF-8");
			response.setContentType("application/msexcel");// 定义输出类型


			// 读取要下载的文件，保存到文件输入流
			FileInputStream in = new FileInputStream(fileName);
			// 创建输出流
			OutputStream out = response.getOutputStream();
			// 缓存区
			byte buffer[] = new byte[1024];
			int len = 0;
			// 循环将输入流中的内容读取到缓冲区中
			while ((len = in.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
			// 关闭
			in.close();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}*/

	@RequestMapping(value = "download", method = RequestMethod.POST)
	@ResponseBody
	public void test(HttpServletRequest request, HttpServletResponse response){

		String fileName = request.getParameter("fileName");

		try {
			File file = new File("F://test.xlsx");
			InputStream is = new FileInputStream(file);
			response.reset(); // 必要地清除response中的缓存信息
			response.setHeader("Content-Disposition", "attachment; filename=" + fileName);// 在浏览器提示用户是保存还是下载
			response.setContentType("application/octet-stream; charset=UTF-8");// 根据个人需要,这个是下载文件的类型
			response.setHeader("Content-Length", String.valueOf(file.length()));// 告诉浏览器下载文件的大小
			OutputStream out = response.getOutputStream();
			byte[] content = new byte[1024];
			int length = 0;
			while ((length = is.read(content)) != -1) {
				out.write(content, 0, length);
			}
			out.write(content);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
