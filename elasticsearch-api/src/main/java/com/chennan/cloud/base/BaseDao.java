package com.chennan.cloud.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chennan.cloud.base.annotation.Document;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

/**
 * 操作es的基类，为抽象类，子类必须继承它
 * @param <T> Bean的类型
 */
@Slf4j
@Repository
public abstract class BaseDao<T> {

    private RestHighLevelClient client;

    /**
     * 注入 RestHighLevelClient 客户端
     */
    @Autowired
    @Qualifier("clusterNodeClient")
    private void setClient(RestHighLevelClient client) {
        this.client = client;
    }

    public RestHighLevelClient getClient() {
        return client;
    }

    /**
     * 判断索引是否存在
     */
    public boolean existsIndex(String index) throws IOException {
        GetIndexRequest request = new GetIndexRequest(index);
        return client.indices().exists(request, RequestOptions.DEFAULT);
    }

    /**
     * 判断索引是否存在
     */
    public boolean existsIndex() throws IOException {
        return existsIndex(getDefaultIndex());
    }

    /**
     * 创建索引
     * @param index 索引名称
     * @return boolean 创建成功返回true，否则返回false
     */
    public boolean createIndex(String index) throws IOException {
        if (existsIndex(index)){
            log.warn("索引【{}】已存在!", index);
            return false;
        }
        CreateIndexRequest request = new CreateIndexRequest(index);
        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
        log.info("create index【{}】,desc:\n{}", index, JSON.toJSONString(createIndexResponse, SerializerFeature.PrettyFormat));
        return existsIndex(index);
    }

    /**
     * 创建索引，索引名称为实体类名小写
     * @return boolean 创建成功返回true，否则返回false
     */
    public boolean createIndex() throws IOException {
        return createIndex(getDefaultIndex());
    }

    /**
     * 插入数据
     * @param index 索引名称
     * @param t 对象
     */
    public void insert(String index, T t) throws IOException {
        JSONObject data = (JSONObject) JSON.toJSON(t);
        // 判断id
        boolean hasId = data.containsKey("id");
        if (!hasId)
            data.put("id", UUID.randomUUID().toString());
        else if (!(data.get("id") instanceof String))
            throw new RuntimeException("id 必须为 String 类型");
        IndexRequest indexRequest = new IndexRequest(index).id(data.getString("id"));
        indexRequest.source(JSON.toJSONString(data), XContentType.JSON);
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        log.info("insert index【{}】 data ,desc:\n{}", index, JSON.toJSONString(indexResponse, SerializerFeature.PrettyFormat));
    }

    /**
     * 插入数据
     * @param t 对象
     */
    public void insert(T t) throws IOException {
        insert(getDefaultIndex(), t);
    }

    /**
     * 获取默认的索引名称
     */
    public String getDefaultIndex(){
        // 获取Bean的类型
        Class beanClazz = getGenericClass();
        /* 根据注解获取索引名称 */
        // 获取Bean上所有的注解
        Annotation[] annotations = beanClazz.getAnnotations();
        // 查找 @com.chennan.cloud.base.annotation.Document 注解
        Optional<Annotation> docOptional = Arrays.stream(annotations).filter(ann -> ann instanceof Document).findFirst();
        // 若有 @com.chennan.cloud.base.annotation.Document 注解，则获取注解里面标注的索引名称
        if (docOptional.isPresent()){
            Document doc = (Document) docOptional.get();
            String index = doc.index();
            String type = doc.type();
            log.info("index is 【{}】，type is 【{}】", index,  type);
            if (StringUtils.isNotBlank(index)) return index;
        }
        // 默认索引名称,类名称转小写
        return beanClazz.getSimpleName().toLowerCase();
    }

    /**
     * 获取泛型 T 的类型Class
     */
    @SuppressWarnings("unchecked")
    private Class getGenericClass(){
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

}
