package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.*;
import groupentity.Goods;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;



    @Override
    public Goods findOne(Long goodsId) {
        Goods goods = new Goods();
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(goodsId);
        goods.setGoods(tbGoods);

        //组装商品分类名称
        String category1Name = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory1Id()).getName();
        String category2Name = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory2Id()).getName();
        String category3Name = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id()).getName();
        Map<String,String> categoryMap = new HashMap<>();
        categoryMap.put("category1Name", category1Name);
        categoryMap.put("category2Name", category2Name);
        categoryMap.put("category3Name", category3Name);
        goods.setCategoryMap(categoryMap);

        TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
        goods.setGoodsDesc(goodsDesc);

        TbItemExample example = new TbItemExample();
        example.createCriteria().andGoodsIdEqualTo(goodsId);
        List<TbItem> itemList = itemMapper.selectByExample(example);
        goods.setItemList(itemList);

        return goods;
    }
}
