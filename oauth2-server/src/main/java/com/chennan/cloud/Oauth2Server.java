package com.chennan.cloud;

import com.codingapi.txlcn.tc.config.EnableDistributedTransaction;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * 程序启动类
 * @see EnableEurekaClient 开启eureka客户端
 * @see EnableDistributedTransaction 开启分布式事务tc
 * @author chen.nan
 */
@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
@EnableDistributedTransaction
public class Oauth2Server {
    public static void main(String[] args) {
        SpringApplication.run(Oauth2Server.class, args);
    }
}
