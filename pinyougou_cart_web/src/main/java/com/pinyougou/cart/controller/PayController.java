package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.PayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.upload.IdWorker;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference
    private PayService payService;


    /**
     * 生成二维码
     */
    @RequestMapping("/createNative")
    public Map<String, Object> createNative(){
        try {
            //基于redis中的用户名获取支付日志
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            TbPayLog payLog = (TbPayLog) payService.findPayByUserId(userId);
            return payService.createNative(payLog.getOutTradeNo(),payLog.getTotalFee()+"");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 查询支付状态
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no){
        int i = 1;
        try {
            //持续性查询用户支付状态
            while(true){
                //每隔3秒查询一次
                Thread.sleep(3000);
                i++;
                //如果用户5分钟没有支付, 支付超时, 跳出循环
                if (i>=100){
                    return new Result(false,"timeout");
                }

                Map<String, String> resultMap = payService.queryStatus(out_trade_no);
                //获取支付状态
                if ("SUCCESS".equals(resultMap.get("trade_state"))) {
                    //1.保存订单时,插入一条支付操作. 此时订单和支付日志中的支付状态都是 未支付
                    //2.当用户微信扫码支付成功后,修改订单和支付日志中的支付状态, 为 已支付 修改支付时间 为当前时间
                    //支付成功后,获取微信返回交易流水号
                    String transaction_id = resultMap.get("transaction_id");
                    //支付成功后, 更新订单状态 和支付日志状态
                    payService.updateStatus(out_trade_no, transaction_id);

                    //支付成功
                    return new Result(true,"支付成功");

                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false,"支付失败");
        }
    }
}
