package com.myj.tools.javac.util;

import com.myj.tools.javac.code.Scope;
import com.myj.tools.javac.code.Symbol;
import com.myj.tools.javac.code.Type;
import com.myj.tools.javac.code.TypeTags;
import com.myj.tools.javac.jvm.ClassReader;
import com.myj.tools.javac.tree.JCTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.myj.tools.javac.code.Flags.ACYCLIC;
import static com.myj.tools.javac.code.Flags.PUBLIC;
import static com.myj.tools.javac.jvm.ByteCodes.*;

public class Symtab {



    public static final Context.Key<Symtab> symtabKey = new Context.Key<Symtab>();


    public final Symbol.PackageSymbol rootPackage;

    public final Symbol.PackageSymbol unnamedPackage;

    public final Type objectType;

    public final Names names;

    public final ClassReader classReader;

    public final Symbol.ClassSymbol predefClass;

    public final Map<Name, Symbol.PackageSymbol> packages = new HashMap<>();

    public final Map<Name, Symbol.ClassSymbol> classes = new HashMap<>();

    public final Type[] typeOfTag = new Type[TypeTags.TypeTagCount];

    public final Name[] boxedName = new Name[TypeTags.TypeTagCount];

    public final Type byteType = new Type(TypeTags.BYTE, null);  // 类型
    public final Type charType = new Type(TypeTags.CHAR, null);
    public final Type shortType = new Type(TypeTags.SHORT, null);
    public final Type intType = new Type(TypeTags.INT, null);
    public final Type longType = new Type(TypeTags.LONG, null);
    public final Type floatType = new Type(TypeTags.FLOAT, null);
    public final Type doubleType = new Type(TypeTags.DOUBLE, null);
    public final Type booleanType = new Type(TypeTags.BOOLEAN, null);
    public final Type botType = new Type.BottomType();
    public final Type.JCNoType voidType = new Type.JCNoType(TypeTags.VOID);

    public final Symbol.ClassSymbol arrayClass;

    public final Symbol.TypeSymbol noSymbol;


    public final Type classType;
    public final Type classLoaderType;
    public final Type stringType;
    public final Type stringBufferType;
    public final Type stringBuilderType;
    public final Type cloneableType;
    public final Type serializableType;
    public final Type methodHandleType;
    public final Type polymorphicSignatureType;
    public final Type throwableType;
    public final Type errorType;
    public final Type interruptedExceptionType;
    public final Type illegalArgumentExceptionType;
    public final Type exceptionType;
    public final Type runtimeExceptionType;
    public final Type classNotFoundExceptionType;
    public final Type noClassDefFoundErrorType;
    public final Type noSuchFieldErrorType;
    public final Type assertionErrorType;
    public final Type cloneNotSupportedExceptionType;
    public final Type annotationType;

    public final Type listType;
    public final Type collectionsType;
    public final Type comparableType;
    public final Type arraysType;

    public final Type iteratorType;
    public final Type annotationTargetType;
    public final Type overrideType;
    public final Type retentionType;
    public final Type deprecatedType;
    public final Type suppressWarningsType;
    public final Type inheritedType;

    public final Type systemType;
    public final Type autoCloseableType;
    public final Type trustMeType;

    public final Symbol.ClassSymbol methodClass;

    public final Symbol.ClassSymbol boundClass;

