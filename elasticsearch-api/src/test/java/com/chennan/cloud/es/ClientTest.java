package com.chennan.cloud.es;

import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ClientTest {

    @Autowired @Qualifier("clusterNodeClient")
    private RestHighLevelClient restHighLevelClient;

    @Test public void testClusterClient() throws IOException {
        GetIndexRequest request = new GetIndexRequest("book");
        boolean exists = restHighLevelClient.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println("索引 book   " + (exists ? "存在" : "不存在"));
    }

}
