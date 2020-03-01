package com.chennan.cloud.es.base.annotation;

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
     * 所在的文档 type，elasticsearch type属性 在es 8.x版本可能要删除
     */
    String type() default "";

}
