package com.myj.tools.javac.util;

import com.myj.tools.javac.comp.*;
import com.myj.tools.javac.jvm.ClassWriter;
import com.myj.tools.javac.jvm.Gen;
import com.myj.tools.javac.parser.Parser;
import com.myj.tools.javac.tree.JCTree;

import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

public class JavaCompiler {

    public ParseFactory parseFactory;

    public TreeMaker maker;

    public Enter enter;

    public Attr attr;

    Todo todo;

    ClassWriter classWriter;

    Gen gen;

    private JavaCompiler(Context context) {
        this.parseFactory = ParseFactory.instance(context);
        this.maker = TreeMaker.instance(context);
        this.enter = Enter.instance(context);
        this.attr = Attr.instance(context);
        this.todo = Todo.instance(context);
        this.classWriter = ClassWriter.instance(context);
        this.gen = Gen.instance(context);
    }

    public static JavaCompiler instance(Context context) {
        return new JavaCompiler(context);
    }

    public void compile(List<JavaFileObject> javaFileObjects, Context context) throws Exception {
        enter(parseFile(javaFileObjects));
        compile2();
    }

    /**
     * 标注并生成class文件
     */
    private void compile2() {
        Env<AttrContext> attrContextEnv = todo.get(0);
        // 标注语法树
        attribute(attrContextEnv);
        // 生成字节码
        generate(attrContextEnv, attrContextEnv.enclClass);
        classWriter.writeClass(attrContextEnv.toplevel.defs.get(0));
    }

    /**
     * 生成字节码文件
     * @param attrContextEnv
     * @param enclClass
     */
    private void generate(Env<AttrContext> attrContextEnv, JCTree.JCClassDecl enclClass) {
        getCode(attrContextEnv, enclClass);
    }

    private JavaFileObject getCode(Env<AttrContext> env, JCTree.JCClassDecl cdef) {

        gen.genClass(env, cdef);
        return classWriter.writeClass(cdef.sym);

    }

    /**
     * 语法树标注
     * @param env
     * @return
     */
    private Env<AttrContext> attribute(Env<AttrContext> env) {

        attr.attrib(env);

        return env;

    }

    private List<JCTree.JCCompilationUnit> enter(List<JCTree.JCCompilationUnit> roots) {

        enter.main(roots);
        JCTree.JCCompilationUnit jcCompilationUnit = roots.get(0);

        return roots;

    }

    // 解析多个文件
    private List<JCTree.JCCompilationUnit> parseFile(List<JavaFileObject> javaFileObjects) throws Exception {

        ArrayList<JCTree.JCCompilationUnit> jcCompilationUnits = new ArrayList<>();

        for (JavaFileObject javaFileObject : javaFileObjects) {
            jcCompilationUnits.add(parseFile(javaFileObject));
        }
        return jcCompilationUnits;
    }

    // 解析单个文件
    private JCTree.JCCompilationUnit parseFile(JavaFileObject javaFileObject) throws Exception {
        return parseFile(javaFileObject, readSource(javaFileObject));
    }

    private JCTree.JCCompilationUnit parseFile(JavaFileObject javaFileObject, CharBuffer readSource) {
        JCTree.JCCompilationUnit tree = maker.TopLevel(null, null, null);

        Parser parser = parseFactory.newParse(readSource);// 获取词法分析器
        JCTree.JCCompilationUnit jcCompilationUnit = parser.parseCompilation();

        return jcCompilationUnit;
    }

    private CharBuffer readSource(JavaFileObject javaFileObject) throws Exception {
        return javaFileObject.readSource();
    }
}
