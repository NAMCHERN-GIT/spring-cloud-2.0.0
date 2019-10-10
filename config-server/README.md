# Spring Cloud 配置服务中心
* 包含了集成 rabbitmq，spring-cloud-bus,git
## 1.pom文件内容如下
```xml
<dependencies>
        <!-- 配置服务中心 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
            <exclusions>
                <!-- 排除掉spring-boot默认内嵌tomcat -->
                <exclusion>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-starter-tomcat</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <!-- 引入jetty作为内嵌的web容器 -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-jetty</artifactId>
        </dependency>

        <!-- spring cloud bus 消息总线依赖 -->
        <!-- https://blog.csdn.net/qq_27828675/article/details/83505630 -->
        <!-- https://blog.51cto.com/12965378/2403913 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bus-amqp</artifactId>
        </dependency>

        <!-- 引入eureka客户端 -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

    </dependencies>
```
## 2.application.yml 配置文件内容如下
```yaml
#-------------------------------------->>* Config-Server 注册中心 *<<--------------------------------------#
server:
  port: 3344

spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        git:
          uri: https://github.com/NAMCHERN-GIT/spring-cloud-config.git
      label: master
  rabbitmq:
    host: rabbitmq
    port: 5672
    username: guest
    password: guest

eureka:
  client:
    register-with-eureka: true
    service-url:
      defaultZone: http://eureka1:8761/eureka/,http://eureka2:8762/eureka/,http://eureka3:8763/eureka/  # 集群配置


management:
  endpoints:
    web:
      exposure:
        include: bus-refresh

```

## 3.SpringBoot启动类内容如下
```java
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

```
