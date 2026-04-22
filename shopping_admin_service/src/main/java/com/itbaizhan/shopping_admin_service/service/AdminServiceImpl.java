package com.itbaizhan.shopping_admin_service.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.shopping_admin_service.mapper.AdminMapper;
import com.itbaizhan.shopping_common.pojo.Admin;
import com.itbaizhan.shopping_common.pojo.Permission;
import com.itbaizhan.shopping_common.service.AdminService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@DubboService
@Transactional
public class AdminServiceImpl implements AdminService {
    @Autowired
    AdminMapper adminMapper;
    @Override
    public void add(Admin admin) {
        adminMapper.insert(admin);
    }

    @Override
    public void update(Admin admin) {
        // 如果前端传来空密码，则密码还是原来的密码
        if(!StringUtils.hasText(admin.getPassword())){
            // 查询原来的密码
            String password = adminMapper.selectById(admin.getAid()).getPassword();
            admin.setPassword(password);
        }
        adminMapper.updateById(admin);
    }

    @Override
    public void delete(Long id) {
        // 删除用户的所有角色
        adminMapper.deleteAdminAllRole(id);
        // 删除用户
        adminMapper.deleteById(id);
    }

    @Override
    public Admin findById(Long id) {
        return adminMapper.findById(id);
    }

    @Override
    public Page<Admin> search(int page, int size) {
        return adminMapper.selectPage(new Page(page,size),null);
    }

    @Override
    public void updateRoleToAdmin(Long aid, Long[] rids) {
        // 删除用户的所有角色
        adminMapper.deleteAdminAllRole(aid);
        // 重新添加管理员角色
        for (Long rid : rids) {
            adminMapper.addRoleToAdmin(aid, rid);
        }
    }

    @Override
    public Admin findByAdminName(String username) {
        QueryWrapper<Admin> wrapper = new QueryWrapper();
        wrapper.eq("username", username);
        Admin admin = adminMapper.selectOne(wrapper);
        return admin;
    }

    @Override
    public List<Permission> findAllPermission(String username) {
        return adminMapper.findAllPermission(username);
    }
}
