package com.myj.tools.javac.util;

import com.myj.tools.javac.parser.JavacParser;
import com.myj.tools.javac.parser.Lexer;
import com.myj.tools.javac.parser.Parser;
import com.myj.tools.javac.parser.Scanner;

import java.nio.CharBuffer;

public class ParseFactory {

    public static final Context.Key<ParseFactory> parseFacotyKey = new Context.Key<ParseFactory>();

    public Names names;

    public TreeMaker treeMaker;


    public ScannerFactory scannerFactory;
    public ParseFactory(Context context) {
        context.put(parseFacotyKey, this);
        this.names = Names.instance(context);
        this.scannerFactory = ScannerFactory.instance(context);
        this.treeMaker = TreeMaker.instance(context);
    }

    public static ParseFactory instance(Context context) {
        ParseFactory parseFactory = context.get(parseFacotyKey);
        if (null == parseFactory) {
            parseFactory = new ParseFactory(context);
        }
        return parseFactory;
    }

    public Parser newParse(CharBuffer readSource) {
        Lexer lexer = scannerFactory.newScanner(readSource);
        return new JavacParser(this, lexer);
    }
}
