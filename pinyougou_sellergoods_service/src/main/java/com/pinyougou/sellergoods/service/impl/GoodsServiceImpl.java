package com.pinyougou.sellergoods.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import groupentity.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.transaction.annotation.Transactional;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper; //商品
    @Autowired
    private TbGoodsDescMapper tbGoodsDescMapper; //商品详情
    @Autowired
    private TbItemCatMapper itemCatMapper; //商品分类
    @Autowired
    private TbBrandMapper brandMapper; //品牌
    @Autowired
    private TbSellerMapper sellerMapper;//商家
    @Autowired
    private TbItemMapper itemMapper; //商品明细

    @Autowired
    private JmsTemplate jmsTemplate; //activemq
    @Autowired
    private Destination addItemSolrDestination; //消息的同步目的地
    @Autowired
    private Destination deleteItemSolrDestination; //删除目的地

    @Autowired
    private Destination addItemPageDestination; //同步生成静态页
    @Autowired
    private Destination deleItemPageDestination; //


    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {
        //保存tb_goods表中的数据
        TbGoods tbGoods = goods.getGoods();
        //新录入的商品,都是未审核状态
        tbGoods.setAuditStatus("0");
        goodsMapper.insert(tbGoods);
        //保存tb_goods_desc表
        TbGoodsDesc goodsDesc = goods.getGoodsDesc();
        goodsDesc.setGoodsId(tbGoods.getId());
        tbGoodsDescMapper.insert(goodsDesc);

        if ("1".equals(tbGoods.getIsEnableSpec())) {

            //保存tb_item表
            List<TbItem> itemList = goods.getItemList();
            for (TbItem item : itemList) {
                //`title` varchar(100) NOT NULL COMMENT '商品标题',   //组装原则：spu名称+规格选择值 中间以逗号隔开
                String title = tbGoods.getGoodsName();
                //获取规格选项值
                String spec = item.getSpec();
                Map<String, String> specMap = JSON.parseObject(spec, Map.class);
                for (String key : specMap.keySet()) {
                    title += specMap.get(key);
                }
                item.setTitle(title);
                //`image` varchar(2000) DEFAULT NULL COMMENT '商品图片',  //从tb_goods_desc 图片列表中取第一个图片
                setItemValue(tbGoods, goodsDesc, item);

                //保存商品
                itemMapper.insert(item);
            }
        } else {
            //保存tb_item表
            List<TbItem> itemList = goods.getItemList();
            for (TbItem item : itemList) {
                //`title` varchar(100) NOT NULL COMMENT '商品标题',   //组装原则：spu名称+规格选择值 中间以逗号隔开
                String title = tbGoods.getGoodsName();
                item.setTitle(title);
                setItemValue(tbGoods, goodsDesc, item);
                //组装未启用规格时,页面无法传达数据
                //spec
                item.setSpec("{}");
                //price
                item.setPrice(tbGoods.getPrice());
                //num
                item.setNum(99999);
                item.setStatus("1");//是否有效商品
                item.setIsDefault("1");//是否默认商品
                //保存商品
                itemMapper.insert(item);
            }
        }


    }

    private void setItemValue(TbGoods tbGoods, TbGoodsDesc goodsDesc, TbItem item) {
        //`image` varchar(2000) DEFAULT NULL COMMENT '商品图片',  //从tb_goods_desc 图片列表中取第一个图片
        String itemImages = goodsDesc.getItemImages();
        List<Map> imageList = JSON.parseArray(itemImages, Map.class);
        if (imageList.size() > 0) {
            String image = (String) imageList.get(0).get("url");
            item.setImage(image);
        }

        //`categoryId` bigint(10) NOT NULL COMMENT '所属类目，叶子类目',   //三级分类id
        item.setCategoryid(tbGoods.getCategory3Id());
        //`create_time` datetime NOT NULL COMMENT '创建时间',
        item.setCreateTime(new Date());
        //`update_time` datetime NOT NULL COMMENT '更新时间',
        item.setUpdateTime(new Date());
        //`goods_id` bigint(20) DEFAULT NULL,
        item.setGoodsId(tbGoods.getId());
        //`seller_id` varchar(30) DEFAULT NULL,
        item.setSellerId(tbGoods.getSellerId());
        //// 以下三个字段，方便商品搜索的
        //`category` varchar(200) DEFAULT NULL, //分类名称
        TbItemCat tbItemCat = itemCatMapper.selectByPrimaryKey(tbGoods.getCategory3Id());
        item.setCategory(tbItemCat.getName());
        //`brand` varchar(100) DEFAULT NULL, //品牌名称
        TbBrand tbBrand = brandMapper.selectByPrimaryKey(tbGoods.getBrandId());
        item.setBrand(tbBrand.getName());
        //	//商家店铺名称
        //`seller` varchar(200) DEFAULT NULL,
        TbSeller tbSeller = sellerMapper.selectByPrimaryKey(tbGoods.getSellerId());
        item.setSeller(tbSeller.getNickName());
    }


    /**
     * 修改
     */
    @Override
    public void update(TbGoods goods) {
        goodsMapper.updateByPrimaryKey(goods);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbGoods findOne(Long id) {
        return goodsMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            //逻辑删除
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setIsDelete("1");
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();
        //查询的商品都是未删除状态的商品
        criteria.andIsDeleteIsNull();

        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                //criteria.andSellerIdLike("%" + goods.getSellerId() + "%");
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusEqualTo(goods.getAuditStatus());
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setAuditStatus(status);
            goodsMapper.updateByPrimaryKey(tbGoods);
        }
    }

    @Override
    public void updateIsMarketable(Long[] ids, String isMarketable) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            //注意,审核通过的商品才能上下架
            if ("1".equals(tbGoods.getAuditStatus())) {

                //上架
                if ("1".equals(isMarketable)) {
                    jmsTemplate.send(addItemSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(id+"");
                        }
                    });
                    jmsTemplate.send(addItemPageDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(id+"");
                        }
                    });
                }

                //下架
                if ("0".equals(isMarketable)) {
                    jmsTemplate.send(deleteItemSolrDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(id+"");
                        }
                    });
                    jmsTemplate.send(deleItemPageDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(id+"");
                        }
                    });
                }
                tbGoods.setIsMarketable(isMarketable);
                goodsMapper.updateByPrimaryKey(tbGoods);
            } else {
                throw new RuntimeException("只有审核通过的商品才能上下架");
            }
        }
    }

}
