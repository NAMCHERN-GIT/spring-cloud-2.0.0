package com.chennan.cloud;

import com.chennan.cloud.config.es.co.HttpHosts;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties({HttpHosts.class, HttpHosts.class})
@SpringBootApplication
public class EsApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(EsApiApplication.class, args);
    }
}
