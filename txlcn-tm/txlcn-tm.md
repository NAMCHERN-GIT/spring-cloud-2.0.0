# txlcn-tm 服务端搭建
## 环境准备
### mysql数据库
* 创建数据库名称为tx-manager
* 创建表 t_tx_exception
```sql
CREATE TABLE `t_tx_exception`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `unit_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `mod_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  `transaction_state` tinyint(4) NULL DEFAULT NULL,
  `registrar` tinyint(4) NULL DEFAULT NULL,
  `remark` varchar(4096) NULL DEFAULT  NULL,
  `ex_state` tinyint(4) NULL DEFAULT NULL COMMENT '0 未解决 1已解决',
  `create_time` datetime(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;
```

## 开发步骤
### 1. pom依赖
```xml
    <dependencies>
        <dependency>
            <groupId>com.codingapi.txlcn</groupId>
            <artifactId>txlcn-tm</artifactId>
            <version>5.0.2.RELEASE</version>
        </dependency>
    </dependencies>
```
### 2.application.yml配置
[application.yml](src/main/resources/application.yml)
```yaml
server:
  port: 7970
spring:
  application:
    name: txlcn-tm
  datasource:
    hikari:
      driver-class-name: com.mysql.cj.jdbc.Driver
      jdbc-url: jdbc:mysql://127.0.0.1:3306/tx-manager?characterEncoding=UTF-8&useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC
      username: chennan
      password: 123456
  jpa:
    database-platform: org.hibernate.dialect.MySQL5InnoDBDialect  # 数据库方言
    hibernate:
      ddl-auto: validate
  redis:
    host: 192.168.232.151
    port: 6379
    password:
tx-lcn:
  manager:
    admin-key: 123456                           # TM后台登录密码
    host: 127.0.0.1                             # TM监听IP. 默认为 127.0.0.1
    port: 8070                                  # TxClient连接请求端口,TM监听Socket端口. 默认为 ${server.port} - 100
    heart-time: 30000                           # 心跳检测时间(ms). 默认为 300000
    dtx-time: 8000                              # 分布式事务执行总时间(ms). 默认为36000
    concurrent-level: 160                       # 事务处理并发等级. 默认为机器逻辑核心数5倍
    dtx-lock-time: ${tx-lcn.manager.dtx-time}   # 分布式事务锁超时时间 默认为-1，当-1时会用tx-lcn.manager.dtx-time的时间
    seq-len: 12                                 # 雪花算法的sequence位长度，默认为12位.
    ex-url-enabled: false                       # 异常回调开关。开启时请制定ex-url
    # ex-url: /provider/email-to/949035443@qq.com # 事务异常通知（任何http协议地址。未指定协议时，为TM提供内置功能接口）。默认是邮件通知
  logger:
    enabled: true
    driver-class-name: ${spring.datasource.hikari.driver-class-name}
    jdbc-url: ${spring.datasource.hikari.jdbc-url}
    username: ${spring.datasource.hikari.username}
    password: ${spring.datasource.hikari.password}
  message:
    netty:
      attr-delay-time: ${tx-lcn.manager.dtx-time}   # 参数延迟删除时间单位ms  默认为dtx-time值
```
### 3.SpringBoot 启动类
此处注意需要添加注解 <b>@EnableTransactionManagerServer</b>
```java
package com.chennan.cloud;

import com.codingapi.txlcn.tm.config.EnableTransactionManagerServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * spring boot 入口启动类
 * @see EnableTransactionManagerServer 开启事务管理服务
 * @author chen.nan 
 */
@EnableTransactionManagerServer
@SpringBootApplication
public class TxLcnTm {
    public static void main(String[] args) {
        SpringApplication.run(TxLcnTm.class, args);
    }
}
```