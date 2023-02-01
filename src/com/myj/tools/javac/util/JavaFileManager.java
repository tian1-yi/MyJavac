package com.myj.tools.javac.util;

import com.myj.tools.javac.file.Paths;

import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.*;
import java.lang.ref.SoftReference;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CoderResult;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;

public class JavaFileManager {

    private Paths paths;

    public Iterable<com.myj.tools.javac.util.JavaFileObject> list(Location location, String packageName, EnumSet<JavaFileObject.Kind> packageFileKinds) {

        Iterable<? extends File> path = getLocation(location);

        ArrayList<com.myj.tools.javac.util.JavaFileObject> results = new ArrayList<>();

        for (File file : path) {
            listContainer(file, results, packageName, packageFileKinds);
        }
        return results;
    }

    private void listContainer(File file, ArrayList<com.myj.tools.javac.util.JavaFileObject> results, String packageName, EnumSet<JavaFileObject.Kind> packageFileKinds) {
        if (file.isDirectory()) {
            listDirectory(file, results, packageName, packageFileKinds);
        } else {

        }
    }

    private void listDirectory(File file, ArrayList<com.myj.tools.javac.util.JavaFileObject> results, String packageName, EnumSet<JavaFileObject.Kind> fileKinds) {
        String replace = packageName.toString().replace('.', '/');
        String absolutePath = file.getAbsolutePath();
        if (!absolutePath.endsWith("\\")) {
            absolutePath = absolutePath + "\\";
        }

        if (!replace.endsWith("/")) {
            replace = replace + "/";
        }
        String sub = replace.toString().replace('/', File.separatorChar);
        File curFile = new File(absolutePath + sub);

        File[] files = curFile.listFiles();
        if (files == null) {
            return;
        }

        for (File f : files) {
            String fname = f.getName();
            if (f.isDirectory()) {
                listDirectory(f, results, fname, fileKinds);
            } else {
                if (isValidFile(fname, fileKinds))
                results.add(new com.myj.tools.javac.util.JavaFileObject(new File(f.getPath()), this));
            }
        }

    }

    /**
     * 验证文件是否需要读取
     * @param fname
     * @param fileKinds
     * @return
     */
    private boolean isValidFile(String fname, EnumSet<JavaFileObject.Kind> fileKinds) {
        JavaFileObject.Kind kind = getKind(fname);
        return fileKinds.contains(kind);
    }

    /**
     * 获取文件类型
     * @param fname
     * @return
     */
    private JavaFileObject.Kind getKind(String fname) {
        if (fname.endsWith(".class")) {
            return JavaFileObject.Kind.CLASS;
        } else if (fname.endsWith(".java")) {
            return JavaFileObject.Kind.SOURCE;
        } else {
            return JavaFileObject.Kind.OTHER;
        }
    }

    private Iterable<? extends File> getLocation(Location location) {

        paths.lazy();

        return paths.getPathForLocation(location);
    }

    public String inferBinaryName(Location location, com.myj.tools.javac.util.JavaFileObject file) {

        Iterable<? extends File> path = getLocation(location);

        return  file.inferBinaryName(path);

    }

    public interface Location {

    }


    // 用软引用，放在gc内存不足
    HashMap<String, SoftReference<CharBuffer>> cache = new HashMap<>();

    ByteBufferCache byteBufferCache = new ByteBufferCache();

    private JavaFileManager(Context context) {
        if (paths == null) {
            paths = Paths.instance(context);
        } else {
            paths.setContext(context);
        }

        
    }


    public static void registerContext(Context context) {
        context.put(JavaFileManager.class, new Context.Factory<JavaFileManager>() {
            @Override
            public JavaFileManager make(Context context) {
                return new JavaFileManager(context);
            }
        });
    }

    // 读取
    public CharBuffer readSource(File file) throws Exception {
        SoftReference<CharBuffer> softReference = cache.get(file.getName());
        if (null == softReference) {
            InputStream inputStream = new FileInputStream(file);
            ByteBuffer byteBuffer = makeCharBuffer(inputStream); // 读取byte流
            CharBuffer charBuffer = decode(byteBuffer);// 解码
            softReference = new SoftReference<CharBuffer>(charBuffer);
            cache.put(file.getName(), softReference);
        }
        return softReference.get();
    }


    // 解码
    private CharBuffer decode(ByteBuffer byteBuffer) {
        Charset charset = Charset.forName("UTF8"); //字符编码UTF8
        CharsetDecoder decoder = charset.newDecoder(); // 创建UTF8的解码器
        float factor =
                decoder.averageCharsPerByte() * 0.8f +
                        decoder.maxCharsPerByte() * 0.2f; // 配置容量
        CharBuffer dest = CharBuffer.
                allocate(10 + (int)(byteBuffer.remaining()*factor));
        CoderResult result = decoder.decode(byteBuffer, dest, true); // 解码
        dest.flip();
        return dest;
    }

    private ByteBuffer makeCharBuffer(InputStream inputStream) throws IOException {
        int limit = inputStream.available();
        if (limit < 1024) {
            limit = 1024;
        }
        ByteBuffer byteBuffer = byteBufferCache.get(limit);
        int position = 0;
        while (inputStream.available() != 0) {
            int result = inputStream.read(byteBuffer.array(), position, limit - position);
            byteBuffer.position(position + result);
        }
        return (ByteBuffer) byteBuffer.flip();
    }



    public static class ByteBufferCache {
        private ByteBuffer byteBuffer;

        // 获取缓存对象，节省内存
        public ByteBuffer get(int capacity) {
            if (capacity < 20480) {
                capacity = 20480;
            }
            ByteBuffer buffer = (byteBuffer != null && byteBuffer.capacity() >= capacity)
                    ? (ByteBuffer) byteBuffer.clear()
                    : ByteBuffer.allocate(capacity + capacity >> 1);
            byteBuffer = null;
            return buffer;
        }

        public void put(ByteBuffer byteBuffer) {
            this.byteBuffer = byteBuffer;
        }
    }
}
