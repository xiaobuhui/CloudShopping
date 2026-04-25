package com.itbaizhan.shopping_category_service.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.shopping_category_service.mapper.CategoryMapper;
import com.itbaizhan.shopping_common.pojo.Category;
import com.itbaizhan.shopping_common.service.CategoryService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
@DubboService
@Transactional
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;
    // 对象名必须叫redisTemplate，否则由于容器中有多个RedisTemplate对象造成无法注入
    @Autowired
    private RedisTemplate redisTemplate;
    @Override
    public void add(Category category) {
        categoryMapper.insert(category);
        refreshRedisCategory();
    }

    @Override
    public void update(Category category) {
        categoryMapper.updateById(category);
        refreshRedisCategory();
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Category category = categoryMapper.selectById(id);
        category.setStatus(status);
        categoryMapper.updateById(category);
        refreshRedisCategory();
    }

    @Override
    public void delete(Long[] ids) {
        categoryMapper.deleteBatchIds(Arrays.asList(ids));
    }

    @Override
    public Category findById(Long id) {
        return categoryMapper.selectById(id);
    }

    @Override
    public Page<Category> search(int page, int size) {
        return categoryMapper.selectPage(new Page(page,size),null);
    }

    @Override
    public List<Category> findAll() {
        // 1.从redis中查询启用的广告
        // 1.1 获取操作redis中list数据的对象
        ListOperations<String,Category> listOperations = redisTemplate.opsForList();
        // 1.2 从redis中获取所有启用的广告
        /*range从 Redis 里取出一整个列表
        "categories"：Redis 里的 key（键）
        0,-1：获取列表中所有数据（0 = 开始，-1 = 最后一条）*/
        List<Category> categoryList = listOperations.range("categories", 0, -1);

        // 1.3 检查是否查询到结果
        if (categoryList != null && categoryList.size() > 0){
            // 2.如果查到结果，直接返回
            System.out.println("从redis中查询广告");
            return categoryList;
        }else {
            // 3.如果redis中没有数据，则从数据库查询广告，并同步到redis中
            System.out.println("从mysql中查询广告");
            // 从数据库查询所有启用的广告
            QueryWrapper<Category> queryWrapper = new QueryWrapper();
            queryWrapper.eq("status",1);
            List<Category> categories = categoryMapper.selectList(queryWrapper);
            // 同步到redis中
            //leftPushAll：把列表一次性存入 Redis
            listOperations.leftPushAll("categories",categories);
            return categories;
        }
    }
    /**
     * 更新redis中的广告数据
     */
    public void refreshRedisCategory(){
        // 从数据库查询广告
        QueryWrapper<Category> queryWrapper = new QueryWrapper();
        queryWrapper.eq("status",1);
        List<Category> categories = categoryMapper.selectList(queryWrapper);

        // 删除redis中的原有广告数据
        redisTemplate.delete("categories");
        // 将新的广告数据同步到redis中
        ListOperations<String,Category> listOperations = redisTemplate.opsForList();
        //leftPushAll：把列表一次性存入 Redis
        listOperations.leftPushAll("categories",categories);
    }
}
