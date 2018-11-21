package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import entity.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    /**
     * @param itemId
     * @param num
     * @return
     */
    @Override
    public List<Cart> addItemToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //根据商品ip获取该商品对应商家id
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        //强制判断
        if (item == null) {
            throw new RuntimeException("该商品不存在");
        }

        String sellerId = item.getSellerId();

        //判断该商家对应的购物车是否存在于购物车列表中
        Cart cart = searchCartBySellerId(cartList, sellerId);
        if (cart == null) {
            //创建购物车对象,添加到购物车列表中
            cart = new Cart();
            cart.setSellerId(sellerId);            //商家id
            cart.setSellerName(item.getSeller()); //商家店铺名称

            //创建购物车列表对象
            List<TbOrderItem> orderItemList = new ArrayList<>();

            //创建购物车明细对象
            TbOrderItem orderItem = createOrderItem(item, num);
            orderItemList.add(orderItem);

            cart.setOrderItemList(orderItemList);
            //将创建的购物车对象添加到购物车列表中
            cartList.add(cart);

        } else {//如果存在该商家对应的购物车数据
            //再判断该商品是否存在于购物车明细对象中
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            TbOrderItem orderItem = searchOrderItemByItemId(orderItemList, itemId);
            if (orderItem == null) { //如果不存在该商品
                //创建商品明细对象, 将其添加到购物车明细列表
                orderItem = createOrderItem(item, num);
                orderItemList.add(orderItem);
            } else { //如果存在该商品
                //修改该商品明细对象的数量和小计金额
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));
                //购物车商品数量减至0 以下时
                if (orderItem.getNum() <= 0) {
                    orderItemList.remove(orderItem);
                }
                //购物车中没有任何该商家的商品时
                if (orderItemList.size() <= 0) {
                    //移除购物车列表中该购物车对象
                    cartList.remove(cart);
                }
            }
        }

        return cartList;
    }


    /**
     * 根据商品ID从购物车明细列表中获取购物车明细对象
     *
     * @param orderItemList
     * @param itemId
     * @return
     */
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue() == itemId.longValue()) {
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 创建购物车商品明细对象
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if (num < 1) {
            throw new RuntimeException("添加商品数量不能小于1");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setItemId(item.getId());
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setTitle(item.getTitle()); //商品标题
        orderItem.setPrice(item.getPrice());// 商品单价
        orderItem.setNum(num);
        orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum())); //商品总金额
        orderItem.setPicPath(item.getImage()); //图片地址
        orderItem.setSellerId(item.getSellerId());
        return orderItem;
    }

    /**
     * //判断该商家对应的购物车是否存在于购物车列表中
     *
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (sellerId.equals(cart.getSellerId())) {
                return cart;
            }
        }
        return null;
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> selectCartListFromRedis(String sessionId) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundValueOps(sessionId).get();
        //没有添加商品到购物车列表, 直接查询购物车列表页面时,购物车
        if (cartList == null) {
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    @Override
    public void saveCartListToRedis(String sessionId, List<Cart> cartList) {
        redisTemplate.boundValueOps(sessionId).set(cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartListSessionId, List<Cart> cartListUserName) {
        //遍历登录前的购物车
        for (Cart cart : cartListSessionId) {
            //添加登录前的购物车列表中的商品合并到登录后的购物车列表中
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            for (TbOrderItem orderItem : orderItemList) {
                cartListUserName = addItemToCartList(cartListUserName, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartListUserName;
    }

    @Override
    public void deleteCartList(String sessionId) {
        redisTemplate.delete(sessionId);
    }
}
