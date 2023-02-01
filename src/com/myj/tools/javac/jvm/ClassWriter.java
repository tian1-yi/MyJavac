package com.myj.tools.javac.jvm;

import com.myj.tools.javac.code.Scope;
import com.myj.tools.javac.code.Symbol;
import com.myj.tools.javac.code.Type;
import com.myj.tools.javac.tree.JCTree;
import com.myj.tools.javac.util.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


import static com.myj.tools.javac.code.Flags.*;
import static com.myj.tools.javac.code.Kinds.*;
import static com.myj.tools.javac.code.TypeTags.*;
import static com.myj.tools.javac.jvm.UninitializedType.UNINITIALIZED_OBJECT;
import static com.myj.tools.javac.jvm.UninitializedType.UNINITIALIZED_THIS;
import static com.myj.tools.javac.util.StandardLocation.CLASS_OUTPUT;


public class ClassWriter extends ClassFile{


    public static final Context.Key<ClassWriter> classWriterKey = new Context.Key<>();

    public JavaFileManager fileManager;

    Types types;

    Pool pool;

    static final int DATA_BUF_SIZE = 0x0fff0;
    static final int POOL_BUF_SIZE = 0x1fff0;

    ByteBuffer databuf = new ByteBuffer(DATA_BUF_SIZE);

    ByteBuffer poolbuf = new ByteBuffer(POOL_BUF_SIZE);

    ByteBuffer sigbuf = new ByteBuffer();

    Names names;

    public ClassWriter(Context context) {
        context.put(classWriterKey, this);

        fileManager = context.get(JavaFileManager.class);
        types = Types.instance(context);
        names = Names.instance(context);
    }

    public static ClassWriter instance(Context context) {
        ClassWriter classWriter = context.get(classWriterKey);
        if (classWriter == null) {
            classWriter = new ClassWriter(context);
        }
        return classWriter;
    }

