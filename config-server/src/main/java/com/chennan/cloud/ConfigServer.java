package com.chennan.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.cloud.context.config.annotation.RefreshScope;

/**
 * Config server 入口启动类
 * {@link @EnableConfigServer} 启动config server配置中心
 *
 * spring-cloud-bus 消息总线通知刷新接口 POST /actuator/bus-refresh
 *
 * 三种访问形式：
 *  1.http://127.0.0.1:3344/application-dev.yml
 *  2.http://127.0.0.1:3344/application/dev/master
 *  3.http://127.0.0.1:3344/master/application-dev.yml
 *
 * @author chen.nan
 *
 */
@SpringBootApplication
@EnableConfigServer
@RefreshScope
public class ConfigServer {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServer.class, args);
    }
}
