package com.itbaizhan.shopping_admin_service.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.shopping_admin_service.mapper.RoleMapper;
import com.itbaizhan.shopping_common.pojo.Role;
import com.itbaizhan.shopping_common.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Slf4j
@DubboService
public class RoleServiceImpl implements RoleService {
    @Autowired
    private RoleMapper roleMapper;

    @Override
    public void add(Role role) {
        roleMapper.insert(role);
    }

    @Override
    public void update(Role role) {
        roleMapper.updateById(role);
    }

    @Override
    public void delete(Long id) {
        // 删除角色
        roleMapper.deleteById(id);
        // 删除角色的所有权限
        roleMapper.deleteRoleAllPermission(id);
        // 删除用户_角色中间表的相关数据
        roleMapper.deleteRoleAllAdmin(id);
    }

    @Override
    public Role findById(Long id) {
        return roleMapper.findById(id);
    }

    @Override
    public List<Role> findAll() {
        return roleMapper.selectList(null);
    }

    @Override
    public Page<Role> search(int page, int size) {
        return roleMapper.selectPage(new Page(page,size),null);
    }

    @Override
    public void updatePermissionToRole(Long rid, Long[] pids) {
        // 删除角色的所有权限
        roleMapper.deleteRoleAllPermission(rid);
        // 给角色添加权限
        for (Long pid : pids) {
            roleMapper.addPermissionToRole(rid,pid);
        }
    }
}
