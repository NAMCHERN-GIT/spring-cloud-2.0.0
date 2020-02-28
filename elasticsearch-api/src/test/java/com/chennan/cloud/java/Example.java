package com.chennan.cloud.java;

import java.lang.reflect.ParameterizedType;

public abstract class Example<T> {

    @SuppressWarnings("unchecked")
    private Class getGenericClass(){
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public void testGenericClass(){
        Class clazz = getGenericClass();
        System.out.println(" Example 的泛型是 -->  " + clazz.getSimpleName());
    }

}
