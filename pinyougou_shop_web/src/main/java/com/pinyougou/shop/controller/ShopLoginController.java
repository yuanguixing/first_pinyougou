package com.pinyougou.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/login")
public class ShopLoginController {
    /**
     * 获取登录人信息
     */
    @RequestMapping("/getName")
    public Map<String, String> getName() {
        Map<String, String> map = new HashMap<>();
        //获取登录人信息
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("loginName", loginName);
        return map;
    }
}
