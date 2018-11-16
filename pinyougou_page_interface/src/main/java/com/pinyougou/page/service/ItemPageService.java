package com.pinyougou.page.service;

import groupentity.Goods;

public interface ItemPageService {
    /**
     * 根据tb_goods中的商品id查询商品静态页面需要的数据
     */
    public Goods findOne(Long goodsId);
}
