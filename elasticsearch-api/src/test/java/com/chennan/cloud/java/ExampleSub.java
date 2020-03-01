package com.chennan.cloud.java;

import com.chennan.cloud.junit.bo.Book;
import org.junit.Test;

public class ExampleSub extends Example<Book> {

    @Test
    public void test(){
         new ExampleSub().testGenericClass();
    }

}
