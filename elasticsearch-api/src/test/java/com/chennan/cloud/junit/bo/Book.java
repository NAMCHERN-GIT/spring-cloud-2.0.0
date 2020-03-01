package com.chennan.cloud.junit.bo;

import com.alibaba.fastjson.annotation.JSONField;
import com.chennan.cloud.es.base.annotation.Document;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

@Data
@ToString
@Accessors(chain = true)
@Document(index = "book")
public class Book implements Serializable {

    private String id;              // 主键

    private String bookName;        // 书名

    private String author;          // 作者

    private String serialNumber;    // 书籍序列号

    private String publisher;       // 出版社

    @JSONField(format = "yyyy-MM-dd")
    private Date publicationDate;   // 出版日期

    private String editionNumber;   // 版次

    private Integer wordCount;      // 字数

    private BigDecimal price;       // 定价

    private Instant createTime;        // 创建时间

}
