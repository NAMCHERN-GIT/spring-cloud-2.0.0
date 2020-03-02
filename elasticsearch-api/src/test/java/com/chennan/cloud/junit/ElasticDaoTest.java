package com.chennan.cloud.junit;

import com.alibaba.fastjson.JSONObject;
import com.chennan.cloud.es.base.dao.common.ElasticDao;
import com.chennan.cloud.es.base.vo.Page;
import com.chennan.cloud.junit.bo.Book;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.client.RequestOptions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ElasticDaoTest {

    @Autowired private ElasticDao elasticDao;

    /**
     * 测试客户端获取
     */
    @Test
    public void testGetClient() throws IOException {
        String clusterName = elasticDao.getClient().cluster().health(new ClusterHealthRequest(), RequestOptions.DEFAULT).getClusterName();
        Assert.assertEquals("anchivasoc", clusterName);
    }

    /**
     * 测试 索引是否存在方法
     */
    @Test
    public void testExistsIndex() throws IOException{
        boolean exists = elasticDao.existsIndex("book");
        Assert.assertTrue(exists);
    }

    /**
     * 测试 创建索引方法
     */
    @Test
    public void testCreateIndex() throws IOException{
        boolean isSuccess = elasticDao.createIndex("book");
        Assert.assertFalse(isSuccess);
    }

    /**
     * 测试插入object 对象数据
     */
    @Test
    public void testInsertObject() throws IOException, ParseException {
        Book book = new Book().setId("1000").setAuthor("葛一鸣/郭超")
                .setBookName("Java 高并发程序设计").setEditionNumber("2015年11月第1版")
                .setPrice(new BigDecimal("69.00")).setPublisher("电子工业出版社")
                .setPublicationDate(DateUtils.parseDate("2017-10-10", "yyyy-MM-dd"))
                .setWordCount(493_000).setCreateTime(Instant.now());
        boolean flag = elasticDao.insert("book", book);
        Assert.assertTrue(flag);
    }

    /**
     * 测试插入json数据
     */
    @Test
    public void testInsertJSONObject() throws IOException, ParseException {
        JSONObject data = new JSONObject();
        data.fluentPut("id", "1001").fluentPut("author", "翟永超")
            .fluentPut("bookName", "Spring Cloud 微服务实战").fluentPut("editionNumber", "2017年5月第一版")
            .fluentPut("price", new BigDecimal("89.89")).fluentPut("publisher", "电子工业出版社")
            .fluentPut("publicationDate", DateUtils.parseDate("2018-10-08", "yyyy-MM-dd"))
            .fluentPut("wordCount", 586_1000).put("createTime", Instant.now());
        boolean flag = elasticDao.insert("book", data);
        Assert.assertTrue(flag);
    }

    /**
     * 测试插入 json字符串数据
     */
    @Test
    public void testInsertJSONObjectString() throws IOException, ParseException {
        JSONObject data = new JSONObject();
        data.fluentPut("id", "1002").fluentPut("author", "罗刚君")
                .fluentPut("bookName", "Excel VBA 程序开发自学宝典").fluentPut("editionNumber", "2014年9月第3版")
                .fluentPut("price", new BigDecimal("75.00")).fluentPut("publisher", "电子工业出版社")
                .fluentPut("publicationDate", DateUtils.parseDate("2015-01-04", "yyyy-MM-dd"))
                .fluentPut("wordCount", 930_1000).put("createTime", Instant.now());
        boolean flag = elasticDao.insert("book", data.toString());
        Assert.assertTrue(flag);
    }

    /**
     * 测试 根据索引和id查询文档
     */
    @Test
    public void testGetJSONObject() throws IOException {
        Optional<JSONObject> optional = elasticDao.getJSONObject("book", "1002");
        if (optional.isPresent()){
            JSONObject data = optional.get();
            Assert.assertEquals("罗刚君", data.getString("author"));
        }
    }

    /**
     * 测试查询 查询索引下的所有的文档方法
     */
    @Test
    public void testListJSONObject() throws IOException {
        List<JSONObject> data = elasticDao.listJSONObject("book");
        Assert.assertFalse(data.isEmpty());
    }

    /**
     * 测试 查询分页数据
     */
    @Test
    public void testListPageJSONObject() throws IOException {
        Page<JSONObject> page = elasticDao.listPageJSONObject(1, 5, "book");
        Assert.assertEquals(5, page.getData().size());
    }

    /**
     * 测试删除方法
     */
    @Test
    public void testDelete() throws IOException {
        boolean flag = elasticDao.delete("book", "4");
        Assert.assertTrue(flag);
    }

}