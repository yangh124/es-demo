---
title: ElasticSearch
categories:
- 进阶
  tags:
- Java
---
# 简介

Elasticsearch是一个基于Lucene的搜索服务器，他提供了一个分布式全文搜索引擎，基于restful web接口。

Elasticsearch是用Java语言开发的，基于Apache协议的开源项目，是目前最受欢迎的企业搜索引擎。Elasticsearch广泛运用于云计算中，能够达到实时搜索，具有稳定，可靠，快速的特点。
<!--more-->
# 安装

Elasticsearch，Kibana，IKAnalyzer中文分词器。版本要一致

# 相关概念

1. Never Realation（近实时）：Elasticsearch是一个近乎实时的搜索平台，这意味着从索引文档到可搜索文档之间只有轻微的延迟（通常是一秒钟）。
2. Cluster（集群）：集群是一个或多个节点的集合，它们一起保存整个数据，并提供所有节点的联合索引和搜索功能。每个集群都有自己的唯一集群名称，节点通过名称加入集群。
3. Node（节点）：节点是指属于集群的单个Elasticsearch实例，存储数据并参与集群的索引的搜索功能。可以将节点配置为按集群名称加入特定集群，默认情况下，每个节点都设置为加入一个名为elasticsearch的集群。
4. **Index（索引）**：索引是一些具有相似特征的文档集合，类似于Mysql中数据库的概念。
5. **Type（类型）**：类型是索引的逻辑类别分区，通常，为具有一组公共字段的文档类型，类似Mysql中表的概念。（在ES6及之后的版本，一个索引只能包含一个类型，ES7中标记为过时的，ES8中将移除Type）。
6. **Document（文档）**：文档是可被搜索的基本信息单位，以JSON格式表示，类似于Mysql中的行。
7. Shards（分片）：当索引存储大量数据时，可能会超出当个节点的硬件设置，为了解决这个问题，ES提供了将索引细分为分片的概念。分片机制赋予了索引水平扩容的能力、并允许跨分片分发和并行化操作，从而提高性能和吞吐量。
8. Replicas（副本）：在可能出现故障的网络的环境中，需要有一个故障切换机制，ES提供了将索引的分片复制为一个或多个副本的功能，副本在某些节点失效的情况下提供高可用性。

# 集群状态查看

1. 查看集群健康状态
```
GET /_cat/health?v
```
2. 查看节点状态
```
GET /_cat/nodes?v
```
3. 查看所有索引信息
```
GET /_cat/indices?v
```
# 索引操作

1. 创建索引并查看
```
PUT /customer GET /_cat/indices?v
```
2. 删除索引
```
DELETE /customer
```
# 类型操作

* 查看文档的类型
```
GET /bank/_mapping
```
```
{
  "bank": {
    "mappings": {
      "account": {
        "properties": {
          "account_number": {
            "type": "long"
          },
          "address": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "age": {
            "type": "long"
          },
          "balance": {
            "type": "long"
          },
          "city": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "email": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "employer": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "firstname": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "gender": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "lastname": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "state": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          }
        }
      }
    }
  }
}
```
# 文档操作

2. 在索引中添加文档
```
PUT /customer/doc/1GET
{
    "name":"yanghao"
}
```
```
{
  "_index": "customer",
  "_type": "doc",
  "_id": "1",
  "_version": 1,
  "result": "created",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  },
  "_seq_no": 3,
  "_primary_term": 1
}
```
3. 查看索引中的文档
```
GET /customer/doc/1
```
```
{
  "_index": "customer",
  "_type": "doc",
  "_id": "1",
  "_version": 2,
  "found": true,
  "_source": {
    "name": "yanghao"
  }
}
```
4. 修改索引中的文档
```
POST /customer/doc/1/_update
{
    "doc":{"name":"yanghao dashuaige"}
}
```
```
{
  "_index": "customer",
  "_type": "doc",
  "_id": "1",
  "_version": 2,
  "result": "updated",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  },
  "_seq_no": 4,
  "_primary_term": 1
}
```
5. 删除索引中的文档
```
DELETE /customer/doc/1
```
```
{
  "_index": "customer",
  "_type": "doc",
  "_id": "1",
  "_version": 3,
  "result": "deleted",
  "_shards": {
    "total": 2,
    "successful": 1,
    "failed": 0
  },
  "_seq_no": 2,
  "_primary_term": 1
}
```
6. 对索引中的文档执行批量操作
```
POST /customer/doc/_bulk
{"index":{"_id":"1"}}
{"name":"xiaohong"}
{"index":{"_id":"2"}}
{"name":"xiaoming"}
```
```
{
  "took": 45,
  "errors": false,
  "items": [
    {
      "index": {
        "_index": "customer",
        "_type": "doc",
        "_id": "1",
        "_version": 3,
        "result": "updated",
        "_shards": {
          "total": 2,
          "successful": 1,
          "failed": 0
        },
        "_seq_no": 5,
        "_primary_term": 1,
        "status": 200
      }
    },
    {
      "index": {
        "_index": "customer",
        "_type": "doc",
        "_id": "2",
        "_version": 1,
        "result": "created",
        "_shards": {
          "total": 2,
          "successful": 1,
          "failed": 0
        },
        "_seq_no": 0,
        "_primary_term": 1,
        "status": 201
      }
    }
  ]
}
```
# 数据搜索

查询表达式（Query DSL）是一种非常灵活又富有表现力的查询语言，ES使用它可以以简单的JSON接口来实现丰富的搜索功能。

## 数据准备

