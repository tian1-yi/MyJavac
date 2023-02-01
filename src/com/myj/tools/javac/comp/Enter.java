package com.myj.tools.javac.comp;

import com.myj.tools.javac.code.Lint;
import com.myj.tools.javac.code.Scope;
import com.myj.tools.javac.code.Symbol;
import com.myj.tools.javac.code.Type;
import com.myj.tools.javac.jvm.ClassReader;
import com.myj.tools.javac.tree.JCTree;
import com.myj.tools.javac.tree.TreeInfo;
import com.myj.tools.javac.util.Context;
import com.myj.tools.javac.util.Symtab;
import com.myj.tools.javac.util.TreeMaker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.myj.tools.javac.code.Flags.PUBLIC;
import static com.myj.tools.javac.code.Kinds.PCK;

public class Enter extends JCTree.Visitor {

    public static final Context.Key<Enter> enterKey = new Context.Key<>();

    ClassReader classReader;

    Symtab syms;

    TreeMaker treeMaker;

    Lint lint;

    List<Symbol.ClassSymbol> uncompleted;

    MemberEnter memberEnter;

    Map<Symbol.TypeSymbol, Env<AttrContext>> typeEnvs = new HashMap<>();

    public List<Env<AttrContext>> todo = new ArrayList<Env<AttrContext>>();

    private JCTree.JCClassDecl predefClassDef;

    public static Enter instance(Context context) {
        Enter enter = context.get(enterKey);
        if (null == enter) {
            enter = new Enter(context);
        }
        return enter;
    }


    public Env<AttrContext> env;

    public Type result;

    private Enter(Context context) {
        context.put(enterKey, this);

        this.classReader = ClassReader.instance(context);
        this.syms = Symtab.instance(context);
        this.treeMaker = TreeMaker.instance(context);
        this.lint = Lint.instance(context);
        this.memberEnter = MemberEnter.instance(context);
        predefClassDef = treeMaker.ClassDef(treeMaker.Modifiers(PUBLIC), syms.predefClass.name, null, null, null, null);


    }

    public void main(List<JCTree.JCCompilationUnit> roots) {

        complete(roots, null);
    }

    private void complete(List<JCTree.JCCompilationUnit> trees, Symbol.ClassSymbol c) {

        uncompleted = new ArrayList<>();

        classEnter(trees, null);


        int i = 0;

        while (i < uncompleted.size()) {
            Symbol.ClassSymbol clazz = uncompleted.get(i);
            clazz.complete();
            i ++;
        }

    }

    /**
     * 填充class
     * @param trees
     * @param env
     * @return
     * @param <T>
     */
    <T extends JCTree> List<Type> classEnter(List<T> trees, Env<AttrContext> env) {
        ArrayList<Type> ts = new ArrayList<>();
        for (T tree : trees) {
            Type type = classEnter(tree, env);
            if (type != null) {
                ts.add(type);
            }
        }
        return ts;
    }

    Type classEnter(JCTree tree, Env<AttrContext> env) {
        Env<AttrContext> preEnv = this.env;

        this.env = env;
        tree.accept(this);
        return result;
    }

    @Override
    public void visitTree(JCTree tree) {
        result = null;
    }

    @Override
    public void visitTopLevel(JCTree.JCCompilationUnit tree) {
        if (tree.pid != null) {
            // 包符号
            tree.packageSymbol = classReader.enterPackage(TreeInfo.fullName(tree.pid));
        } else {
            tree.packageSymbol = syms.unnamedPackage;
        }

        // 包符号填充
        tree.packageSymbol.complete();
        //创建编译环境
        Env<AttrContext> topEnv = topLevelEnv(tree);

        classEnter(tree.defs, topEnv);
        result = null;
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl tree) {
        Symbol owner = env.info.scope.owner;

        Scope enclScope = enterScope(env);

        Symbol.ClassSymbol c;
        if (owner.kind == PCK) {
            // 属于包
            Symbol.PackageSymbol packge = (Symbol.PackageSymbol) owner;

            c = classReader.enterClass(tree.name, packge);
            packge.members().enterIfAbsent(c);
        } else {
            return;
        }

        tree.sym = c;


        Env<AttrContext> localEnv = classEnv(tree, env);
        typeEnvs.put(c, localEnv);

        c.members_field = new Scope(c);

        if (!c.isLocal() && uncompleted != null) {
            uncompleted.add(c);
        }

        c.completer = memberEnter;

        classEnter(tree.defs, localEnv);
        result = c.type;
    }

    private Env<AttrContext> classEnv(JCTree.JCClassDecl tree, Env<AttrContext> env) {

        Env<AttrContext> localEnv = env.dup(tree, env.info.dup(new Scope(tree.sym)));
        localEnv.enclClass = tree;
        localEnv.outer = env;
        localEnv.info.lint = null;
        return localEnv;
    }

    Scope enterScope(Env<AttrContext> env) {
        return (env.tree.getTag() == JCTree.CLASSDEF)
                ? ((JCTree.JCClassDecl) env.tree).sym.members_field
                : env.info.scope;
    }

    private Env<AttrContext> topLevelEnv(JCTree.JCCompilationUnit tree) {

        Env<AttrContext> localEnv = new Env<AttrContext>(tree, new AttrContext());
        localEnv.toplevel = tree;
        localEnv.enclClass = predefClassDef;
        tree.namedImportScope = new Scope.ImportScope(tree.packageSymbol);
        tree.starImportScope = new Scope.StarImportScope(tree.packageSymbol);
        localEnv.info.scope = tree.namedImportScope;
        localEnv.info.lint = lint;
        return localEnv;
    }
}
