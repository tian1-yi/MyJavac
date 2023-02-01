package com.myj.tools.javac.util;

public class Names {

    public Table table;

    public static final Context.Key<Names> namesKey = new Context.Key<Names>();
    public final Name error;

    public final Name asterisk;

    public final Name init;

    public final Name clinit;

    public final Name empty;

    public final Name hyphen;

    public final Name _this;

    public final Name java_lang_Object;

    public final Name Array;

    public final Name java_lang;
    public final Name Method;

    public final Name Bound;
    public final Name StackMapTable;
    public final Name slash;
    public Name _super;
    public Name ConstantValue;
    public Name Code;
    public Name SourceFile;

    public Names(Context context) {
        context.put(namesKey, this);
        this.table = new SharedTable(this);

        error = fromString("<error>");
        asterisk = fromString("*");
        init = fromString("<init>");
        clinit = fromString("<clinit>");
        empty = fromString("");
        hyphen = fromString("-");
        _this = fromString("this");
        java_lang_Object = fromString("java.lang.Object");
        Array = fromString("Array");
        java_lang = fromString("java.lang");
        Method = fromString("Method");
        Bound = fromString("Bound");
        StackMapTable = fromString("StackMapTable");
        slash = fromString("/");
        _super = fromString("super");
        ConstantValue = fromString("ConstantValue");
        Code = fromString("Code");
        SourceFile = fromString("SourceFile");
    }

    public static Names instance(Context context) {
        Names names = context.get(namesKey);
        if (null == names) {
            names = new Names(context);
        }
        return names;
    }

    /**
     * 获取Name
     * @param cs 数组
     * @param start 起始位置
     * @param len 长度
     * @return
     */
    public Name fromChars(char[] cs, int start, int len) {
        return table.fromChars(cs, start, len);
    }

    public Name fromString(String name) {
        return table.fromString(name);
    }

    public Name fromUtf(byte[] elems, int i, int length) {
        return table.fromUtf(elems, i, length);
    }

    public Name fromUtf(byte[] cs) {
        return table.fromUtf(cs);
    }


    public static abstract class Table {

        public Names names;

        public Table(Names names) {
            this.names = names;
        }

        /**
         * 获取Name
         * @param cs 字符数组
         * @param start 起始位置
         * @param len 长度
         * @return
         */
        public abstract Name fromChars(char[] cs, int start, int len);

        /**
         * 根据字符串获取Name
         * @param name 字符串
         * @return
         */
        public Name fromString(String name) {
            char[] cs = name.toCharArray();
            return fromChars(cs, 0, cs.length);
        }

        public abstract Name fromUtf(byte[] bs, int i, int length);

        public Name fromUtf(byte[] cs) {
            return fromUtf(cs, 0, cs.length);
        }
    }

}
