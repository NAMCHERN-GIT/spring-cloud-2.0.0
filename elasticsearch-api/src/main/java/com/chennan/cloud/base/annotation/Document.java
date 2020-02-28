package com.chennan.cloud.base.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Document {

    /**
     * 实体对应的索引
     */
    String index() default "";

    /**
     * 所在的文档 type
     */
    String type() default "";

}
