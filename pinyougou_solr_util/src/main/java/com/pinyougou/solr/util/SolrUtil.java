package com.pinyougou.solr.util;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;
    @Autowired
    private SolrTemplate solrTemplate;


    public void dataImport() {
        //查询满足导入条件的商品
        /*
        1、上架商品导入索引库  tb_goods  is_marketable='1'
		2、商品状态为1，正常状态 tb_item   status='1'
         */
        List<TbItem> itemList = itemMapper.selectAllGrounding();

        //处理规格动态域字段 需要当前商品规格名称 和规格选项值
        for (TbItem item : itemList) {
            //当前商品规格名称和商品规格选项值 例如:{"机身内存":"64G","网络":"电信4G"}
            String spec = item.getSpec();
            Map<String, String> specMap = JSON.parseObject(spec, Map.class);
            item.setSpecMap(specMap);
        }


        //将商品导入索引库
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();
    }
}
