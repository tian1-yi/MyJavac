package com.myj.tools.javac.code;

import com.myj.tools.javac.util.Context;

public class Lint {

    public static final Context.Key<Lint> lintKey = new Context.Key<>();

    public Lint(Context context) {
        context.put(lintKey, this);
    }

    public static Lint instance(Context context) {
        Lint lint = context.get(lintKey);
        if (lint == null) {
            lint = new Lint(context);
        }
        return lint;
    }

}
