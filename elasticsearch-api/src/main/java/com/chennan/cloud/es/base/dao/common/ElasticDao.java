package com.chennan.cloud.es.base.dao.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chennan.cloud.es.base.vo.Page;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ElasticDao {

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
        return client.indices().exists(new GetIndexRequest(index), RequestOptions.DEFAULT);
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
        CreateIndexResponse response = client.indices().create(new CreateIndexRequest(index), RequestOptions.DEFAULT);
        log.info("create index【{}】,desc:\n{}", index, JSON.toJSONString(response, SerializerFeature.PrettyFormat));
        return existsIndex(index);
    }

    /**
     * 插入数据
     * @param index     索引名称
     * @param obj       实体对象
     */
    public boolean insert(String index, Object obj) throws IOException {
        return insert(index, JSON.toJSONString(obj));
    }

    /**
     * 插入数据
     * @param index         索引名称
     * @param jsonObject    JSON对象
     */
    public boolean insert(String index, JSONObject jsonObject) throws IOException {
        return insert(index, jsonObject.toJSONString());
    }

    /**
     * 插入数据
     * @param index     索引名称
     * @param jsonData  json字符串
     */
    public boolean insert(String index, String jsonData) throws IOException {
        JSONObject data = JSON.parseObject(jsonData);
        // 判断id
        boolean hasId = data.containsKey("id");
        if (!hasId)
            data.put("id", UUID.randomUUID().toString());
        else if (!(data.get("id") instanceof String))
            throw new RuntimeException("id 必须为 String 类型");
        IndexRequest request = new IndexRequest(index).id(data.getString("id"));
        request.source(jsonData, XContentType.JSON);
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        log.info("insert index【{}】 data ,desc:\n{}", index, JSON.toJSONString(response, SerializerFeature.PrettyFormat));
        return response.status() == RestStatus.CREATED;
    }

    /**
     * 根据索引和id查询文档
     * @param index 索引名称
     * @param id  主键
     * @return T 返回查询数据
     */
    public Optional<JSONObject> getJSONObjectById(String index, String id) throws IOException {
        return get(getGetResponseById(index, id), JSONObject.class);
    }

    /**
     * 查询索引下的所有的文档
     * @param index 索引名称
     */
    public List<JSONObject> listJSONObject(String index) throws IOException {
        return listPageJSONObject(null, null, index).getData();
    }

    /**
     * 根据索引和多个id查询对应的文档
     * @param index 索引名称
     * @param ids   id数组
     * @return      List<JSONObject>
     */
    public List<JSONObject> multiGetJSONObject(String index, List<String> ids) throws IOException {
        return multiGet(getMultiGetResponse(index, ids), JSONObject.class);
    }

    /**
     * 查询分页数据
     * @param current   页码
     * @param size      每页条数
     * @param index     索引名称
     */
    public Page<JSONObject> listPageJSONObject(Integer current, Integer size, String index) throws IOException {
        return page(getSearchResponse(current, size, index), current, size, JSONObject.class);
    }

    /**
     * 根据id修改文档
     * @param index 索引名称
     * @param id    id
     * @param object 数据object
     * @return      boolean
     */
    public boolean updateById(String index, String id, Object object) throws IOException {
        JSONObject json = JSON.parseObject(JSON.toJSONString(object));
        UpdateResponse response = client.update(new UpdateRequest(index, id).doc(json), RequestOptions.DEFAULT);
        return RestStatus.OK == response.status();
    }

    /**
     * 根据id和索引删除文档
     * @param index 索引名称
     * @param id    主键
     * @return      是否成功
     */
    public boolean deleteById(String index, String id) throws IOException {
        DeleteResponse response = client.delete(new DeleteRequest(index, id), RequestOptions.DEFAULT);
        return response.status() == RestStatus.OK;
    }

    /**
     * 查询获取 GetResponse  对象
     * @param index         索引名称
     * @param id            id
     * @return              GetResponse
     */
    protected GetResponse getGetResponseById(String index, String id) throws IOException {
        return client.get(new GetRequest(index, id), RequestOptions.DEFAULT);
    }

    protected MultiGetResponse getMultiGetResponse(String index, List<String> ids) throws IOException {
        MultiGetRequest request = new MultiGetRequest();
        ids.forEach(id -> request.add(index, id));
        return client.mget(request, RequestOptions.DEFAULT);
    }

    /**
     *  查询 SearchResponse
     * @param current   当前页码
     * @param size      每页大小
     * @param index     索引
     * @return          SearchResponse
     */
    protected SearchResponse getSearchResponse(Integer current, Integer size, String index) throws IOException {
        SearchRequest searchRequest = new SearchRequest(index);
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        /* 当 current 和 size 都不为 null 的时候增加分页查询 */
        if (current != null && size != null) sourceBuilder.from((current - 1) * size).size(size);
        /* 默认按照id升序排列，使用“id.keyword”.因为id没有设置 fielddata:true 属性，原因是 text类型字段设置为true太消耗内存了 */
        sourceBuilder.sort(new FieldSortBuilder("id.keyword").order(SortOrder.ASC));
        searchRequest.source(sourceBuilder);
        return client.search(searchRequest, RequestOptions.DEFAULT);
    }

    /**
     * 将 GetResponse结果 转为 Optional
     * @param response GetResponse
     * @param clazz     类型
     * @param <T>       泛型
     * @return          Optional
     */
    protected static <T> Optional<T> get(GetResponse response, Class<T> clazz){
        return Optional.ofNullable(response.isExists() ? JSON.parseObject(response.getSourceAsString(), clazz) : null);
    }

    /**
     * 将 MultiGetResponse 结果 转为 List<T>
     * @param itemResponseList MultiGetResponse
     * @param clazz            类型
     * @param <T>              泛型
     * @return                 List<T>
     */
    protected static <T> List<T> multiGet(MultiGetResponse itemResponseList, Class<T> clazz){
        List<T> list = new ArrayList<>();
        itemResponseList.forEach(item -> {
            if (item.getResponse().isExists())
                list.add(JSON.parseObject(item.getResponse().getSourceAsString(), clazz ));
        });
        return list;
    }

    /**
     * 把 SearchResponse ， current， size， clazz 封装为分页对象
     * @param searchResponse    searchResponse
     * @param current           当前页码
     * @param size              每页大小
     * @param clazz             类型
     * @param <T>               泛型
     * @return                  Page<T> 分页对象
     */
    protected static <T> Page<T> page(SearchResponse searchResponse , Integer current, Integer size, Class<T> clazz){
        if (searchResponse.status() != RestStatus.OK )
            throw new RuntimeException("search error !");
        SearchHits searchHits = searchResponse.getHits();
        List<T> data = Arrays.stream(searchHits.getHits()).map(hit -> JSON.parseObject(hit.getSourceAsString(), clazz)).collect(Collectors.toList());
        Page<T> page = new Page<T>().setData(data);
        if (current != null && size != null){
            long total = searchHits.getTotalHits().value;
            long pageTotal = total / size ;
            if (total % size > 0) pageTotal ++ ;
            page.setCurrent(current);   // 当前页
            page.setSize(size);         // 每页大小
            page.setTotal(total);
            page.setPageTotal(pageTotal);
        }
        return page;
    }
}
