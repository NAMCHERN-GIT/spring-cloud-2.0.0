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
