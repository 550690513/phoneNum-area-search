package com.cheung.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by Cheung on 2018/2/9.
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
