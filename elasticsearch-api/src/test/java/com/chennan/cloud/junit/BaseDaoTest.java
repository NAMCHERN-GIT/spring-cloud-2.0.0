package com.chennan.cloud.junit;

import com.chennan.cloud.es.base.vo.Page;
import com.chennan.cloud.junit.bo.Book;
import com.chennan.cloud.junit.dao.BookDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class BaseDaoTest {

    @Autowired private BookDao bookDao;

    /**
     * 测试 RestHighLevelClient 客户端获取
     */
    @Test
    public void testGetClient() throws IOException {
        RestHighLevelClient client = bookDao.getClient();
        ClusterHealthRequest request = new ClusterHealthRequest();
        String clusterName = client.cluster().health(request, RequestOptions.DEFAULT).getClusterName();
        Assert.assertEquals("anchivasoc", clusterName);
    }

    /**
     * 测试索引是否存在
     */
    @Test
    public void testExistsIndex() throws IOException {
        boolean flag = bookDao.existsIndex();
        Assert.assertTrue(flag);
    }

    /**
     * 测试创建索引
     */
    @Test
    public void testCreateIndex() throws IOException {
        boolean flag = bookDao.createIndex();
        Assert.assertFalse(flag);
    }

    /**
     * 测试插入数据
     */
    @Test
    public void testInsert() throws ParseException, IOException {
        Book book = new Book().setId("6").setAuthor("陈楠")
                .setBookName("局外人").setEditionNumber("2019年9月第一版")
                .setPrice(new BigDecimal("69.00")).setPublisher("中国友谊出版社")
                .setPublicationDate(DateUtils.parseDate("2019-06-01", "yyyy-MM-dd"))
                .setWordCount(166_000).setCreateTime(Instant.now());
        boolean flag = bookDao.insert(book);
        Assert.assertTrue("插入失败!", flag);
    }

    /**
     * 测试根据id查询文档数据
     */
    @Test
    public void testGet() throws IOException {
        Optional<Book> bookOptional = bookDao.get("1");
        Assert.assertTrue(bookOptional.isPresent());
    }

    /**
     * 测试根据多个id查询文档
     */
    @Test
    public void testMultiGet() throws IOException{
        List<Book> bookList = bookDao.multiGet(Arrays.asList("1001", "1002"));
        System.out.println(bookList);
        Assert.assertEquals(2, bookList.size());
    }

    /**
     * 测试数据删除
     */
    @Test
    public void testDelete() throws IOException {
        boolean flag = bookDao.delete("6");
        Assert.assertTrue("删除id为3的book失败", flag);
    }

    /**
     * 测试 查询文档中的所有数据
     */
    @Test
    public void testList() throws IOException {
        List<Book> bookList = bookDao.list();
        Assert.assertFalse(bookList.isEmpty());
    }

    /**
     * 测试分页查询
     */
    @Test
    public void testPage() throws IOException {
        Page<Book> page = bookDao.listPage(1 , 5 );
        Assert.assertNotNull(page.getData());
    }

}
