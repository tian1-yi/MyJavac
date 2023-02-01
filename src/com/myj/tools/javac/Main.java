package com.myj.tools.javac;

import com.myj.tools.javac.util.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main {


    // 解析文件
    List<File> files;

    // Javac命令处理器
    List<JavacOption> options;

    JavaFileManager javaFileManager;

    int argsIndex = 0;

    public Main() {
        options = new ArrayList<>();
        options.add(new JavacOption() {
            @Override
            public boolean isMatch(String s) {
                return true;
            }

            @Override
            public void process(String s) {
                File file = new File(s);
                files.add(file);
            }
        });
        files = new ArrayList<>();
    }

    public int compile(String[] args) throws Exception {
        Context context = new Context();
        JavaFileManager.registerContext(context);
        int result = compile(args, context);
        return result;
    }

    private  int compile(String[] args, Context context) throws Exception {

        // 处理参数
        List<File> files = processArgs(args);

        // 文件管理
        javaFileManager = context.get(JavaFileManager.class);

        // 编译器
        JavaCompiler compiler = JavaCompiler.instance(context);

        List<JavaFileObject> javaFileObjects = new ArrayList<>();
        for (File file : files) {
            javaFileObjects.add(new JavaFileObject(file, javaFileManager));
        }

        // 开始编译
        compiler.compile(javaFileObjects, context);
        return 1;
    }

    private List<File> processArgs(String[] args) {
        int length = args.length;
        while (argsIndex < length) {
            JavacOption curOption = null;
            String arg = args[argsIndex];
            for (JavacOption option : options) {
                if (option.isMatch(arg)) {
                    curOption = option;
                    break;
                }
            }
            curOption.process(arg);
            argsIndex ++;
        }
        return files;
    }

}
