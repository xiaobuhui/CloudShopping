package com.itbaizhan.shopping_manager_api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.shopping_common.pojo.Admin;
import com.itbaizhan.shopping_common.result.BaseResult;
import com.itbaizhan.shopping_common.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

/**
 * 后台管理员
 */
@Slf4j
@RestController
@RequestMapping("/admin")
public class AdminController {
    @DubboReference
    private AdminService adminService;
    // security的加密工具
    @Autowired
    private PasswordEncoder encoder;

    /**
     *      * 新增管理员
     *      * @param admin 管理员对象
     *      * @return 执行结果
     *      */
    @PostMapping("/add")
    public BaseResult add(@RequestBody Admin admin) {
        String password = admin.getPassword();
        password = encoder.encode(password);
        admin.setPassword(password);
        //log.info("新增管理员:{}",admin.getUsername());
        //log.info("加密后的密码:{}",admin.getPassword());
        adminService.add(admin);
        return BaseResult.ok();
    }

    /**
     * 修改管理员
     * @param admin 管理员对象
     * @return 执行结果
     */
    @PutMapping("/update")
    public BaseResult update(@RequestBody Admin admin) {
        String password = admin.getPassword();
        //hasText判断字符串是否非空、非空白相当于!= null && !isEmpty()
        if (StringUtils.hasText(password)){ // 密码不为空加密
            password = encoder.encode(password);
            admin.setPassword(password);
        }
        //这里密码可能为空，所以我们在service中设置为空的话用旧密码
        adminService.update(admin);
        return BaseResult.ok();
    }

    /**
     * 删除管理员
     * @param aid 管理员id
     * @return 执行结果
     */
    @DeleteMapping("/delete")
    public BaseResult delete(Long aid) {
        adminService.delete(aid);
        return BaseResult.ok();
    }

    /**
     * 根据id查询管理员
     * @param aid
     * @return 查询到的管理员
     */
    @GetMapping("/findById")
    public BaseResult<Admin> findById(Long aid) {
        Admin admin = adminService.findById(aid);
        return BaseResult.ok(admin);
    }

    /**
     * 分页查询管理员
     * @param page 页码
     * @param size 每页条数
     * @return 查询结果
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyAuthority('/admin/search')")
    public BaseResult<Page<Admin>> search(int page, int size) {
        Page<Admin> adminPage = adminService.search(page, size);
        return BaseResult.ok(adminPage);
    }

    /**
     * 修改管理员角色
     * @param aid  管理员id
     * @param rids 角色id
     * @return 执行结果
     */
    @PutMapping("/updateRoleToAdmin")
    public BaseResult updateRoleToAdmin(Long aid, Long[] rids) {
        adminService.updateRoleToAdmin(aid,rids);
        return BaseResult.ok();
    }

    /**
     * 获取登录管理员名
     * @return 管理员名
     */
    @GetMapping("/getUsername")
    public BaseResult<String> getUsername() {
        // 1.获取会话对象
        SecurityContext context = SecurityContextHolder.getContext();
        // 2.获取认证对象
        Authentication authentication = context.getAuthentication();
        // 3.获取登录用户信息
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();
        return BaseResult.ok(username);
    }

}