    private Symtab(Context context) {
        context.put(symtabKey, this);

        this.names = Names.instance(context);

        rootPackage = new Symbol.PackageSymbol(names.empty, null);

        unnamedPackage = new Symbol.PackageSymbol(names.empty, rootPackage);

        classReader = ClassReader.instance(context);
        classReader.init(this, true);

        predefClass = new Symbol.ClassSymbol(PUBLIC|ACYCLIC, names.empty, rootPackage);


        noSymbol = new Symbol.TypeSymbol(0, names.empty, Type.noType, rootPackage);

        initType(byteType, "byte", "Byte");
        initType(shortType, "short", "Short");
        initType(charType, "char", "Character");
        initType(intType, "int", "Integer");
        initType(longType, "long", "Long");
        initType(floatType, "float", "Float");
        initType(doubleType, "double", "Double");
        initType(booleanType, "boolean", "Boolean");
        initType(voidType, "void", "Void");
        initType(botType, "<nulltype>");


        Scope scope = new Scope(predefClass);
        predefClass.members_field = scope;

        boundClass = new Symbol.ClassSymbol(PUBLIC|ACYCLIC, names.Bound, noSymbol);
        boundClass.members_field = new Scope.ErrorScope(boundClass);

        methodClass = new Symbol.ClassSymbol(PUBLIC|ACYCLIC, names.Method, noSymbol);
        methodClass.members_field = new Scope.ErrorScope(boundClass);

        scope.enter(byteType.tsym);
        scope.enter(shortType.tsym);
        scope.enter(charType.tsym);
        scope.enter(intType.tsym);
        scope.enter(longType.tsym);
        scope.enter(floatType.tsym);
        scope.enter(doubleType.tsym);
        scope.enter(booleanType.tsym);


        arrayClass = new Symbol.ClassSymbol(PUBLIC|ACYCLIC, names.Array, noSymbol);


        objectType = enterClass("java.lang.Object");
        classType = enterClass("java.lang.Class");
        stringType = enterClass("java.lang.String");
        stringBufferType = enterClass("java.lang.StringBuffer");
        stringBuilderType = enterClass("java.lang.StringBuilder");
        cloneableType = enterClass("java.lang.Cloneable");
        throwableType = enterClass("java.lang.Throwable");
        serializableType = enterClass("java.io.Serializable");
        methodHandleType = enterClass("java.lang.invoke.MethodHandle");
        polymorphicSignatureType = enterClass("java.lang.invoke.MethodHandle$PolymorphicSignature");
        errorType = enterClass("java.lang.Error");
        illegalArgumentExceptionType = enterClass("java.lang.IllegalArgumentException");
        interruptedExceptionType = enterClass("java.lang.InterruptedException");
        exceptionType = enterClass("java.lang.Exception");
        runtimeExceptionType = enterClass("java.lang.RuntimeException");
        classNotFoundExceptionType = enterClass("java.lang.ClassNotFoundException");
        noClassDefFoundErrorType = enterClass("java.lang.NoClassDefFoundError");
        noSuchFieldErrorType = enterClass("java.lang.NoSuchFieldError");
        assertionErrorType = enterClass("java.lang.AssertionError");
        cloneNotSupportedExceptionType = enterClass("java.lang.CloneNotSupportedException");
        annotationType = enterClass("java.lang.annotation.Annotation");
        classLoaderType = enterClass("java.lang.ClassLoader");


        listType = enterClass("java.util.List");
        collectionsType = enterClass("java.util.Collections");
        comparableType = enterClass("java.lang.Comparable");
        arraysType = enterClass("java.util.Arrays");

        iteratorType = enterClass("java.util.Iterator");
        annotationTargetType = enterClass("java.lang.annotation.Target");
        overrideType = enterClass("java.lang.Override");
        retentionType = enterClass("java.lang.annotation.Retention");
        deprecatedType = enterClass("java.lang.Deprecated");
        suppressWarningsType = enterClass("java.lang.SuppressWarnings");
        inheritedType = enterClass("java.lang.annotation.Inherited");
        systemType = enterClass("java.lang.System");
        autoCloseableType = enterClass("java.lang.AutoCloseable");

        trustMeType = enterClass("java.lang.SafeVarargs");


        initObjectClass();

        enterUnop("+", doubleType, doubleType, nop);
        enterUnop("+", floatType, floatType, nop);
        enterUnop("+", longType, longType, nop);
        enterUnop("+", intType, intType, nop);

        enterUnop("-", doubleType, doubleType, dneg);
        enterUnop("-", floatType, floatType, fneg);
        enterUnop("-", longType, longType, lneg);
        enterUnop("-", intType, intType, ineg);

        enterUnop("~", longType, longType, lxor);
        enterUnop("~", intType, intType, ixor);

        enterUnop("++", doubleType, doubleType, dadd);
        enterUnop("++", floatType, floatType, fadd);
        enterUnop("++", longType, longType, ladd);
        enterUnop("++", intType, intType, iadd);
        enterUnop("++", charType, charType, iadd);
        enterUnop("++", shortType, shortType, iadd);
        enterUnop("++", byteType, byteType, iadd);

        enterUnop("--", doubleType, doubleType, dsub);
        enterUnop("--", floatType, floatType, fsub);
        enterUnop("--", longType, longType, lsub);
        enterUnop("--", intType, intType, isub);
        enterUnop("--", charType, charType, isub);
        enterUnop("--", shortType, shortType, isub);
        enterUnop("--", byteType, byteType, isub);

        enterUnop("!", booleanType, booleanType, bool_not);

        enterBinop("+", doubleType, doubleType, doubleType, dadd);
        enterBinop("+", floatType, floatType, floatType, fadd);
        enterBinop("+", longType, longType, longType, ladd);
        enterBinop("+", intType, intType, intType, iadd);

        enterBinop("-", doubleType, doubleType, doubleType, dsub);
        enterBinop("-", floatType, floatType, floatType, fsub);
        enterBinop("-", longType, longType, longType, lsub);
        enterBinop("-", intType, intType, intType, isub);

        enterBinop("*", doubleType, doubleType, doubleType, dmul);
        enterBinop("*", floatType, floatType, floatType, fmul);
        enterBinop("*", longType, longType, longType, lmul);
        enterBinop("*", intType, intType, intType, imul);

        enterBinop("/", doubleType, doubleType, doubleType, ddiv);
        enterBinop("/", floatType, floatType, floatType, fdiv);
        enterBinop("/", longType, longType, longType, ldiv);
        enterBinop("/", intType, intType, intType, idiv);

    }

