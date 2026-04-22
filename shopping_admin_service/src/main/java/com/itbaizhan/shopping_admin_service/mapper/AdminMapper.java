package com.itbaizhan.shopping_admin_service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itbaizhan.shopping_common.pojo.Admin;
import com.itbaizhan.shopping_common.pojo.Permission;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AdminMapper extends BaseMapper<Admin> {
    // 删除管理员的所有角色
    void deleteAdminAllRole(Long aid);
    // 根据id查询管理员，包括角色和权限
    Admin findById(Long id);
    // 给管理员添加角色
    void addRoleToAdmin(@Param("aid") Long aid, @Param("rid") Long rid);
    // 根据管理员名查询权限
    List<Permission> findAllPermission(String username);

}
