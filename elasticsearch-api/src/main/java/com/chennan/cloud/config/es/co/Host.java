package com.chennan.cloud.config.es.co;

import lombok.Data;
import org.apache.http.HttpHost;

@Data
public class Host {
    /**
     * 主机.
     */
    private String  hostname;

    /**
     * 端口.
     */
    private Integer port;

    /**
     * 模式/协议.
     */
    private String  schema;


    /**
     * 转为 HttpHost 对象.
     */
    public HttpHost toHttpHost(){
        return new HttpHost(hostname, port, schema);
    }
}
