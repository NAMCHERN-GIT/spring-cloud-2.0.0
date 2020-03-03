package com.chennan.cloud.es.base.dao.abs;

import com.chennan.cloud.es.base.annotation.Document;
import com.chennan.cloud.es.base.dao.common.ElasticDao;
import com.chennan.cloud.es.base.vo.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 操作es的基类，为抽象类，子类必须继承它
 * @param <T> Bean的类型
 */
@Slf4j
@Repository
public abstract class BaseDao<T> extends ElasticDao {

    /**
     * 获取 RestHighLevelClient 客户端
     * @return RestHighLevelClient
     */
    public RestHighLevelClient getClient() {
        return super.getClient();
    }

    /**
     * 判断索引是否存在
     */
    public boolean existsIndex() throws IOException {
        return existsIndex(getDefaultIndex());
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
     * @param t 对象
     */
    public boolean insert(T t) throws IOException {
        return insert(getDefaultIndex(), t);
    }

    /**
     * 根据id查询文档
     * @param id  主键
     * @return Optional<T> 返回查询数据
     */
    public Optional<T> get(String id) throws IOException {
        return get(getGetResponse(getDefaultIndex(), id), getGenericClass());
    }

    /**
     * 根据多个id查询文档
     * @param ids id集合
     * @return List<T>
     */
    public List<T> multiGet(List<String> ids) throws IOException {
        return multiGet(getMultiGetResponse(getDefaultIndex(), ids), getGenericClass());
    }

    /**
     * 查询不带条件
     * @param current   页码
     * @param size      每页条数
     */
    public Page<T> listPage(Integer current, Integer size) throws IOException {
        return page(getSearchResponse(current, size, getDefaultIndex()), current, size, getGenericClass());
    }

    /**
     * 查询实体对应索引下的所有数据
     */
    public List<T> list() throws IOException {
        return listPage(null, null).getData();
    }

    /**
     * 根据id删除文档
     * @param id    主键
     * @return      是否成功
     */
    public boolean delete(String id) throws IOException {
        return delete(getDefaultIndex(), id);
    }

    /**
     * 获取默认的索引名称
     */
    private String getDefaultIndex(){
        // 获取Bean的类型
        Class<T> beanClazz = getGenericClass();
        /* 根据注解获取索引名称 */
        // 获取Bean上所有的注解
        Annotation[] annotations = beanClazz.getAnnotations();
        // 查找 @com.chennan.cloud.es.base.annotation.Document 注解
        Optional<Annotation> docOptional = Arrays.stream(annotations).filter(ann -> ann instanceof Document).findFirst();
        // 若有 @com.chennan.cloud.es.base.annotation.Document 注解，则获取注解里面标注的索引名称
        if (docOptional.isPresent()){
            Document doc = (Document) docOptional.get();
            String index = doc.index();
            String type = doc.type();   // type属性 在es 8.x版本可能要删除
            if (StringUtils.isBlank(index))
                throw new RuntimeException(String.format("类【%s】的注解【@com.chennan.cloud.es.base.annotation.Document】中 [index]不能为空!", beanClazz.getName()));
            log.info("index is 【{}】，type is 【{}】", index,  type);
            if (StringUtils.isNotBlank(index)) return index;
        }
        // 默认索引名称,类名称首字母转小写
        String sampleName = beanClazz.getSimpleName();
        return sampleName.substring(0,1).toLowerCase().concat(sampleName.substring(1).toLowerCase());
    }

    /**
     * 获取泛型 T 的 Class
     */
    @SuppressWarnings("unchecked")
    private Class<T> getGenericClass(){
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

}
