package com.pinyougou.page.listener;


import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import groupentity.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddItemPageListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            String goodsId = textMessage.getText();
            //同步生成静态页
            // 第一步：创建一个 Configuration 对象，直接 new 一个对象。构造方法的参数就是freemarker 的版本号。
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            // 第四步：加载一个模板，创建一个模板对象。
            Template template = configuration.getTemplate("item.ftl");
            // 第五步：创建一个模板使用的数据集，可以是 pojo 也可以是 map。一般是 Map。
            Goods goods = itemPageService.findOne(Long.parseLong(goodsId));
            List<TbItem> itemList = goods.getItemList();
            for (TbItem item : itemList) {
                Map<String, Object> map = new HashMap<>();
                map.put("item", item); //当前item数据
                map.put("goods", goods);
                // 第六步：创建一个 Writer 对象，一般创建一 FileWriter 对象，指定生成的文件名。
                Writer out = new FileWriter("D:/test_code65/item/" + item.getId() + ".html");
                // 第七步：调用模板对象的 process 方法输出文件。
                template.process(map, out);
                // 第八步：关闭流
                out.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
