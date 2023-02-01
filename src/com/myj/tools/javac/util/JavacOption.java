package com.myj.tools.javac.util;

public class JavacOption {

    public String target;

    // 处理器是否匹配
    public boolean isMatch(String s) {
        return target.equals(s);
    }

    // 处理器处理命令
    public void process(String s) {

    }

}