    public JavaFileObject writeClass(Symbol.ClassSymbol c) {
        try {
            File file = new File("C:/Users/luozichen/Desktop/MyJavac/javaclass/chapter1/test/TestJavac.class");

            FileOutputStream outputStream = new FileOutputStream(file);
            writeClassFile(outputStream, c);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * 往文件里写入字节码
     * @param outputStream
     * @param c
     */
    private void writeClassFile(OutputStream outputStream, Symbol.ClassSymbol c) {

        //父类
        Type supertype = types.supertype(c.type);
        pool = c.pool;


        int flags = (int) c.flags();
        if (flags == 0) {
            flags = 1;
        }

        flags = flags & ClassFlags & ~STRICTFP;


        if ((flags & INTERFACE) == 0) flags |= ACC_SUPER;

        // 类修饰
        databuf.appendChar(flags);
        // 当前class
        databuf.appendChar(pool.put(c));
        // 父类class
        databuf.appendChar(supertype.tag == CLASS ? pool.put(supertype.tsym) : 0);
        // 接口数，未支持
        databuf.appendChar(0);

        int fieldCount = 0;
        int methodCount = 0;

        for (Scope.Entry e = c.members().elems; e != null; e = e.sibling) {
            switch (e.sym.kind) {
                case VAR: fieldCount++; break;
                case MTH: methodCount++; break;
                case TYP: break;
                default:
                    break;
            }
        }

        // 变量
        databuf.appendChar(fieldCount);
        writeFields(c.members().elems);
        // 方法
        databuf.appendChar(methodCount);
        writeMethods(c.members().elems);


        int accountIdx = beginAttrs();
        int account = 0;


        // 源文件
        int alenIdx = writeAttr(names.SourceFile);
        // 类名
        databuf.appendChar(c.pool.put(names.fromString(c.name.toString() + ".java")));
        endAttr(alenIdx);
        account++;

        //
        account += writeFlagAttrs(c.flags());
        
        poolbuf.appendInt(JAVA_MAGIC);
        poolbuf.appendChar(0);
        poolbuf.appendChar(51);
        
        writePool(c.pool);
        
        endAttrs(accountIdx, account);
        poolbuf.appendBytes(databuf.elems, 0, databuf.length);
        try {
            outputStream.write(poolbuf.elems, 0, poolbuf.length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writePool(Pool pool) {

        int poolCountIdx = poolbuf.length;
        poolbuf.appendChar(0);
        int i = 1;
        while (i < pool.pp) {
            Object value = pool.pool[i];
            if (value instanceof Pool.Method) {
                value = ((Pool.Method)value).m;
            } else if (value instanceof Pool.Variable) {
                value = ((Pool.Variable)value).v;
            }

            if (value instanceof Symbol.MethodSymbol) {
                Symbol.MethodSymbol m = (Symbol.MethodSymbol)value;
                poolbuf.appendByte((m.owner.flags() & INTERFACE) != 0
                        ? CONSTANT_InterfaceMethodref
                        : CONSTANT_Methodref);
                poolbuf.appendChar(pool.put(m.owner));
                poolbuf.appendChar(pool.put(nameType(m)));
            } else if (value instanceof Symbol.VarSymbol) {
                Symbol.VarSymbol v = (Symbol.VarSymbol)value;
                poolbuf.appendByte(CONSTANT_Fieldref);
                poolbuf.appendChar(pool.put(v.owner));
                poolbuf.appendChar(pool.put(nameType(v)));
            } else if (value instanceof Name) {
                poolbuf.appendByte(CONSTANT_Utf8);
                byte[] bs = ((Name)value).toUtf();
                poolbuf.appendChar(bs.length);
                poolbuf.appendBytes(bs, 0, bs.length);


            } else if (value instanceof Symbol.ClassSymbol) {
                Symbol.ClassSymbol c = (Symbol.ClassSymbol)value;
                if (c.owner.kind == TYP) pool.put(c.owner);
                poolbuf.appendByte(CONSTANT_Class);
                if (c.type.tag == ARRAY) {
                    poolbuf.appendChar(pool.put(typeSig(c.type)));
                } else {
                    poolbuf.appendChar(pool.put(names.fromUtf(externalize(c.flatName))));

                }
            } else if (value instanceof NameAndType) {
                NameAndType nt = (NameAndType)value;
                poolbuf.appendByte(CONSTANT_NameandType);
                poolbuf.appendChar(pool.put(nt.name));
                poolbuf.appendChar(pool.put(typeSig(nt.type)));
            } else if (value instanceof Integer) {
                poolbuf.appendByte(CONSTANT_Integer);
                poolbuf.appendInt(((Integer)value).intValue());
            } else if (value instanceof Long) {
                poolbuf.appendByte(CONSTANT_Long);
                poolbuf.appendLong(((Long)value).longValue());
                i++;
            } else if (value instanceof Float) {
                poolbuf.appendByte(CONSTANT_Float);
                poolbuf.appendFloat(((Float)value).floatValue());
            } else if (value instanceof Double) {
                poolbuf.appendByte(CONSTANT_Double);
                poolbuf.appendDouble(((Double)value).doubleValue());
                i++;
            } else if (value instanceof String) {
                poolbuf.appendByte(CONSTANT_String);
                poolbuf.appendChar(pool.put(names.fromString((String)value)));
            }
            i ++;
        }

        putChar(poolbuf, poolCountIdx, pool.pp);
    }

    public static byte[] externalize(Name name) {
        return externalize(name.getByteArray(), name.getByteOffset(), name.getByteLength());
    }

    private static byte[] externalize(byte[] buf, int offset, int len) {
        byte[] translated = new byte[len];
        for (int j = 0; j < len; j++) {
            byte b = buf[offset + j];
            if (b == '.') translated[j] = (byte) '/';
            else translated[j] = b;
        }
        return translated;
    }

    private NameAndType nameType(Symbol sym) {
        return new NameAndType(fieldName(sym), sym.externalType(types));
    }

    /**
     * 写方法
     * @param e
     */
    private void writeMethods(Scope.Entry e) {
        ArrayList<Symbol.MethodSymbol> methods = new ArrayList<>();
        for (Scope.Entry i = e; i != null; i = i.sibling) {
            if (i.sym.kind == MTH) {
                methods.add((Symbol.MethodSymbol) i.sym);
            }
        }

//        ArrayList<Symbol.MethodSymbol> newMETHOD = new ArrayList<>();
//        newMETHOD.add(methods.get(1));
//        newMETHOD.add(methods.get(2));
//        newMETHOD.add(methods.get(0));

        for (Symbol.MethodSymbol method : methods) {
            writeMethod(method);
        }
    }

    private void writeMethod(Symbol.MethodSymbol method) {
        int flags = adjustFlags(method.flags());
        if (method.name == names.init) {
            flags = 1073741825;
        }
        databuf.appendChar(flags);

        databuf.appendChar(pool.put(fieldName(method)));
        databuf.appendChar(pool.put(typeSig(method.externalType(types))));

        int accountIdx = beginAttrs();
        int account = 0;

        if (method.code != null) {
            int alenIdx = writeAttr(names.Code);
            writeCode(method.code);
            method.code = null;
            endAttr(alenIdx);
            account++;
        }

        account += writeMemberAttrs(method);
        account += writeParameterAttrs(method);
        endAttrs(accountIdx, account);
    }

    private int writeParameterAttrs(Symbol.MethodSymbol method) {

        int attrCount = 0;

        if (method.params != null) {
            for (Symbol.VarSymbol param : method.params) {

            }
        }

        return attrCount;
    }

    private void writeCode(Code code) {
        databuf.appendChar(code.max_stack);
        databuf.appendChar(code.max_locals);
        databuf.appendInt(code.cp);
        databuf.appendBytes(code.code, 0, code.cp);
        databuf.appendChar(0);

        int accountIdx = beginAttrs();
        int account = 0;



        endAttrs(accountIdx, account);

    }

    private int adjustFlags(long flags) {
        int result = (int) flags;


        return result;
    }

    /**
     * 写变量
     * @param e
     */
    private void writeFields(Scope.Entry e) {
        ArrayList<Symbol.VarSymbol> vars = new ArrayList<>();

        for (Scope.Entry i = e; i != null; i = i.sibling) {
            if (i.sym.kind == VAR) vars.add((Symbol.VarSymbol) i.sym);
        }

        for (int i = 0; i < vars.size(); i ++) {
            writeField(vars.get(i));
        }
    }

    private void writeField(Symbol.VarSymbol v) {
        int flags = (int) v.flags();
        databuf.appendChar(flags);

        databuf.appendChar(pool.put(fieldName(v)));
        databuf.appendChar(pool.put(typeSig(v.enasure(types))));

        int accountIdx = beginAttrs();
        int account = 0;
        if (v.getConstValue() != null) {
            int alenindex = writeAttr(names.ConstantValue);
            databuf.appendChar(pool.put(v.getConstValue()));
            endAttr(alenindex);
            account++;
        }
        account += writeMemberAttrs(v);
        endAttrs(accountIdx, account);
    }

    private void endAttrs(int accountIdx, int account) {
        putChar(databuf, accountIdx - 2, account);
    }

    private void putChar(ByteBuffer buf, int accountIdx, int account) {
        buf.elems[accountIdx] = (byte) ((account >> 8) & 0xFF);
        buf.elems[accountIdx+1] = (byte) ((account) & 0xFF);
    }

    private int writeMemberAttrs(Symbol sym) {
        int account = writeFlagAttrs(sym.flags());

        long flags = sym.flags();



        return account;
    }

    private int writeFlagAttrs(long flags) {
        int account = 0;

        if ((flags & VARARGS) != 0) {

        }

        return account;
    }

    private void endAttr(int index) {
        putInt(databuf, index - 4, databuf.length - index);
    }

    private void putInt(ByteBuffer buf, int adr, int x) {
        buf.elems[adr] = (byte) ((x >> 24) & 0xFF);
        buf.elems[adr+1] = (byte) ((x >> 16) & 0xFF);
        buf.elems[adr+2] = (byte) ((x >> 8) & 0xFF);
        buf.elems[adr+3] = (byte) ((x) & 0xFF);
    }

    private int writeAttr(Name attr) {
        databuf.appendChar(pool.put(attr));
        databuf.appendInt(0);
        return databuf.length;
    }

    private int beginAttrs() {
        databuf.appendChar(0);
        return databuf.length;
    }

    private Name typeSig(Type type) {
        assembleSig(type);
        Name n = sigbuf.toName(names);
        sigbuf.reset();
        return n;
    }

    private void assembleSig(Type type) {
        switch (type.tag) {
            case BYTE:
                sigbuf.appendByte('B');
                break;
            case SHORT:
                sigbuf.appendByte('S');
                break;
            case CHAR:
                sigbuf.appendByte('C');
                break;
            case INT:
                sigbuf.appendByte('I');
                break;
            case LONG:
                sigbuf.appendByte('J');
                break;
            case FLOAT:
                sigbuf.appendByte('F');
                break;
            case DOUBLE:
                sigbuf.appendByte('D');
                break;
            case BOOLEAN:
                sigbuf.appendByte('Z');
                break;
            case VOID:
                sigbuf.appendByte('V');
                break;
            case CLASS:
                sigbuf.appendByte('L');
                assembleClassSig(type);
                sigbuf.appendByte(';');
                break;
            case ARRAY:
                Type.ArrayType at = (Type.ArrayType) type;
                sigbuf.appendByte('[');
                assembleSig(at.elemtype);
                break;
            case METHOD:
                Type.MethodType mt = (Type.MethodType)type;
                sigbuf.appendByte('(');
                assembleSig(mt.argTypes);
                sigbuf.appendByte(')');
                assembleSig(mt.restType);

                break;

            case TYPEVAR:
                sigbuf.appendByte('T');
                sigbuf.appendName(type.tsym.name);
                sigbuf.appendByte(';');
                break;
            case FORALL:
                Type.ForAll ft = (Type.ForAll)type;
//                assembleParamsSig(ft.tvars);
                assembleSig(ft.qtype);
                break;
            case UNINITIALIZED_THIS:
            case UNINITIALIZED_OBJECT:
                // we don't yet have a spec for uninitialized types in the
                // local variable table
                assembleSig(types.erasure(((UninitializedType)type).qtype));
                break;
            default:

        }
        }

    private void assembleClassSig(Type type) {
        Type.ClassType ct = (Type.ClassType)type;
        Symbol.ClassSymbol c = (Symbol.ClassSymbol)ct.tsym;

        Type outer = ct.getEnclosingType();

        sigbuf.appendBytes(externalize(c.flatName));
    }

    private void assembleSig(List<Type> argTypes) {
        for (Type argType : argTypes) {
            assembleSig(argType);
        }
    }



    private Name fieldName(Symbol sym) {
        return sym.name;
    }

    public void writeClass(JCTree tree) {

    }
}
