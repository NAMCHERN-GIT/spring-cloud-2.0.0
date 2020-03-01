package com.chennan.cloud.es.cfg;

import com.chennan.cloud.es.base.co.Host;
import com.chennan.cloud.es.base.co.HttpHosts;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * RestHighLevelClient Bean配置
 * @author chen.nan
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({HttpHosts.class})
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
    public RestHighLevelClient clusterNodeClient() {
        RestClientBuilder builder = RestClient.builder(httpHosts.getHosts().stream().map(Host::toHttpHost).toArray(HttpHost[]::new))
                .setRequestConfigCallback(cfgBuilder -> cfgBuilder.setConnectTimeout(httpHosts.getConnectTimeout()          == null ? -1 : httpHosts.getConnectTimeout())
                                                                  .setSocketTimeout(httpHosts.getSocketTimeout()            == null ? -1 : httpHosts.getSocketTimeout() )
                                                                  .setConnectionRequestTimeout(httpHosts.getSocketTimeout() == null ? -1 : httpHosts.getSocketTimeout() ));
        return new RestHighLevelClient(builder);
    }
}
