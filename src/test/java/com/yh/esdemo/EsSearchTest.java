package com.yh.esdemo;

import com.alibaba.fastjson.JSONObject;
import com.yh.esdemo.domain.Staff;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;


/**
 * @author : yh
 * @date : 2021/9/1 21:07
 */
@SpringBootTest
public class EsSearchTest {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    /**
     * 简单搜索
     * 查询所有
     *
     * @throws Exception
     */
    @Test
    public void simpleSearch() throws Exception {
        //创建搜索request
        SearchRequest searchRequest = new SearchRequest("bank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());//检索所有字段
        SearchRequest request = searchRequest.source(searchSourceBuilder);
        SearchResponse search = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        SearchHits hits = search.getHits();//命中条数
        System.out.println("总条数：" + hits.getTotalHits().value);
        for (SearchHit hit : hits) {//默认只检索出10条
            Staff staff = JSONObject.parseObject(hit.getSourceAsString(), Staff.class);
            System.out.println(staff);
        }
    }

    /**
     * 聚合搜索
     *
     * @throws Exception
     */
    @Test
    public void aggSearch() throws Exception {
        //创建搜索request
        SearchRequest searchRequest = new SearchRequest("bank");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //查询条件
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        //统计各个年龄的平均工资
        searchSourceBuilder.aggregation(AggregationBuilders.terms("ageAgg").field("age").subAggregation(AggregationBuilders.avg("balanceAvgAgg").field("balance")));
        //所有人平均工资
        searchSourceBuilder.aggregation(AggregationBuilders.avg("allBalanceAvgAgg").field("balance"));
        //构建request
        SearchRequest request = searchRequest.source(searchSourceBuilder);
        //search
        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);
        //Aggregations aggregations = searchResponse.getAggregations();
        //Aggregation接口有很多实现类  Avg（平均值）
//        Avg avg = aggregations.get("balanceAgg");
//        System.out.println(avg.getValue());
        Aggregations aggregations = searchResponse.getAggregations();
        Terms ageAgg = aggregations.get("ageAgg");
        List<? extends Terms.Bucket> buckets = ageAgg.getBuckets();
        for (Terms.Bucket bucket : buckets) {//只查出了10条数据
            System.out.println("年龄:" + bucket.getKey());
            System.out.println("人数:" + bucket.getDocCount());
            Avg avg = bucket.getAggregations().get("balanceAvgAgg");
            System.out.println("平均工资:" + avg.getValue());
            System.out.println("=================================");
        }
        Avg avg = aggregations.get("allBalanceAvgAgg");
        System.out.println("所有人平均工资:" + avg.getValue());
    }

}
