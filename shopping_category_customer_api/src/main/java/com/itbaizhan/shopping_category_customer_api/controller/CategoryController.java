package com.itbaizhan.shopping_category_customer_api.controller;

import com.itbaizhan.shopping_common.pojo.Category;
import com.itbaizhan.shopping_common.result.BaseResult;
import com.itbaizhan.shopping_common.service.CategoryService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 广告
 */
@RestController
@RequestMapping("/user/category")
public class CategoryController {
    @DubboReference
    private CategoryService categoryService;
    /**
     * 查询全部启用广告
     * @return 查询结果
     */
    @GetMapping("/all")
    public BaseResult<List<Category>> findAll() {
        List<Category> categories = categoryService.findAll();
        return BaseResult.ok(categories);
    }
}
