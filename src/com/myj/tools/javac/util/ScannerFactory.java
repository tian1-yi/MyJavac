package com.myj.tools.javac.util;

import com.myj.tools.javac.parser.Scanner;

import java.nio.CharBuffer;

public class ScannerFactory {

    public Context context;

    public static final Context.Key<ScannerFactory> scannerFactoryKey = new Context.Key<>();

    public ScannerFactory(Context context) {
        context.put(scannerFactoryKey, this);
        this.context = context;
    }

    public static ScannerFactory instance(Context context) {
        ScannerFactory scannerFactory = context.get(scannerFactoryKey);
        if (null == scannerFactory) {
            scannerFactory = new ScannerFactory(context);
        }
        return scannerFactory;
    }

    public Scanner newScanner(CharBuffer readSource) {
        return new Scanner(context, readSource);
    }

}
