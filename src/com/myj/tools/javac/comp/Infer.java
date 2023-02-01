package com.myj.tools.javac.comp;

import com.myj.tools.javac.code.Type;
import com.myj.tools.javac.util.Context;

import static com.myj.tools.javac.code.TypeTags.NONE;

public class Infer {

    public static final Type anyPoly = new Type(NONE, null);

    public static final Context.Key<Infer> inferKey = new Context.Key<>();

    public static Infer instance(Context context) {
        Infer instance = context.get(inferKey);
        if (instance == null)
            instance = new Infer(context);
        return instance;
    }

    private Infer(Context context) {
        context.put(inferKey, this);
    }
}
