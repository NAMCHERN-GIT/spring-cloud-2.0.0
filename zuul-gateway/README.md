# API网关
Zuul是Spring Cloud全家桶中的微服务API网关。

所有从设备或网站来的请求都会经过Zuul到达后端的Netflix应用程序。作为一个边界性质的应用程序，Zuul提供了动态路由、监控、弹性负载和安全功能。Zuul底层利用各种filter实现如下功能：

认证和安全 识别每个需要认证的资源，拒绝不符合要求的请求。
性能监测 在服务边界追踪并统计数据，提供精确的生产视图。
动态路由 根据需要将请求动态路由到后端集群。
压力测试 逐渐增加对集群的流量以了解其性能。
负载卸载 预先为每种类型的请求分配容量，当请求超过容量时自动丢弃。
静态资源处理 直接在边界返回某些响应。

## 开发步骤
### 1.pom 参考[POM](./pom.xml)
### 2.启动主程序
```java
package com.chennan.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * springboot 入口启动类
 * @see EnableZuulProxy 网关代理enable
 * @see EnableOAuth2Sso 开启单点登录
 *
 * @author chen.nan
 */
@SpringBootApplication
@EnableZuulProxy
@EnableOAuth2Sso
@EnableDiscoveryClient
public class ZuulGateway {
    public static void main(String[] args) {
        SpringApplication.run(ZuulGateway.class, args);
    }
}

```
### 3.开启防止跨站攻击
```java
package com.chennan.cloud.zuul;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * CSRF 防止跨站攻击
 * @author chen.nan
 */
@Configuration
@EnableWebSecurity
@Order(99)
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable();
    }
}

```
### 4.[application.yml配置](src/main/resources/application.yml)
```yaml
#----------------------------------------->* zuul-gateway 路由网关配置中心 *<-----------------------------------------#
server:
  port: 8000

spring:
  application:
    name: zuul-gateway

eureka:
  instance:
    instance-id: ${spring.application.name}
    prefer-ip-address: true
  client:
    service-url:
      # defaultZone: http://eureka1:8761/eureka/,http://eureka2:8762/eureka/,http://eureka3:8763/eureka/
      defaultZone: http://eureka:8761/eureka/
zuul:
  ignored-services: "*"
  routes:
    oauth2:
      path: /oauth2/**
      serviceId: oauth2-server
      sensitiveHeaders: "*"
    user:
      path: /user/**
      serviceId: user-server
      sensitiveHeaders: "*"
  add-proxy-headers: true

security:
  oauth2:
    client:
      access-token-uri: http://${spring.application.name}:${server.port}/oauth2/oauth/token            # 获取token令牌的地址
      user-authorization-uri: http://${spring.application.name}:${server.port}/oauth2/oauth/authorize  # 授权模式访问之授权码模式,参考 https://blog.csdn.net/u013887008/article/details/80616422
    resource:
      user-info-uri: http://${spring.application.name}:${server.port}/oauth2/api/userInfo
      prefer-token-info: false

info:
  app:
    name: ${eureka.instance.instance-id}
  company:
    name: www.chennan.com

# 在SpringCloud 2.0.0 以后，需要在bootstrap.yml中配置此项，才能手动刷新config-server上的配置，访问方式 post /actuator/refresh
management:
  endpoints:
    web:
      exposure:
        include: refresh,health

```