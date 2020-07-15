package com.ysy.tmall.product.web;

import com.ysy.tmall.product.entity.CategoryEntity;
import com.ysy.tmall.product.service.CategoryService;
import com.ysy.tmall.product.vo.web.Catalog2Vo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @anthor silenceYin
 * @date 2020/7/14 - 0:55
 */
@Controller
public class IndexController {

    @Resource
    private CategoryService categoryService;

    @GetMapping(value = {"/", "/index.html"})
    public String indexPage(Model model){
        // 查出首页1级分类
        List<CategoryEntity> categoryEntities = categoryService.getLevel1Categorys();

        model.addAttribute("categorys", categoryEntities);
        return "index";
    }

    @GetMapping(value = "/index/catalog.json")
    @ResponseBody
    public Map<String, List<Catalog2Vo>> getCatalogJson(){
        // 查出首页1级分类
        Map<String, List<Catalog2Vo>> catalogJson = categoryService.getCatalogJson();

        return catalogJson;
    }
}
