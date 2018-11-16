package com.pinyougou.search.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;

import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class DeleteItemSolrListener implements MessageListener {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            String goodsId = textMessage.getText();
            //同步删除索引库 根据goodsid
            SolrDataQuery query = new SimpleQuery("item_goodsid:" + goodsId);
            solrTemplate.delete(query);
            solrTemplate.commit();

        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