    private void initObjectClass() {
        ((Symbol.ClassSymbol) objectType.tsym).members_field = new Scope(objectType.tsym);
        Scope members_field = ((Symbol.ClassSymbol) objectType.tsym).members_field;
        Type.MethodType initMethodType = new Type.MethodType(new ArrayList<Type>(), voidType, new ArrayList<Type>(), methodClass);
        Symbol.MethodSymbol clinitSymbol = new Symbol.MethodSymbol(8, names.clinit, initMethodType, objectType.tsym);
        members_field.enter(clinitSymbol);

        Symbol.MethodSymbol initSymbol = new Symbol.MethodSymbol(1, names.init, initMethodType, objectType.tsym);
        members_field.enter(initSymbol);
    }

    private void enterBinop(String name, Type left, Type right, Type res, int opcode) {
        ArrayList<Type> types = new ArrayList<>();
        types.add(left);
        types.add(right);
        predefClass.members().enter(new JCTree.OperatorSymbol(names.fromString(name), new Type.MethodType(types, res, null, methodClass), opcode, predefClass));
    }

    private void initType(Type type, String s) {
        intType(type, new Symbol.ClassSymbol(PUBLIC, names.fromString(s), type, rootPackage));
    }

    private void initType(Type type, String name, String bname) {
        intType(type, name);
        boxedName[type.tag] =  names.fromString("java.lang." + bname);
    }

    private void intType(Type type, String name) {
        intType(type, new Symbol.ClassSymbol(PUBLIC, names.fromString(name), type, rootPackage));
    }

    private void intType(Type type, Symbol.ClassSymbol classSymbol) {
        type.tsym = classSymbol;
        typeOfTag[type.tag] = type;
    }

    private Type enterClass(String s) {
        return classReader.enterClass(names.fromString(s)).type;
    }

    public static Symtab instance(Context context) {
        Symtab symtab = context.get(symtabKey);
        if (null == symtab) {
            symtab = new Symtab(context);
        }
        return symtab;
    }


    JCTree.OperatorSymbol enterUnop(String name, Type arg, Type res, int opcode) {
        ArrayList<Type> types = new ArrayList<>();
        types.add(arg);
        JCTree.OperatorSymbol sym = new JCTree.OperatorSymbol(names.fromString(name), new Type.MethodType(types, res, null, methodClass), opcode, predefClass);
        predefClass.members().enter(sym);
        return sym;

    }
}
