package com.ysy.tmall.search.controller;

import com.ysy.tmall.search.service.MallSearchService;
import com.ysy.tmall.search.vo.SearchParam;
import com.ysy.tmall.search.vo.SearchResult;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;

/**
 * @anthor silenceYin
 * @date 2020/7/18 - 4:10
 */
@Controller
public class SearchController {

    @Resource
    MallSearchService mallSearchService;

    @RequestMapping("list.html")
    public String listPage(SearchParam searchParam, Model model) {

        SearchResult result = mallSearchService.search(searchParam);
        model.addAttribute("result", result);
        return "list";
    }
}
