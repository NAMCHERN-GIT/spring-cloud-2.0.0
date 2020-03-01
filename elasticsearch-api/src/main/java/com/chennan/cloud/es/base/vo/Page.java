package com.chennan.cloud.es.base.vo;

import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 分页查询封装结果
 * @param <T>
 */
@Data
@ToString
@Accessors(chain = true)
public class Page<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer current     ;   // 当前页码
    private Integer size        ;   // 每页大小
    private Long    total       ;   // 总记录数
    private Long    pageTotal   ;   // 总共页码
    private List<T> data        ;   // 每页数据

}
