package com.cheung.service;

import com.cheung.model.SearchModel;

import java.util.Map;

/**
 * 查询 service
 *
 * @version 1.0.2
 * @date 2018/2/6
 */
public interface SearchService {

	/**
	 * 单个查询
	 *
	 * @param phoneNum 手机号
	 * @return
	 */
	SearchModel singleSearch(String phoneNum);

	/**
	 * 批量查询
	 *
	 * @param workbookData Excel工作簿数据
	 * @return
	 */
	Map<Integer, Map<Integer, SearchModel>> batchSearch(Map<Integer, Map<Integer, String>> workbookData);
}
