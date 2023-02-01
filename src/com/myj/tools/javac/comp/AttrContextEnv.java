package com.myj.tools.javac.comp;

import com.myj.tools.javac.tree.JCTree;

public class AttrContextEnv extends Env<AttrContext> {

    public AttrContextEnv(JCTree tree, AttrContext info) {
        super(tree, info);
    }
}
