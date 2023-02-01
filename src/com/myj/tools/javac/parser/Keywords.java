package com.myj.tools.javac.parser;

import com.myj.tools.javac.util.Context;
import com.myj.tools.javac.util.Name;
import com.myj.tools.javac.util.Names;

import static com.myj.tools.javac.parser.Token.IDENTIFIER;

public class Keywords {


    public static final Context.Key<Keywords> keywordsKey = new Context.Key<>();

    // token数组
    private Token[] tokens;

    /**
     * 最大的maxkey
     */
    private int maxkey = 0;

    /**
     * 存储Name
     */
    private Name[] tokenNames =  new Name[Token.values().length];

    /**
     * Names对象
     */
    private Names names;

    private Keywords(Context context) {
        context.put(keywordsKey, this);
        names = Names.instance(context);

        for (Token t : Token.values()) {
            if (t.name != null) {
                enterKeyword(t.name, t);
            } else {
                tokenNames[t.ordinal()] = null;
            }
        }

        tokens = new Token[maxkey + 1];
        for (int i = 0; i <= maxkey; i ++) {
            tokens[i] = IDENTIFIER;
        }
        for (Token token : Token.values()) {
            if (token.name != null) {
                tokens[tokenNames[token.ordinal()].getIndex()] = token;
            }
        }
    }

    private void enterKeyword(String name, Token t) {
        Name n = names.fromString(name);
        // 将token的ordinal位置的name设置
        tokenNames[t.ordinal()] = n;
        if (n.getIndex() >  maxkey) maxkey = n.getIndex();
    }

    public static Keywords instance(Context context) {
        Keywords keywords = context.get(keywordsKey);
        if (null == keywords) {
            keywords = new Keywords(context);
        }
        return keywords;
    }

    public Token key(Name name) {
        return (name.getIndex() > maxkey) ? IDENTIFIER : tokens[name.getIndex()];
    }



}
