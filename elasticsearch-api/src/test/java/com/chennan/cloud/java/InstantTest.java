package com.chennan.cloud.java;

import org.junit.Test;

import java.time.Instant;
import java.util.Date;

public class InstantTest {

    @Test
    public void test1(){
        Date date = Date.from(Instant.now());
        System.out.println(date);
    }

}
