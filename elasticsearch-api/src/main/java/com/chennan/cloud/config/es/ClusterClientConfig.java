package com.chennan.cloud.config.es;

import com.chennan.cloud.config.es.component.HttpHosts;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * RestHighLevelClient Bean配置
 * @author chen.nan
 */
@Slf4j
@Configuration
public class ClusterClientConfig {

    private HttpHosts httpHosts;

    /**
     * 注入 yml 文件中的 hosts 配置
     */
    @Autowired
    public void setHttpHosts(HttpHosts httpHosts) {
        this.httpHosts = httpHosts;
    }

    /**
     * 由spring工厂提供bean，使用多例模式。
     * 由于是REST也就是http的；连接请求，所以每次使用后都需要释放连接客户端
     */
    @Scope("prototype")
    @Bean(name = "clusterNodeClient", destroyMethod = "close")
    public RestHighLevelClient clusterNodeClient(){
        return new RestHighLevelClient(RestClient.builder(httpHosts.getHosts().stream().map(host -> new HttpHost(host.getHost(), host.getPort(), host.getSchema())).toArray(HttpHost[]::new)));
    }
}
