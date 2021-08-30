package com.yh.esdemo.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    @Bean
    public RestClient restClient() {
        String[] hostArr = esHostConfig.getHostArr();
        int size = hostArr.length;
        HttpHost[] httpHostArr = new HttpHost[size];
        for (int i = 0; i < size; i++) {
            String[] split = hostArr[i].split(":");
            httpHostArr[i] = new HttpHost(split[0], Integer.parseInt(split[1]), "http");
        }
        return RestClient.builder(httpHostArr).build();
    }
}
