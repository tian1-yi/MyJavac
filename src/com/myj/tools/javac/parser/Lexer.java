package com.myj.tools.javac.parser;

import com.myj.tools.javac.util.Name;

public interface Lexer {
    void nextToken();

    void nextChar();

    void scanChar();

    Token token();

    Name name();

    int pos();

    int radix();

    String stringVal();
}
