package com.myj.tools.javac.comp;

import com.myj.tools.javac.code.Lint;
import com.myj.tools.javac.code.Scope;
import com.myj.tools.javac.code.Symbol;

public class AttrContext {

    public int staticLevel = 0;
    public boolean varArgs = false;
    public boolean isSelfCall = false;
    public boolean selectSuper = false;
    Scope scope = null;

    Lint lint;

    Symbol enclVar = null;

    public AttrContext dup(Scope scope) {
        AttrContext attrContext = new AttrContext();
        attrContext.scope = scope;
        attrContext.lint = lint;
        attrContext.varArgs = varArgs;
        return attrContext;
    }

    public AttrContext dup() {
        return dup(scope);
    }
}
