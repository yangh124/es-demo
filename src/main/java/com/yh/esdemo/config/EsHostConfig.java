package com.yh.esdemo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


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
