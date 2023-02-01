package com.myj.tools.javac.comp;

import com.myj.tools.javac.tree.JCTree;

import java.util.Iterator;

public class Env<A> implements Iterable<Env<A>> {

    public Env<A> next;

    public Env<A> outer;

    public JCTree tree;

    public JCTree.JCCompilationUnit toplevel;

    public JCTree.JCClassDecl enclClass;

    public JCTree.JCMethodDecl enclMethod;

    public A info;

    @Override
    public Iterator<Env<A>> iterator() {
        return null;
    }

    public Env(JCTree tree, A info) {
        this.next = null;
        this.outer = null;
        this.tree = tree;
        this.toplevel = null;
        this.enclClass = null;
        this.enclMethod = null;
        this.info = info;
    }

    public Env<A> dup(JCTree tree, A dup) {
        return dupto(new Env<A>(tree, dup));
    }

    public Env<A> dupto(Env<A> that) {
        that.next = this;
        that.outer = this.outer;
        that.toplevel = this.toplevel;
        that.enclClass = this.enclClass;
        that.enclMethod = this.enclMethod;
        return that;
    }

    public Env<A> enclosing(int toplevel) {
        Env<A> env1 = this;
        while (env1 != null && env1.tree.getTag() != toplevel) env1 = env1.next;
        return env1;
    }

    public Env<A> dup(JCTree.JCMethodDecl tree) {
        return dup(tree, this.info);
    }

    public Env<A> dup(JCTree tree) {
        return dup(tree, this.info);
    }
}
