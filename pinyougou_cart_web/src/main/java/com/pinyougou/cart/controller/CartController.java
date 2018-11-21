package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.upload.CookieUtil;
import entity.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private HttpSession session;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    /**
     * 设置sessionid 基于cookie保存一周
     */
    private String getSessionId() {
        //尝试基于cookie名称取sessionId
        String sessionId = CookieUtil.getCookieValue(request, "sessionId", "utf-8");
        if (sessionId == null) {
            //如果根据cookie名称取不到sessionID
            sessionId = session.getId();
            //基于cookie保存sessionId一周
            CookieUtil.setCookie(request, response, "sessionId", sessionId, 3600 * 24 * 7, "utf-8");
        }
        return sessionId;
    }

    /**
     * 查询购物车列表数据, 用户页面展示购物车列表
     */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList() {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String sessionId = getSessionId();
        List<Cart> cartListSessionId = cartService.selectCartListFromRedis(sessionId);

        if ("anonymousUser".equals(username)) {//未登录 基于sessionId取购物车列表数据
            return cartListSessionId;

        } else {//登录 基于username取
            List<Cart> cartListUserName = cartService.selectCartListFromRedis(username);
            //用户登录前, 如果已经添加商品到购物车列表中
            if (cartListSessionId != null && cartListSessionId.size() > 0) {
                //登录后, 需要将登录前的购物车列表数据合并到登录后的购物车列表中
                cartListUserName = cartService.mergeCartList(cartListSessionId, cartListUserName);
                //清除登录前的购物车列表数据
                cartService.deleteCartList(sessionId);
                //将合并后的购物车列表,重新放入redis中 下次查询时, 查询时的是合并后的购物车列表
                cartService.saveCartListToRedis(username, cartListUserName);

            }
            return cartListUserName;
        }
    }


    /**
     * 添加商品到购物车
     */
    @RequestMapping("/addItemToCartList")
    @CrossOrigin(origins = "http://item.pinyougou.com",allowCredentials = "true")
    public Result addItemToCartList(Long itemId, Integer num) {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            String sessionId = getSessionId();
            //查询购物车列表
            List<Cart> cartList = findCartList();

            //添加商品到购物车列表
            cartList = cartService.addItemToCartList(cartList, itemId, num);

            if ("anonymousUser".equals(username)) {//未登录
                //将添加商品后的购物车列表再存入redis中
                cartService.saveCartListToRedis(sessionId, cartList);
                System.out.println("save cartList to redis by sessionId ...");
            } else {
                //用户登录 基于用户名保存购物车列表数据到redis
                cartService.saveCartListToRedis(username, cartList);
                System.out.println("save cartList to redis by username ...");

            }

            return new Result(true, "添加购物车成功");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return new Result(false, e.getMessage());
        } catch (Exception e1) {
            e1.printStackTrace();
            return new Result(false, "添加购物车失败");
        }
    }
}
