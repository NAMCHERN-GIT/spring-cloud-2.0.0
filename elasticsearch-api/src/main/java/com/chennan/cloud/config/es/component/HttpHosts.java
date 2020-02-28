package com.chennan.cloud.config.es.component;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "elasticsearch-configuration")
public class HttpHosts {

    /**
     * 主机集群集合
     */
    private List<Host> hosts;

    @Data
    public static class Host {
        /**
         * 主机
         */
        private String  host;

        /**
         * 端口
         */
        private Integer port;

        /**
         * 模式/协议
         */
        private String  schema;

    }

}
