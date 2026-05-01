package com.itbaizhan.shopping_user_service.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itbaizhan.shopping_common.pojo.ShoppingUser;
import com.itbaizhan.shopping_common.result.BusException;
import com.itbaizhan.shopping_common.result.CodeEnum;
import com.itbaizhan.shopping_common.service.ShoppingUserService;
import com.itbaizhan.shopping_common.utils.Md5Util;
import com.itbaizhan.shopping_user_service.mapper.ShoppingUserMapper;
import com.itbaizhan.shopping_user_service.util.JwtUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@DubboService
@Transactional
public class ShoppingUserServiceImpl implements ShoppingUserService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private ShoppingUserMapper shoppingUserMapper;
    @Override
    public void saveRegisterCheckCode(String phone, String checkCode) {
        /*opsForValue()：获取字符串类型操作对象
        ValueOperations：专门用来操作 Redis 的 String（键值对）*/
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // redis键为手机号，值为验证码，过期时间5分钟
        valueOperations.set("registerCode:" + phone, checkCode, 300, TimeUnit.SECONDS);
    }

    @Override
    public void registerCheckCode(String phone, String checkCode) {
        // 验证验证码
        ValueOperations valueOperations = redisTemplate.opsForValue();
        Object checkCodeRedis = valueOperations.get("registerCode:" + phone);
        if (!checkCode.equals(checkCodeRedis)) {
            throw new BusException(CodeEnum.REGISTER_CODE_ERROR);
        }
    }

    @Override
    public void register(ShoppingUser shoppingUser) {
        // 1.验证手机号是否存在
        String phone = shoppingUser.getPhone();
        QueryWrapper<ShoppingUser> queryWrapper = new QueryWrapper();
        queryWrapper.eq("phone", phone);
        //selectList()：根据你传入的查询条件，去数据库查询多条数据，并返回一个 List 集合。
        List<ShoppingUser> shoppingUsers = shoppingUserMapper.selectList(queryWrapper);
        if (shoppingUsers != null && shoppingUsers.size() > 0) {
            throw new BusException(CodeEnum.REGISTER_REPEAT_PHONE_ERROR);
        }

        // 2.验证用户名是否存在
        String username = shoppingUser.getUsername();
        QueryWrapper<ShoppingUser> queryWrapper1 = new QueryWrapper();
        queryWrapper1.eq("username", username);
        List<ShoppingUser> shoppingUsers1 = shoppingUserMapper.selectList(queryWrapper1);
        if (shoppingUsers1 != null && shoppingUsers1.size() > 0) {
            throw new BusException(CodeEnum.REGISTER_REPEAT_NAME_ERROR);
        }

        // 3.新增用户
        shoppingUser.setStatus("Y");
        shoppingUser.setPassword(Md5Util.encode(shoppingUser.getPassword()));
        shoppingUserMapper.insert(shoppingUser);
    }

    //根据用户名和密码登录
    @Override
    public String loginPassword(String username, String password) {
        QueryWrapper<ShoppingUser> queryWrapper = new QueryWrapper();
        queryWrapper.eq("username", username);
        //selectOne()：根据你传入的查询条件，去数据库查询一条数据，并返回一个对象。
        ShoppingUser shoppingUser = shoppingUserMapper.selectOne(queryWrapper);
        // 验证用户名
        if (shoppingUser == null) {
            throw new BusException(CodeEnum.LOGIN_NAME_PASSWORD_ERROR);
        }
        // 验证密码
        boolean verify = Md5Util.verify(password, shoppingUser.getPassword());
        if (!verify) {
            throw new BusException(CodeEnum.LOGIN_NAME_PASSWORD_ERROR);
        }
        // 3.生成JWT令牌，返回令牌
        String sign = JwtUtils.sign(shoppingUser.getId(),shoppingUser.getUsername());
        return sign;
    }

    //保存登录验证码到redis
    @Override
    public void saveLoginCheckCode(String phone, String checkCode) {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // redis键为手机号，值为验证码，过期时间5分钟
        valueOperations.set("loginCode:" + phone, checkCode, 300, TimeUnit.SECONDS);
    }

    // 验证登录验证码,在发送验证码就验证了是否注册等，所以这里账户一定存在
    @Override
    public String loginCheckCode(String phone, String checkCode) {
        // 验证用户传入的手机号验证码是否在redis中存在,没指定泛型只能返回Object类型
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 1.从Redis拿登录验证码（key：loginCode:手机号）
        Object checkCodeRedis = valueOperations.get("loginCode:" + phone);
        // 2. 判断验证码是否为空/过期
        // == 本质 比较的是：两个变量在内存里的 地址 / 引用指针
        if (checkCodeRedis == null) {
            throw new BusException(CodeEnum.LOGIN_CODE_EXPIRE);
        }
        // 3. 判断输入的验证码是否正确
        if (!checkCode.equals(checkCodeRedis)) {
            throw new BusException(CodeEnum.LOGIN_CODE_ERROR);
        }
        // 4. 删除Redis中的验证码（一次性有效，非常重要！一个验证码多次登录非常危险）
        redisTemplate.delete("loginCode:" + phone);
        // 5.登录成功，查询用户信息
        QueryWrapper<ShoppingUser> queryWrapper = new QueryWrapper();
        queryWrapper.eq("phone", phone);
        ShoppingUser shoppingUser = shoppingUserMapper.selectOne(queryWrapper);
        // 6.生成JWT令牌，返回令牌
        String sign = JwtUtils.sign(shoppingUser.getId(),shoppingUser.getUsername());
        return sign;
    }

    @Override
    public String getName(String token) {
        Map<String, Object> map = JwtUtils.verify(token);
        String username = (String) map.get("username");
        return username;
    }

    @Override
    public ShoppingUser getLoginUser(String token) {
        // 从令牌中获取用户id
        Map<String, Object> map = JwtUtils.verify(token);
        Long userId = (Long) map.get("userId");
        // 根据id查询用户
        QueryWrapper<ShoppingUser> queryWrapper = new QueryWrapper();
        queryWrapper.eq("id", userId);
        ShoppingUser shoppingUser = shoppingUserMapper.selectOne(queryWrapper);
        return shoppingUser;
    }

    @Override
    public void checkPhone(String phone) {
        // 1.判断手机号是否存在
        QueryWrapper<ShoppingUser> queryWrapper = new QueryWrapper();
        queryWrapper.eq("phone",phone);
        ShoppingUser shoppingUser = shoppingUserMapper.selectOne(queryWrapper);
        if (shoppingUser == null){
            throw new BusException(CodeEnum.LOGIN_NOPHONE_ERROR);
        }
        // 2.判断用户状态
        if(!"Y".equals(shoppingUser.getStatus())){
            throw new BusException(CodeEnum.LOGIN_USER_STATUS_ERROR);
        }
    }
}
