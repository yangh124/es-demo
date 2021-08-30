package com.yh.esdemo;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@SpringBootTest
class EsDemoApplicationTests {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 创建索引
     *
     * @throws Exception
     */
    @Test
    public void addIndex() throws Exception {
        IndicesClient indices = restHighLevelClient.indices();
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("zeda_customer");
        createIndexRequest.settings(
                Settings.builder()
                        .put("index.number_of_shards", 1)
                        .put("index.number_of_replicas", 0)
        );
        Map<String, Object> name = new HashMap<>();
        name.put("type", "text");
        name.put("analyzer", "ik_max_word");
        name.put("search_analyzer", "ik_smart");
        Map<String, Object> timestamp = new HashMap<>();
        timestamp.put("type", "date");
        timestamp.put("format", "yyyy-MM-dd HH:mm:ss");
        Map<String, Object> uuid = new HashMap<>();
        uuid.put("type", "keyword");
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", name);
        properties.put("timestamp", timestamp);
        properties.put("uuid", uuid);
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        //创建mapping
        createIndexRequest.mapping(mapping);
        //创建索引
        CreateIndexResponse createIndexResponse = indices.create(createIndexRequest, RequestOptions.DEFAULT);
        System.out.println(createIndexResponse.isAcknowledged());
        System.out.println(createIndexResponse.index());
    }

    /**
     * 删除索引
     *
     * @throws Exception
     */
    @Test
    public void delIndex() throws Exception {
        IndicesClient indices = restHighLevelClient.indices();
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("zeda_customer");
        AcknowledgedResponse deleteIndexResponse = indices.delete(deleteIndexRequest, RequestOptions.DEFAULT);
        System.out.println(deleteIndexResponse.isAcknowledged());
        System.out.println(deleteIndexResponse.isAcknowledged());
    }

    /**
     * 添加数据
     *
     * @throws Exception
     */
    @Test
    public void addDoc() throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = simpleDateFormat.format(new Date());
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("uuid", "534523dqw4qweq24324");
        jsonMap.put("name", "杨皓");
        jsonMap.put("timestamp", timestamp);
        IndexRequest indexRequest = new IndexRequest("zeda_customer").id("1").source(jsonMap);
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(indexResponse.toString());
    }
}
