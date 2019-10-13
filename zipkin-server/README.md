# zipkin 服务链路追踪服务中心搭建
## 一、简介
　　Zipkin 是一个开放源代码分布式的跟踪系统，每个服务向zipkin报告计时数据，zipkin会根据调用关系通过Zipkin UI生成依赖关系图。  
　　Zipkin提供了可插拔数据存储方式：In-Memory、MySql、Cassandra以及Elasticsearch。为了方便在开发环境我直接采用了In-Memory
方式进行存储，生产数据量大的情况则推荐使用Elasticsearch。  

## 二、服务端开发
### 2.1 pom依赖
```xml
    <dependencies>
        <!-- 引入 zipkin-server 依赖 -->
        <dependency>
            <groupId>io.zipkin.java</groupId>
            <artifactId>zipkin-server</artifactId>
        </dependency>
        <!-- 引入zipkin autoconfigure-ui 依赖 -->
        <dependency>
            <groupId>io.zipkin.java</groupId>
            <artifactId>zipkin-autoconfigure-ui</artifactId>
        </dependency>
    </dependencies>
```
### 2.2 yml 配置文件内容
```yaml
#-------------------------------------->>* zipkin-server 服务链路追踪服务 *<<--------------------------------------#
# 设置zipkin访问端口协议
armeria:
  ports:
    - port: 9000
      protocols:
        - http

# 必须设置属性 server.compression.enabled = true
server:
  compression:
    enabled: true

# 必须设置属性 spring.main.web-application-type = none
# 参考 See https://blog.csdn.net/chenglu6516/article/details/100698050
# 参考 See https://docs.spring.io/spring-boot/docs/current/reference/html/howto-embedded-web-servers.html.
# 参考 See https://github.com/line/armeria-examples/blob/master/spring-boot-minimal/src/main/resources/config/application.yml
spring:
  application:
    name: zipkin-server
  main:
    web-application-type: none

```

### 2.3 SpringBoot 启动类
```java
package com.chennan.cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import zipkin2.server.internal.EnableZipkinServer;

/**
 * spring-boot 程序入口启动类
 * @see EnableZipkinServer 开启zipkin 服务中心功能
 */
@EnableZipkinServer
@SpringBootApplication
public class ZipkinServer {
    public static void main(String[] args) {
        SpringApplication.run(ZipkinServer.class, args);
    }
}

```

## 三、客户端开发
### 3.1 pom 依赖引入
```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-sleuth-zipkin</artifactId>
        </dependency>
```
### 3.2 yml 配置
```yaml
spring:
  ## zipkin 链路追踪，客户端配置
  zipkin:
    base-url: http://zipkin-server:9000
  sleuth:
    sampler:
      probability: 1.0
```
