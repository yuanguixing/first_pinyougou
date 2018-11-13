package cn.itcastdemo;

import com.pinyougou.pojo.TbItem;
import com.pinyougou.solr.util.SolrUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext*.xml")
public class SolrTest {
    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private SolrUtil solrUtil;
    @Test
    public void test() {
        solrUtil.dataImport();

    }



    /**
     * 新增 修改 solr索引数据
     */
    @Test
    public void save() {
        TbItem item = new TbItem();
        item.setId(2L);
        item.setBrand("华为");
        item.setTitle("华为p20 移动3G 64G");
        item.setSeller("华为旗航店");
        solrTemplate.saveBean(item);
        solrTemplate.commit(); //保存数据时, 必须提交
    }

    /**
     * 基于id查询商品数据
     */
    @Test
    public void queryById() {
        TbItem item = solrTemplate.getById(1, TbItem.class);
        System.out.println(item.getId() + " .. " + item.getBrand() + "  " + item.getTitle() + "  " + item.getSeller());
    }

    /**
     * 基于id删除商品数据
     */
    @Test
    public void DeleteById() {
        solrTemplate.deleteById("1");
        solrTemplate.commit();
    }


    /**
     * 删除所有商品数据
     */
    @Test
    public void DeleteAll() {
        SolrDataQuery query = new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    /**
     * 批量添加100条商品数据
     */
    @Test
    public void saveBatch() {
        List<TbItem> items = new ArrayList<>();
        for (long i = 1; i <= 100; i++) {
            TbItem item = new TbItem();
            item.setId(i);
            item.setBrand("华为");
            item.setTitle(i + "华为p20 移动3G 64G");
            item.setSeller("华为" + i + "旗航店");
            items.add(item);
        }
        solrTemplate.saveBeans(items);
        solrTemplate.commit(); //保存数据时, 必须提交
    }

    /**
     * 分页查询商品数据
     */
    @Test
    public void queryPage() {
        Query query = new SimpleQuery("*:*");
        //设置分页条件
        query.setOffset(2); //分页查询起始值 默认起始值0   默认每页查询10条
        query.setRows(5); //每页显示记录数据
        ScoredPage<TbItem> items = solrTemplate.queryForPage(query, TbItem.class);
        //输出分页查询结果
        for (TbItem item : items) {
            System.out.println(item.getId() + " .. " + item.getBrand() + "  " + item.getTitle() + "  " + item.getSeller());
        }
    }

    /**
     * 条件查询商品数据 多条件
     */
    @Test
    public void multiQueryTest() {
        Query query = new SimpleQuery();
        //设置查询条件
        Criteria criteria = new Criteria("item_title").contains("9").and("item_seller").contains("5");
        query.addCriteria(criteria);

        ScoredPage<TbItem> items = solrTemplate.queryForPage(query, TbItem.class);
        //输出分页查询结果
        for (TbItem item : items) {
            System.out.println(item.getId() + " .. " + item.getBrand() + "  " + item.getTitle() + "  " + item.getSeller());
        }
    }

    /**
     * 条件查询商品数据 高亮
     */
    @Test
    public void multiQuery() {
        //高亮查询对象
        HighlightQuery query = new SimpleHighlightQuery();
        //设置查询条件
        Criteria criteria = new Criteria("item_seller").contains("华为");
        query.addCriteria(criteria);

        //高亮对象
        HighlightOptions highlightOptions = new HighlightOptions();
        //需要制定高亮字段是什么, 需要设置高亮内容的前缀和后缀
        highlightOptions.addField("item_seller");
        highlightOptions.setSimplePrefix("<font color='red'>");
        highlightOptions.setSimplePostfix("</font>");

        //高亮设置
        query.setHighlightOptions(highlightOptions);

        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //输出分页查询结果
        for (TbItem item : page) {
            //需要进行高亮结果替换操作
            List<HighlightEntry.Highlight> highlights = page.getHighlights(item);
            if (highlights.size() > 0) {
                //说明有高亮内容
                List<String> snipplets = highlights.get(0).getSnipplets();
                if (snipplets.size() > 0) {
                    //重新设置高亮字段, 进行替换
                    item.setSeller(snipplets.get(0));
                }
            }
            System.out.println(item.getId() + " .. " + item.getBrand() + "  " + item.getTitle() + "  " + item.getSeller());
        }
    }
@Test
    public void Test() {

}

}
