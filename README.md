# Spring Boot整合ElasticSearch

使用ElasticSearch-Rest-Client对elasticsearch进行操作

引入依赖：
```
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
    <version>7.9.0</version>
</dependency>
```
因为SpringBoot管理了elasticsearch的依赖版本，所以我们需要指定一下elasticsearch的版本与其一致：
```
<properties>
    <elasticsearch.version>7.9.0</elasticsearch.version>
</properties>
```
然后编写一个配置类，向容器中注册一个操作elasticsearch的组件：
```
/**
 * @author : yh
 * @date : 2021/8/30 20:35
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "elasticsearch")
public class EsHostConfig {

    private String[] hostArr;
}

/**
 * 定义RestClient Bean
 *
 * @author : yh
 * @date : 2021/8/30 20:32
 */
@Configuration
public class EsRestClientConfig {

    @Autowired
    private EsHostConfig esHostConfig;

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        String[] hostArr = esHostConfig.getHostArr();
        int size = hostArr.length;
        HttpHost[] httpHostArr = new HttpHost[size];
        for (int i = 0; i < size; i++) {
            String[] split = hostArr[i].split(":");
            httpHostArr[i] = new HttpHost(split[0], Integer.parseInt(split[1]), "http");
        }
        return new RestHighLevelClient(RestClient.builder(httpHostArr));
    }
}

```
接下来我们就可以通过它操作 elasticsearch 了：
```
@Autowired
private RestHighLevelClient client;

@Data
class User {
    private String name;    
    private Integer age;    
    private String gender;
}
@Test
public void index() throws IOException {    
    IndexRequest indexRequest = new IndexRequest("users");    
    indexRequest.id("1");    
    // indexRequest.source("name","zhangsan","age",20,"gender","男");    
    User user = new User();    
    user.setName("zhangsan");    
    user.setAge(20);    
    user.setGender("男");    
    String json = JSON.toJSONString(user);    
    indexRequest.source(json, XContentType.JSON);    
    // 执行保存操作    
    IndexResponse index = client.index(indexRequest, RequestOptions.DEFAULT);    
    // 响应数据    
    System.out.println(index);
}
```
RestHighLevelClient提供了非常多的方式用于保存数据，但比较常用的是通过json数据直接保存，首先需要指定索引， IndexRequest indexRequest = new IndexRequest(&quot;users&quot;); 指定了users索引，然后指定数据id，接着指定数据值，最后使用client执行保存操作，然后可以拿到响应数据。

elasticsearch的其它简单操作，诸如：更新、删除等，都只需要转换一下调用方法即可，如更新操作，就需要使用client调用update方法，接下来我们看看Java程序该如何实现较为复杂的检索操作。

比如现在想聚合出年龄的分布情况，并求出每个年龄分布人群的平均薪资，就应该这样进行编写：
```
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
```
* 最终输出结果：
```
年龄:31
人数:61
平均工资:28312.918032786885
=================================
年龄:39
人数:60
平均工资:25269.583333333332
=================================
年龄:26
人数:59
平均工资:23194.813559322032
=================================
年龄:32
人数:52
平均工资:23951.346153846152
=================================
年龄:35
人数:52
平均工资:22136.69230769231
=================================
年龄:36
人数:52
平均工资:22174.71153846154
=================================
年龄:22
人数:51
平均工资:24731.07843137255
=================================
年龄:28
人数:51
平均工资:28273.882352941175
=================================
年龄:33
人数:50
平均工资:25093.94
=================================
年龄:34
人数:49
平均工资:26809.95918367347
=================================
所有人平均工资:25714.837

```