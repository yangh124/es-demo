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
     * ????????????
     *
     * @throws Exception
     */
    @Test
    public void addIndex() throws Exception {
        //?????? IndicesClient ??????????????? Index
        IndicesClient indices = restHighLevelClient.indices();
        //???????????? CreateIndexRequest ???????????????????????????
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("yh_customer");
        //CreateIndexRequest?????????????????? number_of_shards????????????  number_of_replicas???????????????????????????
        createIndexRequest.settings(
                Settings.builder()
                        .put("index.number_of_shards", 1)
                        .put("index.number_of_replicas", 0)
        );
        Map<String, Object> name = new HashMap<>();
        name.put("type", "text");
        name.put("analyzer", "ik_max_word");//???????????????
        name.put("search_analyzer", "ik_smart");
        Map<String, Object> timestamp = new HashMap<>();
        timestamp.put("type", "date");
        timestamp.put("format", "yyyy-MM-dd HH:mm:ss");//??????????????????
        Map<String, Object> uuid = new HashMap<>();
        uuid.put("type", "keyword");
        Map<String, Object> properties = new HashMap<>();
        properties.put("name", name);
        properties.put("timestamp", timestamp);
        properties.put("uuid", uuid);
        Map<String, Object> mapping = new HashMap<>();
        mapping.put("properties", properties);
        //??????mapping  mapping????????????????????????????????????????????????
        createIndexRequest.mapping(mapping);
        //???????????? ???????????????????????????  ???????????? createAsync
        CreateIndexResponse createIndexResponse = indices.create(createIndexRequest, RequestOptions.DEFAULT);
        //???????????????true/false???
        System.out.println(createIndexResponse.isAcknowledged());
        //?????????????????????????????????
        System.out.println(createIndexResponse.index());
    }

    /**
     * ????????????
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
     * ????????????????????????
     *
     * @throws Exception
     */
    @Test
    public void addDoc() throws Exception {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timestamp = simpleDateFormat.format(new Date());
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("uuid", "534523dqw4qweq24324");
        jsonMap.put("name", "??????");
        jsonMap.put("timestamp", timestamp);
        //?????? IndexRequest ??????id
        IndexRequest indexRequest = new IndexRequest("yh_customer").id("1").source(jsonMap);
        IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        System.out.println(indexResponse);
    }


    /**
     * ????????????
     */
    @Test
    public void bulkTest() {
        //?????????????????? BulkRequest
        BulkRequest bulkRequest = new BulkRequest();
        for (int i = 0; i < 100; i++) {
            Customer customer = new Customer(UUID.randomUUID().toString().replace("-", ""), i + "?????????", LocalDateTime.now());
            //String data = JSONObject.toJSONString(customer);
            String data = JSONObject.toJSONStringWithDateFormat(customer, "yyyy-MM-dd HH:mm:ss");
            IndexRequest request = new IndexRequest("yh_customer").source(data, XContentType.JSON);
            bulkRequest.add(request);
        }
        //??????
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

        //??????
        restHighLevelClient.bulkAsync(bulkRequest, RequestOptions.DEFAULT, new ActionListener<BulkResponse>() {
            //????????????
            @Override
            public void onResponse(BulkResponse bulkItemResponses) {
                System.out.println("success");
            }

            //????????????
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
     * ????????????
     *
     * @throws Exception
     */
    @Test
    public void updateDoc() throws Exception {
        UpdateRequest updateRequest = new UpdateRequest("yh_customer", "1");
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("name", "??????");
        updateRequest.doc(jsonMap);
        UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        System.out.println(update);
    }

    /**
     * ??????????????????
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
     * ????????????
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
     * ????????????
     * ??????source
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
     * ??????
     *
     * @throws Exception
     */
    @Test
    public void search() throws Exception {
        SearchRequest searchRequest = new SearchRequest("yh_customer");//?????????????????????????????????
        //??????????????????
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("name", "1"));
        searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //??????????????????
        SearchHits hits = search.getHits();
        for (SearchHit hit : hits.getHits()) {
            String sourceAsString = hit.getSourceAsString();
            Customer customer = JSONObject.parseObject(sourceAsString, Customer.class);
            System.out.println(customer);
        }
    }
}
