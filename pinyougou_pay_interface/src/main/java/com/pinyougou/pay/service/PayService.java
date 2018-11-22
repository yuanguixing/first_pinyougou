package com.pinyougou.pay.service;

import java.util.Map;

public interface PayService {
    /**
     * 调用微信支付接口, 获取支付链接, 生成二维码
     */
    public Map<String, Object> createNative(String out_trade_no, String total_fee) throws Exception;
    /**
     *查询支付状态
     */
    public Map<String, String> queryStatus(String out_trade_no) throws Exception;

    Object findPayByUserId(String userId);

    void updateStatus(String out_trade_no, String transaction_id);
}
