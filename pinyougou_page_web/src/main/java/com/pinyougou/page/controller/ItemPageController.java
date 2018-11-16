package com.pinyougou.page.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbItem;

import freemarker.template.Configuration;
import freemarker.template.Template;
import groupentity.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;


import java.io.FileWriter;

import java.io.Writer;
import java.util.*;

@RestController
@RequestMapping("/itemPage")
public class ItemPageController {
    @Reference
    private ItemPageService itemPageService;

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    /**
     * 基于商品id 查询商品数据生成静态页面
     *
     * @param goodsId
     * @return
     */
    @RequestMapping("/getHtml")
    public String getHtml(Long goodsId) {

        try {
            // 第一步：创建一个 Configuration 对象，直接 new 一个对象。构造方法的参数就是freemarker 的版本号。
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            // 第四步：加载一个模板，创建一个模板对象。
            Template template = configuration.getTemplate("item.ftl");
            // 第五步：创建一个模板使用的数据集，可以是 pojo 也可以是 map。一般是 Map。
            Goods goods = itemPageService.findOne(goodsId);
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
            return "success ......."; //一个标记 没有实际意义
        } catch (Exception e) {
            e.printStackTrace();
            return "fail .......";
        }
    }
}
