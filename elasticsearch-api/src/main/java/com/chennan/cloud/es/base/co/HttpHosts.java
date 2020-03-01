package com.chennan.cloud.es.base.co;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * ES 连接配置类
 */
@Data
@Component
@ConfigurationProperties(prefix = "elasticsearch-configuration")
public class HttpHosts {

    /**
     * 主机集群集合.
     */
    private List<Host> hosts;

    /**
     * 连接超时时间，单位毫秒.
     */
    private Integer connectTimeout;

    /**
     * 网络超时时间，单位毫秒.
     */
    private Integer socketTimeout;

    /**
     * 连接请求超时时间，单位毫秒.
     */
    private Integer connectionRequestTimeout;

}
