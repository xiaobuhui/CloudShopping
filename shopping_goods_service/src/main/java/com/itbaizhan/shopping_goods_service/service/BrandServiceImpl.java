package com.itbaizhan.shopping_goods_service.service;

import com.itbaizhan.shopping_common.pojo.Brand;
import com.itbaizhan.shopping_common.service.BrandService;
import com.itbaizhan.shopping_goods_service.mapper.BrandMapper;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@DubboService
/*服务层的方法不可分割，要么同时成功，要么同时失败*/
@Transactional
public class BrandServiceImpl implements BrandService {
    @Autowired
    private BrandMapper brandMapper;
    /**
     * 根据id查询品牌
     * @param id
     * @return
     */
    @Override
    public Brand findById(Long id) {
        return brandMapper.selectById(id);
    }
}

