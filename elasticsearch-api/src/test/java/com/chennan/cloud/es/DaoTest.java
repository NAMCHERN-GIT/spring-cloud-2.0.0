package com.chennan.cloud.es;

import com.chennan.cloud.document.dao.BookDao;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class DaoTest {

    @Autowired
    private BookDao bookBaseDao;

    @Test
    public void testExistsIndex() throws IOException {
        boolean flag = bookBaseDao.existsIndex();
        Assert.assertTrue(flag);
    }

}
