package com.chennan.cloud.java;

import com.chennan.cloud.document.bo.Book;
import org.junit.Test;

public class ExampleSub extends Example<Book> {

    @Test
    public void test(){
         new ExampleSub().testGenericClass();
    }

}
