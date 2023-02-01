package com.myj.tools.javac.util;

public interface Filter<T> {

    boolean accepts(T t);

}
