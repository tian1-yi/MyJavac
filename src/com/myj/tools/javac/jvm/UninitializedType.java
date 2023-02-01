package com.myj.tools.javac.jvm;

import com.myj.tools.javac.code.Symbol;
import com.myj.tools.javac.code.Type;
import com.myj.tools.javac.code.TypeTags;

public class UninitializedType extends Type.DelegatedType {

    public static final int UNINITIALIZED_THIS = TypeTags.TypeTagCount;
    public static final int UNINITIALIZED_OBJECT = UNINITIALIZED_THIS + 1;

    public static UninitializedType uninitializedThis(Type qtype) {
        return new UninitializedType(UNINITIALIZED_THIS, qtype, -1);
    }

    public static UninitializedType uninitializedObject(Type qtype, int offset) {
        return new UninitializedType(UNINITIALIZED_OBJECT, qtype, offset);
    }

    public final int offset;

    public UninitializedType(int tag, Type qtype, int offset) {
        super(tag, qtype);
        this.offset = offset;
    }

    Type initializedType() {
        return qtype;
    }
}
