package com.yh.esdemo;

import com.alibaba.fastjson.JSONObject;
import com.yh.esdemo.domain.Customer;
import org.elasticsearch.action.ActionListener;
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
import org.elasticsearch.client.Cancellable;
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
import java.util.concurrent.TimeUnit;
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
        //获取 IndicesClient ，用于创建 Index
        IndicesClient indices = restHighLevelClient.indices();
        //定义一个 CreateIndexRequest ，参数为索引的名称
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("yh_customer");
        //CreateIndexRequest配置相关参数 number_of_shards：分片数  number_of_replicas：每个分片的副本数
        createIndexRequest.settings(
                Settings.builder()
                        .put("index.number_of_shards", 1)
                        .put("index.number_of_replicas", 0)
        );
        Map<String, Object> name = new HashMap<>();
        name.put("type", "text");
        name.put("analyzer", "ik_max_word");//指定分词器
        name.put("search_analyzer", "ik_smart");
        Map<String, Object> timestamp = new HashMap<>();
        timestamp.put("type", "date");
        timestamp.put("format", "yyyy-MM-dd HH:mm:ss");//指定时间格式
        Map<String, Object> uuid = new HashMap<>();
        uuid.put("type", "keyword");
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", name);
        properties.put("timestamp", timestamp);
        properties.put("uuid", uuid);
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        //创建mapping  mapping可以理解为此索引中数据的数据结构
        createIndexRequest.mapping(mapping);
        //创建索引 此方法同步返回结果  异步使用 createAsync
        CreateIndexResponse createIndexResponse = indices.create(createIndexRequest, RequestOptions.DEFAULT);
        //创建结果（true/false）
        System.out.println(createIndexResponse.isAcknowledged());
        //输出创建成功的索引名称
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
        DeleteIndexRequest deleteIndexRequest = new DeleteIndexRequest("yh_customer");
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
        jsonMap.put("name", "张三");
        jsonMap.put("timestamp", timestamp);
        //创建 IndexRequest 指定id
        IndexRequest indexRequest = new IndexRequest("yh_customer").id("1").source(jsonMap);
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(indexResponse);
    }


    /**
     * 批量添加
     */
    @Test
    public void bulkTest() {
        //批量添加使用 BulkRequest
        BulkRequest bulkRequest = new BulkRequest();
        for (int i = 0; i < 100; i++) {
            Customer customer = new Customer(UUID.randomUUID().toString().replace("-", ""), i + "号客户", LocalDateTime.now());
            //String data = JSONObject.toJSONString(customer);
            String data = JSONObject.toJSONStringWithDateFormat(customer, "yyyy-MM-dd HH:mm:ss");
            IndexRequest request = new IndexRequest("yh_customer").source(data, XContentType.JSON);
            bulkRequest.add(request);
        }
        //同步
//        try {
//            BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
//            boolean hasFailures = bulk.hasFailures();
//            if (hasFailures) {
//                List<BulkItemResponse> collect = Arrays.stream(bulk.getItems()).filter(BulkItemResponse::isFailed).collect(Collectors.toList());
//                System.out.println(collect.size());
//                String msg = bulk.buildFailureMessage();
//                System.out.println(msg);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //异步
        restHighLevelClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
            //成功回调
            @Override
            public void onResponse(BulkResponse bulkItemResponses) {
                System.out.println("success");
            }

            //失败回调
            @Override
            public void onFailure(Exception e) {
                System.out.println("failure");
                e.printStackTrace();
            }
        });

        try {
            TimeUnit.MINUTES.sleep(5);
        } catch (InterruptedException e) {
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
        UpdateRequest updateRequest = new UpdateRequest("yh_customer", "1");
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name", "李四");
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
        GetRequest getRequest = new GetRequest("yh_customer");
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
        GetRequest getRequest = new GetRequest("yh_customer");
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
        GetSourceRequest getRequest = new GetSourceRequest("yh_customer", "1");
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
        SearchRequest searchRequest = new SearchRequest("yh_customer");//没有参数，查询所有索引
        //构建搜索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "1"));
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //返回命中数据
        SearchHits hits = search.getHits();
        for (SearchHit hit : hits.getHits()) {
            String sourceAsString = hit.getSourceAsString();
            Customer customer = JSONObject.parseObject(sourceAsString, Customer.class);
            System.out.println(customer);
        }
    }
}
