package com.myj.tools.javac.file;

import com.myj.tools.javac.util.Context;
import com.myj.tools.javac.util.JavaFileManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;

import static com.myj.tools.javac.util.StandardLocation.CLASS_PATH;

public class Paths {

    public static final Context.Key<Paths> pathsKey = new Context.Key<>();

    public boolean inited = false;

    public HashMap<JavaFileManager.Location, Path> pathsForLocation = new HashMap<>();

    public static Paths instance(Context context) {
        Paths paths = context.get(pathsKey);
        if (paths == null) {
            paths = new Paths(context);
        }
        return paths;
    }

    private Paths(Context context) {
        context.put(pathsKey, this);
    }

    public void setContext(Context context) {



    }

    private Path computeUserClassPath() {
        return new Path().addFiles(System.getProperty("java.class.path"));
    }

    public void lazy() {
        if (!inited) {
            pathsForLocation.put(CLASS_PATH, computeUserClassPath());
            inited = true;
        }
    }

    public Path getPathForLocation(JavaFileManager.Location location) {
        return pathsForLocation.get(location);
    }

    private class Path extends LinkedHashSet<File> {


        public Path addFiles(String property) {
            ArrayList<File> files = new ArrayList<>();
            int start = 0;
            while (start <= property.length()) {
                int sep = property.indexOf(File.pathSeparator, start);
                if (sep == -1) {
                    sep = property.length();
                }
                if (start < sep) {
                    files.add(new File(property.substring(start, sep)));
                }
                start = sep + 1;
            }
            for (File file : files) {
                addFile(file);
            }
            return this;
        }

        private void addFile(File file) {
            if (contains(file)) {
                return;
            }

            super.add(file);
        }
    }

}