1. 首先导入测试数据，数据结构如下/
```
{
    "account_number": 0,
    "balance": 16623,
    "firstname": "Bradshaw",
    "lastname": "Mckenzie",
    "age": 29,
    "gender": "F",
    "address": "244 Columbus Place",
    "employer": "Euron",
    "email": "bradshawmckenzie@euron.com",
    "city": "Hobucken",
    "state": "CO"
}
```
2. 然后使用批量操作来导入数据（使用Kibana的Dev Tools操作）
```
POST /bank/account/_bulk
{
  "index": {
    "_id": "1"
  }
}
{
  "account_number": 1,
  "balance": 39225,
  "firstname": "Amber",
  "lastname": "Duke",
  "age": 32,
  "gender": "M",
  "address": "880 Holmes Lane",
  "employer": "Pyrami",
  "email": "amberduke@pyrami.com",
  "city": "Brogan",
  "state": "IL"
}
......省略若干条数据
```
3. 导入完成查看索引信息，可以发现bank索引中已经创建了1000条文档

## 搜索入门

1. 最简单的搜索
   ``
   GET /bank/_search
   {
   "query":{ "match_all":{} }
   }
   ``
2. 分页搜索
```
GET /bank/_search
{
    "query":{ "match_all":{} },
    "from":0,
    "size":10
}
```
3. 搜索排序，使用sort
```
GET /bank/_search
{
    "query":{ "match_all":{} },
    "sort":{"balance":{"order":"desc"}} #按照balance字段降序
}
```
4. 搜索并返回指定字段内容，使用_source 表示
```
GET /bank/_search
{
    "query":{ "match_all":{} },
    "_source":["account_number","balance"] #只返回account_number，balance字段
}
```
## 条件搜索

1. 条件搜索，使用match表示匹配条件
```
GET /bank/_search
{
    "query":{
        "match":{
            "account_number":20
        }
    }
}
```
2. 文本类型字段的条件搜索，对比上一条可以发现，对于数字类型的字段进行的是精确匹配，而对于文本使用的是模糊匹配
```
GET /bank/_search
{
  "query": {
    "match": {
      "address": "mill"
    }
  },
  "_source": [
    "address",
    "account_number"
  ]
}
```
3. 短语匹配搜索，使用match_phrase
```
GET /bank/_search
{
  "query": {
    "match_phrase": {
      "address": "mill lane"
    }
  }
}
```
**组合搜索**

1. 组合搜索，使用bool来进行组合，must表示必须同时满足
```
GET /bank/_search
{
  "query": {
    "bool": {
      "must": [
      #同时包含mill lane
        { "match": { "address": "mill" } },
        { "match": { "address": "lane" } }
      ]
    }
  }
}
```
2. 组合搜索，使用should，表示只要满足其中一个
```
GET /bank/_search
{
  "query": {
    "bool": {
      "should": [
        { "match": { "address": "mill" } },
        { "match": { "address": "lane" } }
      ]
    }
  }
}
```
3. 组合搜索，must_not表示必须同时不满足
```
GET /bank/_search
{
  "query": {
    "bool": {
      "must_not": [
        { "match": { "address": "mill" } },
        { "match": { "address": "lane" } }
      ]
    }
  }
}
```
4. 组合搜索，组合must和must_not
```
GET /bank/_search
{
  "query": {
    "bool": {
      "must": [
        { "match": { "age": "40" } }
      ],
      "must_not": [
        { "match": { "state": "ID" } }
      ]
    }
  }
}
```
## 搜索聚合

1. 对搜索结果进行聚合，使用aggs表示，类似于Mysql中的group by ，例如对state字段进行聚合，统计出不同state的文档数量。
```
GET /bank/_search
{
    "size":0,
    "aggs":{
        "group_by_state":{
            "terms":{
                "field":"state.keyword"
            }
        }
    }
}
```
2. 嵌套聚合，例如对state进行聚合，统计出不同state的文档数量，再统计出blance的平均值。
```
GET /bank/_search
{
  "size": 0,
  "aggs": {
    "group_by_age": {
      "terms": {
        "field": "state.keyword"
      }
    },
    "avg_blance": {
      "avg": {
        "field": "age"
      }
    }
  }
}
```
3. 对聚合结果进行排序，例如按balance的平均值降序排序
```
GET /bank/_search
{
  "size": 0,
  "aggs": {
    "group_by_state": {
      "terms": {
        "field": "state.keyword",
        "order": {
          "average_balance": "desc"
        }
      },
      "aggs": {
        "average_balance": {
          "avg": {
            "field": "balance"
          }
        }
      }
    }
  }
}
```
4. 按字段的范围进行分段聚合，例如分段范围为age字段的[20,30] [30,40] [40,50]，之后按gender统计文档个数和balance的平均值。
```
GET /bank/_search
{
    "size": 0,
    "aggs": {
        "group_by_age": {
            "range": {
                "field": "age",
                "ranges": [
                    {
                        "from": 20,
                        "to": 30
                    },
                    {
                        "from": 30,
                        "to": 40
                    },
                    {
                        "from": 40,
                        "to": 50
                    }
                ]
            },
            "aggs": {
                "group_by_gender": {
                    "terms": {
                        "field": "gender.keyword"
                    },
                    "aggs": {
                        "average_balance": {
                            "avg": {
                                "field": "balance"
                            }
                        }
                    }
                }
            }
        }
    }
}
```
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
private RestHighLevelClient client;

@Data
class User {
    private String name;    
    private Integer age;    
    private String gender;
}
@Test
public void index() throws IOException {    
    IndexRequest indexRequest = new IndexRequest("users");    
    indexRequest.id("1");    
    // indexRequest.source("name","zhangsan","age",20,"gender","男");    
    User user = new User();    
    user.setName("zhangsan");    
    user.setAge(20);    
    user.setGender("男");    
    String json = JSON.toJSONString(user);    
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