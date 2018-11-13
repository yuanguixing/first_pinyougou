package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {
        //高亮查询对象
        HighlightQuery query = new SimpleHighlightQuery();
        //1.关键字搜索
        String keywords = (String) searchMap.get("keywords");
        Criteria criteria = null;
        if (keywords != null && !"".equals(keywords)) {
            //输入了关键字
            criteria = new Criteria("item_keywords").is(keywords);
        } else {
            //未输入关键字, 查询所有
            criteria = new Criteria().expression("*:*");
        }

        //2. 基于品牌进行条件过滤查询
        String brand = (String) searchMap.get("brand");
        if (brand != null && !"".equals(brand)) {
            //设置品牌查询条件
            Criteria brandCriteria = new Criteria("item_brand").is(brand);
            //设置过滤查询对象
            SimpleHighlightQuery filterQuery = new SimpleHighlightQuery(brandCriteria);
            query.addFilterQuery(filterQuery);
        }
        //3. 基于分类进行条件过滤查询
        String category = (String) searchMap.get("category");
        if (category != null && !"".equals(category)) {
            //设置过滤查询条件
            Criteria categoryCriteria = new Criteria("item_category").is(category);
            //设置过滤查询对象
            SimpleHighlightQuery filterQuery = new SimpleHighlightQuery(categoryCriteria);
            query.addFilterQuery(filterQuery);
        }

        //4. 基于规格进行条件过滤查询
        Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
        if (specMap != null) {
            //设置规格过滤查询条件
            for (String key : specMap.keySet()) {
                Criteria specCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                //设置规格过滤查询对象
                SimpleHighlightQuery filterQuery = new SimpleHighlightQuery(specCriteria);
                query.addFilterQuery(filterQuery);
            }
        }

        //5. 基于规格进行条件过滤查询
        String price = (String) searchMap.get("price");
        if (specMap != null && !"".equals(price)) {
            //0-500 500-1000 3000-*   考虑价格临界值 0
            //0-500进来, 判断 0==0
            String[] prices = price.split("-");
            if (!"0".equals(prices[0])) {
                //设置规格过滤查询条件
                Criteria priceCriteria = new Criteria("item_price").greaterThanEqual(prices[0]);
                //设置规格过滤查询对象
                SimpleHighlightQuery filterQuery = new SimpleHighlightQuery(priceCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!"*".equals(prices[1])) {
                //设置规格过滤查询条件
                Criteria priceCriteria = new Criteria("item_price").lessThanEqual(prices[1]);
                //设置规格过滤查询对象
                SimpleHighlightQuery filterQuery = new SimpleHighlightQuery(priceCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //6.商品排序操作
        String sortField = (String) searchMap.get("sortField");
        String sort = (String) searchMap.get("sort");
        if (sortField != null && !"".equals(sortField)) {
            //设置排序条件
            if ("ASC".equals(sort)) {
                //升序
                query.addSort(new Sort(Sort.Direction.ASC, "item_" + sortField));
            } else {
                //降序
                query.addSort(new Sort(Sort.Direction.DESC, "item_" + sortField));
            }
        }

        //7.分页条件查询
        Integer pageNo = (Integer) searchMap.get("pageNo");
        Integer pageSize = (Integer) searchMap.get("pageSize");

        query.setOffset((pageNo - 1) * pageSize);//分页起始值
        query.setRows(pageSize);//每页记录数据

        //将查询条件付给总查询对象
        query.addCriteria(criteria);

        //设置高亮显示
        HighlightOptions highlightOptions = new HighlightOptions();
        //设置高亮字段
        highlightOptions.addField("item_title");
        //设置高亮前缀 和后缀
        highlightOptions.setSimplePrefix("<font color='red'>");
        highlightOptions.setSimplePostfix("</font>");
        query.setHighlightOptions(highlightOptions);

        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);

        //总记录数  page.getTotalElements()
        //总条数  page.getTotalPages()

        List<TbItem> content = page.getContent();
        //高亮显示结果处理
        for (TbItem item : content) {
            //获取高亮结果值
            List<HighlightEntry.Highlight> highlights = page.getHighlights(item);
            if (highlights.size() > 0) {
                //获取高亮结果
                HighlightEntry.Highlight highlight = highlights.get(0);
                List<String> snipplets = highlight.getSnipplets();
                if (snipplets.size() > 0) {
                    item.setTitle(snipplets.get(0));
                }
            }
        }
        //创建map集合, 封装查询结果
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("rows", content);
        resultMap.put("pageNo", pageNo);
        resultMap.put("totalPages", page.getTotalPages());

        return resultMap;
    }
}
