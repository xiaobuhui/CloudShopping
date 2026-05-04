package com.itbaizhan.shopping_pay_service.service;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.itbaizhan.shopping_common.pojo.Orders;
import com.itbaizhan.shopping_common.pojo.Payment;
import com.itbaizhan.shopping_common.result.BusException;
import com.itbaizhan.shopping_common.result.CodeEnum;
import com.itbaizhan.shopping_common.service.ZfbPayService;
import com.itbaizhan.shopping_pay_service.mapper.PaymentMapper;
import com.itbaizhan.shopping_pay_service.utils.ZfbPayConfig;
import com.itbaizhan.shopping_pay_service.utils.ZfbVerifierUtils;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
@DubboService
public class ZfbPayServiceImpl implements ZfbPayService {
    @Autowired
    private ZfbPayConfig zfbPayConfig;
    @Autowired
    private AlipayClient alipayClient;
    @Autowired
    private PaymentMapper paymentMapper;

    @Override
    public String pcPay(Orders orders) {
        /**
         * 判断订单状态，未支付才会生成二维码
         */
        if (orders.getStatus() != 1){
            throw new BusException(CodeEnum.ORDER_STATUS_ERROR);
        }
        // 1.创建请求对象
        /*AlipayTradePrecreateRequest：支付宝 SDK 提供的预创建订单请求对象；
        对应支付宝开放平台接口：统一收单线下交易预创建（专门用于生成扫码支付二维码）；*/
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        // 2.设置请求内容
        //2.1设置回调地址
        request.setNotifyUrl(zfbPayConfig.getNotifyUrl() + zfbPayConfig.getPcNotify());
        //2.2支付宝要求请求内容必须用json格式
        JSONObject bizContent = new JSONObject();
        bizContent.put("out_trade_no", orders.getId()); // 订单编号
        bizContent.put("total_amount", orders.getPayment()); // 订单金额
        bizContent.put("subject", orders.getCartGoods().get(0).getGoodsName()); //订单标题
        request.setBizContent(bizContent.toString());

        try {
            // 3.发送请求
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            // 4.返回二维码
            return response.getQrCode();
        } catch (AlipayApiException e) {
            throw new BusException(CodeEnum.QR_CODE_ERROR);
        }
    }

    /**
     * 在支付成功后，支付宝会通知我们支付成功，此时我们需要
     * 准备一个回调方法供支付宝调用，在该方法中我们要进行验签、
     * 修改订单状态、生成交易记录。
     */
    @Override
    public void checkSign(Map<String, Object> paramMap) {
        // 获取所有参数
        Map<String, String[]> requestParameterMap = (Map<String, String[]>) paramMap.get("requestParameterMap");
        // 验签
        boolean valid = ZfbVerifierUtils.isValid(requestParameterMap, zfbPayConfig.getPublicKey());
        // 验签失败，抛出异常
        if (!valid) {
            throw new BusException(CodeEnum.CHECK_SIGN_ERROR);
        }
    }

    @Override
    public void addPayment(Payment payment) {
        paymentMapper.insert(payment);
    }
}
