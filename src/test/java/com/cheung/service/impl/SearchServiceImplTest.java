package com.cheung.service.impl;

import com.cheung.model.SearchModel;
import com.cheung.service.SearchService;
import junit.framework.TestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author Cheung
 */
public class SearchServiceImplTest extends TestCase {

	@Autowired
	private SearchService searchService;

	public void testSingleSearch() {

		SearchModel searchModel = this.searchService.singleSearch("13866668888");

		System.out.println(searchModel.toString());
	}

	public void testBatchSearch() {
	}
}