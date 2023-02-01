package com.myj.tools.javac.parser;

import com.myj.tools.javac.tree.JCTree;

public interface Parser {

    JCTree.JCCompilationUnit parseCompilation();
}
