package com.chennan.cloud.doc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chennan.cloud.junit.bo.Book;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;

/**
 * elasticsearch 单元测试
 */
@Slf4j
public class ElasticsearchTest {

    private RestHighLevelClient client  = null;
    private String bookIndexName        = null;
    private final static String hostname = "192.168.232.110";
    private final static Integer port = 9200;
    private final static String schema = "http";


    @Before public void init(){
        client = new RestHighLevelClient(RestClient.builder(new HttpHost(hostname, port, schema)));
        bookIndexName = Book.class.getSimpleName().toLowerCase();
    }

    @After public void destory(){
        try { client.close(); } catch (IOException e) { log.error(e.getMessage(), e); }
    }

    /**
     * 创建索引 书籍
     */
    @Test public void testCreateIndex() throws IOException {
        if (!existsIndex(bookIndexName))
            createIndex(bookIndexName);
    }

    /**
     * 测试插入数据
     */
    @Test public void testAddData() throws IOException {
        Book book = new Book().setId("1").setAuthor("罗利民").setBookName("从Docker到Kubernetes入门到实践")
                .setEditionNumber("2019年9月第1版").setPrice(new BigDecimal("69.00"))
                .setPublisher("清华大学出版社").setPublicationDate(new Date()).setWordCount(384_000).setCreateTime(Instant.now());
        addData(bookIndexName, book);
    }

    /**
     * 创建索引
     */
    private void createIndex(String index) throws IOException {
        CreateIndexRequest request = new CreateIndexRequest(index);
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        log.info("create index【{}】,desc:\n{}", index, JSON.toJSONString(response, SerializerFeature.PrettyFormat));
    }

    /**
     * 判断索引是否存在
     */
    private boolean existsIndex(String index) throws IOException {
        GetIndexRequest request = new GetIndexRequest(index);
        return client.indices().exists(request, RequestOptions.DEFAULT);
    }

    /**
     * 插入数据
     */
    private void addData(String index, Book book) throws IOException {
        IndexRequest indexRequest = new IndexRequest(index).id(book.getId());
        indexRequest.source(JSON.toJSONString(book), XContentType.JSON);
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        log.info("insert index【{}】 data ,desc:\n{}", index, JSON.toJSONString(indexResponse, SerializerFeature.PrettyFormat));
    }




}
