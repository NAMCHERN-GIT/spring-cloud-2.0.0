package com.chennan;

import org.junit.Test;

public class BooleanTest {
    @Test
    public void initBoolean(){
        boolean f = false;
        int a = 0;
        System.out.println(a);
        System.out.println(f);
    }

    @Test
    public void split(){
        String a = "1,2,,4,5,,d,f,,h";
        String[] childJobIds = a.split(",");
        for (String t:childJobIds) {
            System.out.println(t);
        }
    }

    @Test
    public void join(){
        String[] childJobIds = {"1","2","3"};
        String t = String.join(",", childJobIds);
        System.out.println(t);
    }

}
