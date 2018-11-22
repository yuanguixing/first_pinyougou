package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.mapper.TbOrderMapper;
import com.pinyougou.mapper.TbPayLogMapper;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.upload.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbPayLogMapper payLogMapper;
    @Autowired
    private TbOrderMapper orderMapper;

    @Value("${appid}")
    private String appid; //公众号
    @Value("${partner}")
    private String partner; //商户号id
    @Value("${partnerkey}")
    private String partnerkey; //商户秘钥
    @Value("${notifyurl}")
    private String notifyurl; //回调地址

    @Override
    public Map<String, Object> createNative(String out_trade_no, String total_fee) throws Exception {
        //封装微信支付必须需要的参数
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", appid); //公众号id
        paramMap.put("mch_id", partner);//商户号
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串
        paramMap.put("body", "品优购"); //商品描述
        paramMap.put("out_trade_no", out_trade_no); //商品订单号
        paramMap.put("total_fee", total_fee); //商品金额 是分
        paramMap.put("spbill_create_ip", "127.0.0.1"); //终端ip
        paramMap.put("notify_url", notifyurl); //通知地址
        paramMap.put("trade_type", "NATIVE");//交易类型
        paramMap.put("productid", "1");  //商品id
        String paramXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
        System.out.println(paramXml);
        //调用微信支付统一下单接口, 完成获取支付链接操作
        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
        httpClient.setHttps(true);
        httpClient.setXmlParam(paramXml);
        httpClient.post();
        //处理响应结果
        String resultXml = httpClient.getContent();
        Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);
        String code_url = resultMap.get("code_url");
        Map<String, Object> map = new HashMap<>();
        map.put("code_url", code_url);
        map.put("out_trade_no", out_trade_no);
        map.put("total_fee", total_fee);
        return map;
    }

    @Override
    public Map<String, String> queryStatus(String out_trade_no) throws Exception {
        //封装微信支付平台所需要的参数
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", appid); //公众号id
        paramMap.put("mch_id", partner);//商户号
        paramMap.put("out_trade_no", out_trade_no);
        paramMap.put("nonce_str", WXPayUtil.generateNonceStr()); //随机字符串

        String paramXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);

        //调用微信支付查询接口, 完成查询操作
        HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
        httpClient.setHttps(true);
        httpClient.setXmlParam(paramXml);
        httpClient.post();

        //处理响应结果
        String resultXml = httpClient.getContent();
        Map<String, String> resultMap = WXPayUtil.xmlToMap(resultXml);
        return resultMap;
    }

    @Override
    public TbPayLog findPayByUserId(String userId) {
        return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
    }

    @Override
    public void updateStatus(String out_trade_no, String transaction_id) {
        //更新支付日志
        TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
        payLog.setTradeState("2"); //已支付
        payLog.setPayTime(new Date());
        payLog.setTransactionId(transaction_id);
        payLogMapper.updateByPrimaryKey(payLog);
        //更新订单
        String orderList = payLog.getOrderList();
        String[] ids = orderList.split(",");
        for (String id : ids) {
            TbOrder tbOrder = orderMapper.selectByPrimaryKey(Long.parseLong(id));
            tbOrder.setPaymentTime(new Date());
            tbOrder.setStatus("2");
            orderMapper.updateByPrimaryKey(tbOrder);
        }
        //清除缓存中的支付日志
        redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
    }
}
