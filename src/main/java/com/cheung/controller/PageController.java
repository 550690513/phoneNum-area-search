package com.cheung.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Cheung
 * @version 1.0.0
 * @date 2018/2/9
 */
@Controller
@RequestMapping(value = "page")
public class PageController {

	/**
	 * 通用页面跳转方法
	 *
	 * @param pageName
	 * @return
	 */
	@RequestMapping(value = "{pageName}", method = RequestMethod.GET)
	public String toPage(@PathVariable(value = "pageName") String pageName) {
		return pageName;
	}


}
