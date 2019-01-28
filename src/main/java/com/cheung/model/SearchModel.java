package com.cheung.model;

import lombok.Data;
import lombok.ToString;

/**
 * 查询结果 model
 *
 * @author Cheung
 * @date 2019/01/26
 */
@Data
@ToString(callSuper = true)
public class SearchModel {

	/**
	 * 手机号
	 */
	private String phoneNum;

	/**
	 * 归属地
	 */
	private String area;

}
