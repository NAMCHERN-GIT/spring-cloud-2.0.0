# txlcn-tc 客户端搭建
## 开发步骤
### 1. pom依赖
```xml
    <!--tc-->
    <dependency>
        <groupId>com.codingapi.txlcn</groupId>
        <artifactId>txlcn-tc</artifactId>
        <version>5.0.2.RELEASE</version>
    </dependency>
    <!--tc与tm通讯-->
    <dependency>
        <groupId>com.codingapi.txlcn</groupId>
        <artifactId>txlcn-txmsg-netty</artifactId>
        <version>5.0.2.RELEASE</version>
    </dependency>
```
### 2.application.yml 添加配置
```yaml
# Tm项目地址。 默认是127.0.0.1:8070,如果再服务器上的话要改成对应的地址。
# 8070是Tm默认的监听端口，需要更改的话去看TM的配置文件信息，TM监听Socket端口. tx-lcn.manager.port=8070。默认是Tm的启动端口+100。虽然文档写的是-100.实际上是+100
tx-lcn:
  client:
    manager-address: 127.0.0.1:8070
```
### 3.SpringBoot 启动类添加注解 @EnableDistributedTransaction
```java
package com.chennan.cloud;

import com.codingapi.txlcn.tc.config.EnableDistributedTransaction;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * 服务提供者 Spring-boot 入口启动类
 * @author chen.nan
 */
@SpringBootApplication // SpringBoot应用启动标识
@EnableEurekaClient    // 开启eureka客户端注册
@EnableDiscoveryClient // 开启服务发现
@EnableCircuitBreaker  // 对hystrixR熔断机制的支持
@EnableDistributedTransaction   // 开启分布式事务
public class MscProviderHystrix_8001_App {

    public static void main(String[] args) {
        SpringApplication.run(MscProviderHystrix_8001_App.class, args);
    }

}
```

### 4.业务方法添加注解 @LcnTransaction 或者 @LccTransaction