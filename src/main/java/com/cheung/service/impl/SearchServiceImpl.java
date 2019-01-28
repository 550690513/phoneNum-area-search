package com.cheung.service.impl;

import com.cheung.model.SearchModel;
import com.cheung.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 查询 serviceImpl
 *
 * @version 1.0.2
 * @date 2018/2/6
 */
@Service
public class SearchServiceImpl implements SearchService {

	@Value("${URL_IP138}")
	private String urlOfIP138;
	@Value("${URL_TaoBao}")
	private String urlOfTaoBao;

	/**
	 * 单个查询
	 *
	 * @param phoneNum
	 * @return
	 */
	@Override
	public SearchModel singleSearch(String phoneNum) {

		SearchModel model = new SearchModel();
		model.setPhoneNum(phoneNum);

		String regex = "^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1}))+\\d{8})$";
		Pattern pattern = Pattern.compile(regex);
		if (!pattern.matcher(phoneNum).matches()) {
			model.setArea("error！手机号码格式不正确！");
			return model;
		}

		try {
			/**
			 * 方式一:请求ip138接口获取数据(通过解析html获取)
			 */
			String url = String.format(urlOfIP138, phoneNum);
			Document doc = Jsoup.connect(url).get();
			Elements els = doc.getElementsByClass("tdc2");
			String area = els.get(1).text();

			/**
			 * 方式二:请求淘宝api获取数据(通过解析json获取)
			 */
			/*String str = HttpRequestUtil.sendGet(urlOfTaoBao, "tel=" + phoneNum);
			JSONObject obj = ParseUtil.parseTBStr(str);
			String area = obj.getString("carrier");*/

			model.setArea(area);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return model;
	}

	/**
	 * 批量查询
	 *
	 * @param workbookData Excel工作簿数据
	 * @return
	 */
	@Override
	public Map<Integer, Map<Integer, SearchModel>> batchSearch(Map<Integer, Map<Integer, String>> workbookData) {
		if (CollectionUtils.isEmpty(workbookData)) {
			return null;
		}
		LinkedHashMap<Integer, Map<Integer, SearchModel>> resMap = new LinkedHashMap<>(workbookData.size());
		for (Map.Entry<Integer, Map<Integer, String>> wbEntry : workbookData.entrySet()) {
			Integer sheetIndex = wbEntry.getKey();
			Map<Integer, String> sheetData = wbEntry.getValue();
			if (CollectionUtils.isEmpty(sheetData)) {
				resMap.put(sheetIndex, null);
			} else {
				LinkedHashMap<Integer, SearchModel> modelMap = new LinkedHashMap<>(sheetData.size());
				for (Map.Entry<Integer, String> shEntry : sheetData.entrySet()) {
					Integer rowIndex = shEntry.getKey();
					String phoneNum = shEntry.getValue();
					if (StringUtils.isBlank(phoneNum)) {
						modelMap.put(rowIndex, null);
					} else {
						SearchModel searchModel = singleSearch(phoneNum);
						modelMap.put(rowIndex, searchModel);
					}
				}
				resMap.put(sheetIndex, modelMap);
			}
		}
		return resMap;
	}

}
