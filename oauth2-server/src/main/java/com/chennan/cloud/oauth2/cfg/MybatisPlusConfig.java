package com.chennan.cloud.oauth2.cfg;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * mybatisPlus 配置类，配置信息位于application.yml中。
 * 结合了mybatisPlus-spring-boot-starter 后，自动配置结合。
 * 注解上配置扫描的bo类。
 * {@link @EnableTransactionManagement} 开启注解事务管理，等同于xml配置文件中的 <tx:annotation-driven />。
 * @author chen.nan
 */
@Configuration
@EnableTransactionManagement
@MapperScan(basePackages = "com.chennan.cloud.*.dao")
public class MybatisPlusConfig {

    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

}
