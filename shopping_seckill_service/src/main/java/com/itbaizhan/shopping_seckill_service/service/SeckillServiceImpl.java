package com.itbaizhan.shopping_seckill_service.service;

import cn.hutool.bloomfilter.BitMapBloomFilter;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itbaizhan.shopping_common.pojo.CartGoods;
import com.itbaizhan.shopping_common.pojo.Orders;
import com.itbaizhan.shopping_common.pojo.SeckillGoods;
import com.itbaizhan.shopping_common.result.BusException;
import com.itbaizhan.shopping_common.result.CodeEnum;
import com.itbaizhan.shopping_common.service.SeckillService;
import com.itbaizhan.shopping_seckill_service.mapper.SeckillGoodsMapper;
import com.itbaizhan.shopping_seckill_service.redis.RedissonLock;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@DubboService
@Service
@Slf4j
public class SeckillServiceImpl implements SeckillService {
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private BitMapBloomFilter bitMapBloomFilter;
    //注入锁
    @Autowired
    private RedissonLock redissonLock;

    /**
     * 每5秒查询一次数据库，更新redis中的秒杀商品数据
     * 条件为startTime < 当前时间 < endTime，库存大于0
     * 测试我们用1分一次
     */
    //@Scheduled(cron = "0/5 * * * * *")// 每5秒执行一次
   @Scheduled(cron = "0 0/1 * * * ?") // 从第 0 分钟开始，每隔 1 分钟
    public void refreshRedis(){
        /*!!!
        * 在更新redis数据前，我们必须先把redis中的商品数量同步到数据库，
        * 否则mysql原本的数据会更新覆盖redis中数据，造成严重错误*/
        // 将redis中秒杀商品的库存数据同步到mysql
        List<SeckillGoods> seckillGoodsListOld = redisTemplate.boundHashOps("seckillGoods").values();
        for (SeckillGoods seckillGoods : seckillGoodsListOld) {
            // 在数据库查询秒杀商品
            QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper();
            queryWrapper.eq("goodsId",seckillGoods.getGoodsId());
            SeckillGoods sqlSeckillGoods = seckillGoodsMapper.selectOne(queryWrapper);
            if (sqlSeckillGoods != null){
                // 修改数据库中秒杀商品的库存，和redis中的库存保持一致
                sqlSeckillGoods.setStockCount(seckillGoods.getStockCount());
                seckillGoodsMapper.updateById(sqlSeckillGoods);
            }
        }
        System.out.println("同步mysql秒杀商品到redis...");

        // 1.查询数据库中正在秒杀的商品
        QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper();
        Date date = new Date();
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
        queryWrapper.le("startTime",now) // 当前时间晚于开始时间
                .ge("endTime",now) // 当前时间早于结束时间
                .gt("stockCount",0); // 库存大于0
        List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(queryWrapper);

        // 2.删除之前的秒杀商品
        redisTemplate.delete("seckillGoods");

        // 3.保存现在正在秒杀的商品
        for (SeckillGoods seckillGoods : seckillGoodsList) {
            redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getGoodsId(),seckillGoods);
            // 将正在秒杀的商品保存到布隆过滤器
            bitMapBloomFilter.add(seckillGoods.getGoodsId().toString());
        }
    }
    @SentinelResource(value = "findPageByRedis")
    @Override
    public Page<SeckillGoods> findPageByRedis(int page, int size) {
        // 1.查询所有秒杀商品列表
        List<SeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();

        // 2.获取当前页商品列表
        // 开始截取索引(页数减一*每页条数)
        int start = (page - 1) * size;
        // 结束截取索引(最后一页的数据不够size条，就取列表末尾,不用减一，因为subList方法是左闭右开)
        int end = start + size > seckillGoodsList.size() ? seckillGoodsList.size() : start + size;
        // 获取当前页结果集
        List<SeckillGoods> seckillGoods = seckillGoodsList.subList(start, end);

        // 3.构造页面对象
        Page<SeckillGoods> seckillGoodsPage = new Page();
        seckillGoodsPage.setCurrent(page) // 当前页
                .setSize(size) // 每页条数
                .setTotal(seckillGoodsList.size()) // 总条数
                .setRecords(seckillGoods); // 结果集
/*        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }*/
        return seckillGoodsPage;
    }

    /*我们的redis是不保证数据的安全性的，如果有商品数据丢失，会给商家造成损失*/
    @Override
    public SeckillGoods findSeckillGoodsByRedis(Long goodsId) {
        // 布隆过滤器判断秒杀商品是否存在，如果不存在，直接返回空
        /*if (!bitMapBloomFilter.contains(goodsId.toString())){
            System.out.println("布隆过滤器判断商品不存在！");
            return null;
        }*/
        // 1.从redis中查询秒杀商品
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(goodsId);
        // 2.如果查到商品，返回
        if (seckillGoods != null) {
            System.out.println("从redis中查询秒杀商品");
            return seckillGoods;
        }
        return null;
    }

    //创建订单后，库存会扣减，但是如果时时不支付，这库存会被一直占用着
    //所以我们必须设置一个过期时间，如果订单过期，把库存回退
    //设置redis过期时间，利用redis过期监听方法在一段时间后查看订单状态。
    @Override
    public Orders createOrder(Orders orders) {
        //拿到锁的key，也就是id
        String lockKey = orders.getCartGoods().get(0).getGoodId().toString();
        //如果拿到锁就上锁，并设置上锁时间，拿不到就返回null
        if (redissonLock.lock(lockKey,10000)) {
            //如果遇到异常就立马释放锁，防止死锁
            try {
                // 1.生成订单对象
                orders.setId(IdWorker.getIdStr()); // 手动用雪花算法生产订单id
                orders.setStatus(1); // 订单状态未付款
                orders.setCreateTime(new Date()); // 订单创建时间
                orders.setExpire(new Date(new Date().getTime() + 1000 * 60 * 5)); // 订单过期时间5分钟
                // 计算商品价格
                CartGoods cartGoods = orders.getCartGoods().get(0);
                Integer num = cartGoods.getNum();
                BigDecimal price = cartGoods.getPrice();
                BigDecimal sum = price.multiply(BigDecimal.valueOf(num));
                orders.setPayment(sum);

                // 2.减少秒杀商品库存
                // 查询秒杀商品
                SeckillGoods seckillGoods = findSeckillGoodsByRedis(cartGoods.getGoodId());
                // 查询库存，库存不足抛出异常
                Integer stockCount = seckillGoods.getStockCount();
                if (seckillGoods == null || stockCount <= 0) {
                    throw new BusException(CodeEnum.NO_STOCK_ERROR);
                }
                // 减少库存
                seckillGoods.setStockCount(seckillGoods.getStockCount() - cartGoods.getNum());
                redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getGoodsId(), seckillGoods);

                // 3.保存订单数据
                //Redis 本质上存储的是字节数组，Java 对象存进去之前需要序列化成字节，取出来需要反序列化。
                //这里是设置key的序列化方式，设置字符串序列化方式
                redisTemplate.setKeySerializer(new StringRedisSerializer());
                // 设置订单一分钟过期
                redisTemplate.opsForValue().set(orders.getId(), orders, 1, TimeUnit.MINUTES);
                /**
                 * 给订单创建副本，副本的过期时间长于原订单
                 * redis过期后触发过期事件时，redis数据已经过期，此时只能拿到key，拿不到value。
                 * 而过期事件需要回退商品库存，必须拿到value即订单详情，才能拿到商品数据，进行回退操作
                 * 我们保存一个订单副本，过期时间长于原订单，此时就可以通过副本拿到原订单数据
                 */
                redisTemplate.opsForValue().set(orders.getId() + "_copy", orders, 2, TimeUnit.MINUTES);
                System.out.println("下单成功，订单号:" + orders.getId());
                System.out.println("库存:" + seckillGoods.getStockCount());
                return orders;
            } finally {
                redissonLock.unlock(lockKey);
            }
        }else {
            return null;
        }
    }

    @Override
    public Orders findOrder(String id) {
        return (Orders) redisTemplate.opsForValue().get(id);
    }

    //这里为了省事，我们不再掉支付宝支付接口了，直接假支付成功即可，
    //但是数据依旧该生成生成
    @Override
    public Orders pay(String orderId) {
        // 1.查询订单，设置数据
        Orders orders = (Orders) redisTemplate.opsForValue().get(orderId);
        if (orders == null) {
            throw new BusException(CodeEnum.ORDER_EXPIRED_ERROR); // 订单过期
        }

        orders.setStatus(2);
        orders.setPaymentTime(new Date());
        orders.setPaymentType(2); // 支付宝支付

        // 2.从redis删除订单数据
        redisTemplate.delete(orderId);
        //支付成功，订单副本就没啥用了，要删除
        redisTemplate.delete(orderId+"_copy");

        // 3.返回订单数据
        return orders;
    }

    @Override
    public void addRedisSeckillGoods(SeckillGoods seckillGoods) {
        redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getGoodsId(),seckillGoods);
        bitMapBloomFilter.add(seckillGoods.getGoodsId().toString());
    }

    //秒杀商品数据从mysql中查询，将这个方法独立出来便于流量控制
    //当超过阈值，则调用服务降级方法
    @SentinelResource(value = "findSecillGoodsByMySql",blockHandler = "mysqlBlockHandler")
    //用于标记一个需要被保护（限流、熔断、降级）的资源
    @Override
    public SeckillGoods findSecillGoodsByMySql(Long goodsId){
        // 4. 如果该商品不在秒杀状态，返回null
        QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper();
        queryWrapper.eq("goodsId", goodsId);
        SeckillGoods seckillGoodsMysql = seckillGoodsMapper.selectOne(queryWrapper);
        System.out.println("从mysql中查询秒杀商品");

        Date now = new Date();
        if (seckillGoodsMysql == null
                || now.after(seckillGoodsMysql.getEndTime())
                || now.before(seckillGoodsMysql.getStartTime())
                || seckillGoodsMysql.getStockCount() <= 0){
            return null;
        }
        // 5. 如果该商品在秒杀状态，将商品保存到redis中，并返回该商品
        addRedisSeckillGoods(seckillGoodsMysql);
        return seckillGoodsMysql;
    }

    /**
     * 降级处理
     * @return 空值
     */
    public SeckillGoods mysqlBlockHandler(Long goodsId, BlockException e){
        log.error("服务降级处理");
        return null;
    }
}
