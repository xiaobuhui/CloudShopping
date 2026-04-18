package com.itbaizhan.shopping_manager_api.controller;

import com.itbaizhan.shopping_common.pojo.Brand;
import com.itbaizhan.shopping_common.result.BaseResult;
import com.itbaizhan.shopping_common.result.BusException;
import com.itbaizhan.shopping_common.result.CodeEnum;
import com.itbaizhan.shopping_common.service.BrandService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/brand")
public class BrandController {
    // 远程注入
    @DubboReference(timeout = 10000)
    private BrandService brandService;

    @GetMapping("/findById")
    public BaseResult<Brand> findById(Long id){
        Brand brand = brandService.findById(id);
        if (id == 0){
            int i = 1/0; // 模拟系统异常
        }else if (id == -1){
            throw new BusException(CodeEnum.PARAMETER_ERROR); // 模拟业务异常
        }
        return BaseResult.ok(brand);
    }
}
