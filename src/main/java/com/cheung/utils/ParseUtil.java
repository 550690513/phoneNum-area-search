package com.cheung.utils;

import com.alibaba.fastjson.JSONObject;

/**
 * @author Cheung
 * @version 1.0.0
 * @date 2018/3/13
 */
public class ParseUtil {

	/**
	 * 淘宝api接口数据解析
	 * @param str
	 * @return
	 */
	public static JSONObject parseTBStr(String str) {
		JSONObject obj = new JSONObject();
		if (str.contains("\t")) str = str.replace("\t", "");
		str = str.substring(str.indexOf("{") + 1, str.length() - 1);
		String[] items = str.split(",");
		for (int i = 0; i < items.length; i++) {
			String[] split = items[i].split(":");
			obj.put(split[0].trim(), split[1].substring(1, split[1].length() - 1).trim());
		}
		return obj;
	}
}
