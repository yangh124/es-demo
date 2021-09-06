package com.yh.esdemo;

import com.alibaba.fastjson.JSONObject;
import com.yh.esdemo.domain.Customer;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.core.GetSourceRequest;
import org.elasticsearch.client.core.GetSourceResponse;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


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
    }

    /**
     * 添加数据（单个）
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
        System.out.println(indexResponse);
    }


    /**
     * 批量添加
     */
    @Test
    public void bulkTest() {
        BulkRequest bulkRequest = new BulkRequest();
        for (int i = 0; i < 100; i++) {
            Customer customer = new Customer(UUID.randomUUID().toString().replace("-", ""), i + "号客户", LocalDateTime.now());
            //String data = JSONObject.toJSONString(customer);
            String data = JSONObject.toJSONStringWithDateFormat(customer, "yyyy-MM-dd HH:mm:ss");
            IndexRequest request = new IndexRequest("zeda_customer").source(data, XContentType.JSON);
            bulkRequest.add(request);
        }
        try {
            BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
            boolean hasFailures = bulk.hasFailures();
            if (hasFailures) {
                List<BulkItemResponse> collect = Arrays.stream(bulk.getItems()).filter(BulkItemResponse::isFailed).collect(Collectors.toList());
                System.out.println(collect.size());
                String msg = bulk.buildFailureMessage();
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 修改数据
     *
     * @throws Exception
     */
    @Test
    public void updateDoc() throws Exception {
        UpdateRequest updateRequest = new UpdateRequest("zeda_customer", "1");
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name", "杨皓大帅哥");
        updateRequest.doc(jsonMap);
        UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(update);
    }

    /**
     * 判断是否存在
     *
     * @throws Exception
     */
    @Test
    public void exists() throws Exception {
        GetRequest getRequest = new GetRequest("zeda_customer");
        getRequest.id("1");
        boolean exists = restHighLevelClient.exists(getRequest, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    /**
     * 获取数据
     *
     * @throws Exception
     */
    @Test
    public void get() throws Exception {
        GetRequest getRequest = new GetRequest("zeda_customer");
        getRequest.id("1");
        GetResponse getResponse = restHighLevelClient.get(getRequest, RequestOptions.DEFAULT);
        System.out.println(getResponse);
    }

    /**
     * 获取数据
     * 获取source
     *
     * @throws Exception
     */
    @Test
    public void getSource() throws Exception {
        GetSourceRequest getRequest = new GetSourceRequest("zeda_customer", "1");
        GetSourceResponse source = restHighLevelClient.getSource(getRequest, RequestOptions.DEFAULT);
        System.out.println(source);
    }

    /**
     * 搜索
     *
     * @throws Exception
     */
    @Test
    public void search() throws Exception {
        SearchRequest searchRequest = new SearchRequest("zeda_customer");//没有参数，查询所有索引
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "1"));
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits hits = search.getHits();
        for (SearchHit hit : hits.getHits()) {
            String sourceAsString = hit.getSourceAsString();
            Customer customer = JSONObject.parseObject(sourceAsString, Customer.class);
            System.out.println(customer);
        }
    }
}
