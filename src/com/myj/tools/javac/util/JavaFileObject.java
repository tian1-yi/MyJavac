package com.myj.tools.javac.util;

import java.io.File;
import java.nio.CharBuffer;

public class JavaFileObject {

    enum Kind {

        SOURCE(".java"),


        CLASS(".class"),

        HTML(".html"),

        OTHER("");

        public final String extension;
        private Kind(String extension) {
            extension.getClass(); // null check
            this.extension = extension;
        }
    };




    private String name;

    private File file;

    private JavaFileManager javaFileManager;

    public JavaFileObject(File file, JavaFileManager javaFileManager) {
        this.javaFileManager = javaFileManager;
        this.name = file.getAbsolutePath();
        this.file = file;
    }

    public CharBuffer readSource() throws Exception {

        return javaFileManager.readSource(file);
    }

    public javax.tools.JavaFileObject.Kind getKind() {
        if (name.endsWith(".class")) {
            return javax.tools.JavaFileObject.Kind.CLASS;
        } else if (name.endsWith(".java")) {
            return javax.tools.JavaFileObject.Kind.SOURCE;
        } else {
            return javax.tools.JavaFileObject.Kind.OTHER;
        }
    }

    public String inferBinaryName(Iterable<? extends File> path) {
        String filePath = file.getPath();
        for (File dir : path) {
            String dPath = dir.getPath();
            if (!dPath.endsWith(File.separator)) {
                dPath += File.separator;
            }
            if (filePath.regionMatches(true, 0, dPath, 0, dPath.length()) && new File(filePath.substring(0, dPath.length())).equals(new File(dPath))) {
                String substring = filePath.substring(dPath.length());
                return removeExtension(substring).replace(File.separator, ".");
            }
        }
        return null;
    }

    private String removeExtension(String substring) {
        int lastDot = substring.lastIndexOf('.');
        return lastDot == -1 ? substring : substring.substring(0, lastDot);
    }
}
