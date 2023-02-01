package com.myj.tools.javac.util;

import java.util.HashMap;
import java.util.Map;

public class Context {

    public static class Key<T> {}


    // class 到key的映射
    private Map<Class<?>, Key<?>> kt = new HashMap<Class<?>, Key<?>>();

    // key到object的映射
    private Map<Key<?>,Object> ht = new HashMap<Key<?>,Object>();

    // key到Factory的映射
    private Map<Key<?>,Factory<?>> ft = new HashMap<Key<?>,Factory<?>>();

    public <T> T get(Class<T> clazz) {
        return get(key(clazz));
    }

    public <T> T get(Key<T> key) {
        Object o = ht.get(key);
        if (o instanceof Factory<?>) {
            Factory<?> factory = (Factory<?>) o;
            o = factory.make(this);
        }
        return Context.uncheckCast(o);
    }

    // 根据class获取key类型
    private <T> Key<T> key(Class<T> clazz) {
        Key<T> key = uncheckCast(kt.get(clazz));
        if (null == key) {
            key = new Key<T>();
            kt.put(clazz, key);
        }
        return key;
    }

    private static  <T> T uncheckCast(Object o) {
        return (T) o;
    }

    // 存放object到工厂的映射
    public <T> void put(Class<T> javaFileManagerClass, Factory<T> factory) {
        put(key(javaFileManagerClass), factory);
    }

    public <T> void put(Key<T> key, Factory<T> factory) {
        Object put = ht.put(key, factory);
        ft.put(key, factory);
    }

    public <T> void put(Key<T> key, T data) {
        ht.put(key, data);
    }


    public static interface Factory<T> {
        T make(Context context);
    }



}
