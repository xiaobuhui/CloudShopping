package com.itbaizhan.shopping_manager_api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.shopping_common.pojo.Specification;
import com.itbaizhan.shopping_common.pojo.SpecificationOptions;
import com.itbaizhan.shopping_common.result.BaseResult;
import com.itbaizhan.shopping_common.service.SpecificationService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品规格
 */
@RestController
@RequestMapping("/specification")
public class SpecificationController {
    @DubboReference
    private SpecificationService specificationService;

    /**
     * 新增商品规格
     * @param specification 商品规格
     * @return 执行结果
     */
    @PostMapping("/add")
    public BaseResult add(@RequestBody Specification specification){
        specificationService.add(specification);
        return BaseResult.ok();
    }

    /**
     * 修改商品规格
     * @param specification 商品规格
     * @return 执行结果
     */
    @PutMapping("/update")
    public BaseResult update(@RequestBody Specification specification){
        specificationService.update(specification);
        return BaseResult.ok();
    }

    /**
     * 删除商品规格
     * @param ids 商品规格id集合
     * @return 执行结果
     */
    @DeleteMapping("/delete")
    public BaseResult delete(Long[] ids){
        specificationService.delete(ids);
        return BaseResult.ok();
    }

    /**
     * 根据id查询商品规格
     * @param id 商品规格id
     * @return 查询结果
     */
    @GetMapping("/findById")
    public BaseResult findById(Long id){
        Specification specification = specificationService.findById(id);
        return BaseResult.ok(specification);
    }

    /**
     * 分页查询商品规格
     * @param page 页码
     * @param size 每页条数
     * @return 查询结果
     */
    @GetMapping("/search")
    public BaseResult<Page<Specification>> search(int page,int size){
        Page<Specification> page1 = specificationService.search(page, size);
        return BaseResult.ok(page1);
    }

    /**
     * 查询某种商品类型下的所有规格
     * @param id 商品类型id
     * @return 查询结果
     */
    @GetMapping("/findByProductTypeId")
    public BaseResult<List<Specification>> findByProductTypeId(Long id){
        List<Specification> specifications = specificationService.findByProductTypeId(id);
        return BaseResult.ok(specifications);
    }

    /**
     * 新增商品规格项
     * @param specificationOptions 商品规格项集合
     * @return 执行结果
     */
    @PostMapping("/addOption")
    public BaseResult addOption(@RequestBody SpecificationOptions specificationOptions){
        specificationService.addOption(specificationOptions);
        return BaseResult.ok();
    }

    /**
     * 删除商品规格项
     * @param ids 商品规格项id集合
     * @return 执行结果
     */
    @DeleteMapping("/deleteOption")
    public BaseResult deleteOption(Long[] ids){
        specificationService.deleteOption(ids);
        return BaseResult.ok();
    }
}
