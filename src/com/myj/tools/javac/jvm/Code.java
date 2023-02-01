package com.myj.tools.javac.jvm;

import com.myj.tools.javac.code.Symbol;
import com.myj.tools.javac.code.Type;
import com.myj.tools.javac.util.*;

import java.util.List;

import static com.myj.tools.javac.code.TypeTags.*;
import static com.myj.tools.javac.jvm.ByteCodes.*;
import static com.myj.tools.javac.jvm.UninitializedType.UNINITIALIZED_OBJECT;
import static com.myj.tools.javac.jvm.UninitializedType.UNINITIALIZED_THIS;
import static com.myj.tools.javac.util.Position.NOPOS;

public class Code {

    final Symbol.MethodSymbol meth;

    final boolean fatcode;

    StackMapFormat stackMap;

    Symtab syms;

    Types types;

    final Pool pool;

    public int nextreg = 0;

    public int max_locals = 0;

    LocalVar[] lvar;

    State state;

    int cp = 0;

    public int max_stack = 0;

    boolean alive = true;

    public byte[] code = new byte[64];

    int pendingStatPos = NOPOS;

    public Code(Symbol.MethodSymbol meth, boolean fatcode, StackMapFormat stackMap, Symtab syms, Types types, Pool pool) {
        this.meth = meth;
        this.fatcode = fatcode;
        this.stackMap = stackMap;
        this.syms = syms;
        this.types = types;
        this.pool = pool;
        this.lvar = new LocalVar[20];
        this.state = new State();
    }

    public static int typeCode(Type type) {
        switch (type.tag) {
            case BYTE: return BYTEcode;
            case SHORT: return SHORTcode;
            case CHAR: return CHARcode;
            case INT: return INTcode;
            case LONG: return LONGcode;
            case FLOAT: return FLOATcode;
            case DOUBLE: return DOUBLEcode;
            case BOOLEAN: return BYTEcode;
            case VOID: return VOIDcode;
            case CLASS:
            case ARRAY:
            case METHOD:
            case BOT:
            case TYPEVAR:
            case UNINITIALIZED_THIS:
            case UNINITIALIZED_OBJECT:
                return OBJECTcode;
            default: throw new AssertionError("typecode " + type.tag);
        }
    }

    public static int truncate(int tc) {
        switch (tc) {
            case BYTEcode: case SHORTcode: case CHARcode: return INTcode;
            default: return tc;
        }
    }


    public void emitop0(int op) {
        emitop(op);
        if (!alive) return;
        switch (op) {
            case iconst_m1:
            case iconst_0:
            case iconst_1:
            case iconst_2:
            case iconst_3:
            case iconst_4:
            case iconst_5:
            case iload_0:
            case iload_1:
            case iload_2:
            case iload_3:
                state.push(syms.intType);
                break;
            case aload_0:
                state.push(lvar[0].sym.type);
                break;
            case istore_0:
            case istore_1:
            case istore_2:
            case istore_3:
            case fstore_0:
            case fstore_1:
            case fstore_2:
            case fstore_3:
            case astore_0:
            case astore_1:
            case astore_2:
            case astore_3:
            case pop:
            case lshr:
            case lshl:
            case lushr:
                state.pop(1);
                break;
        }
    }

    private void emitop(int op) {

        if (alive) {
            if (pendingStatPos != NOPOS) {
                maskStatBegin();
            }
            emit1(op);
        }

    }

    private void maskStatBegin() {

    }

    private void emit1(int op) {
        if (!alive) return;

        if (cp == code.length) {
            byte[] newcode = new byte[cp * 2];
            System.arraycopy(code, 0, newcode, 0, cp);
            code = newcode;
        }
        code[cp++] = (byte) op;
    }

    public int newLocal(Symbol.VarSymbol v) {
        int reg = v.adr = newLocal(v.enasure(types));
        addLocalVar(v);
        return reg;
    }

    private void addLocalVar(Symbol.VarSymbol v) {
        int adr = v.adr;
        // 扩容
        if (adr + 1 >= lvar.length) {
            int newlenth = lvar.length << 1;
            if (newlenth <= adr) newlenth = adr + 10;
            LocalVar[] newLvar = new LocalVar[newlenth];
            System.arraycopy(lvar, 0, newLvar, 0, lvar.length);
            lvar = newLvar;
        }

        lvar[adr] = new LocalVar(v);
        state.defined.excl(adr);
    }

    private int newLocal(Type type) {
        return newLocal(typecode(type));
    }

    private int newLocal(int typecode) {
        int reg = nextreg;
        int w = width(typecode);
        nextreg = reg + w;
        if (nextreg > max_locals) max_locals = nextreg;
        return reg;
    }

    public static int width(int typecode) {
        switch (typecode) {
            case LONGcode: case DOUBLEcode: return 2;
            case VOIDcode: return 0;
            default: return 1;
        }
    }

    private static int typecode(Type type) {
        switch (type.tag) {
            case BYTE: return BYTEcode;
            case SHORT: return SHORTcode;
            case CHAR: return CHARcode;
            case INT: return INTcode;
            case LONG: return LONGcode;
            case FLOAT: return FLOATcode;
            case DOUBLE: return DOUBLEcode;
            case BOOLEAN: return BYTEcode;
            case VOID: return VOIDcode;
            case CLASS:
            case ARRAY:
            case METHOD:
            case BOT:
            case TYPEVAR:
            case UNINITIALIZED_THIS:
            case UNINITIALIZED_OBJECT:
                return OBJECTcode;
            default:
                return -10000;
        }
    }

    public void setDefined(int newLocal) {
        LocalVar localVar = lvar[newLocal];
        if (localVar == null) {
            state.defined.excl(newLocal);
        } else {
            state.defined.incl(newLocal);
            if (cp < Character.MAX_VALUE) {
                if (localVar.start_pc == Character.MAX_VALUE) {
                    localVar.start_pc = (char) cp;
                }
            }
        }
    }

    public void emitop2(int op, int od) {
        emitop(op);
        if (!alive) return;
        emit2(od);


    }

    public void emitInvokespecial(int meth, Type.MethodType mType) {
        int argsize = width(mType.getParameterTypes());
        emitop(invokespecial);
        if (!alive) return;
        emit2(meth);
        Symbol sym = (Symbol) pool.pool[meth];
        state.pop(argsize);
        if (sym.isConstructor())
            state.markInitialized((UninitializedType) state.peek());
        state.pop(1);
        state.push(mType.getReturnType());
    }

    private void emit2(int od) {
        if (!alive) return;
        if ((cp + 2 > code.length)) {
            emit1(od >> 8);
            emit1(od);
        } else {
            code[cp++] = (byte) (od >> 8);
            code[cp++] = (byte) od;
        }
    }

    public static int width(List<Type> parameterTypes) {
        int w = 0;
        for (Type type : parameterTypes) {
            w += width(type);
        }
        return w;
    }

    public void setBegin(int pos) {
        if (pos != NOPOS) {
            this.pendingStatPos = pos;
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public enum StackMapFormat {
        NONE,
        JRS202 {
            Name getAttributeName(Names names) {
                return names.StackMapTable;
            }
        };

        Name getAttributeName(Names names) {
            return names.empty;
        }
    }

    static class LocalVar {
        final Symbol.VarSymbol sym;

        final char reg;

        char start_pc = Character.MAX_VALUE;

        char length = Character.MAX_VALUE;

        LocalVar(Symbol.VarSymbol v) {
            this.sym = v;
            this.reg = (char) v.adr;
        }

        public LocalVar dup() {
            return new LocalVar(sym);
        }

    }

    class State implements Cloneable {
        Bits defined;

        /** The (types of the) contents of the machine stack. */
        Type[] stack;

        /** The first stack position currently unused. */
        int stacksize;

        /** The numbers of registers containing locked monitors. */
        int[] locks;
        int nlocks;

        State() {
            defined = new Bits();
            stack = new Type[16];
        }

        public void push(Type t) {
            switch (t.tag) {
                case VOID:
                    return;
                case BYTE:
                case CHAR:
                case SHORT:
                case BOOLEAN:
                    t = syms.intType;
                    break;
                default:
                    break;
            }

            if (stacksize + 2 >= stack.length) {
                Type[] newstack = new Type[2*stack.length];
                System.arraycopy(stack, 0, newstack, 0, stack.length);
                stack = newstack;
            }

            stack[stacksize++] = t;

            switch (width(t)) {
                case 1:
                    break;
                case 2:
                    stack[stacksize++] = null;
                    break;
                default:
            }
            if (stacksize > max_stack) {
                max_stack = stacksize;
            }
        }

        public void pop(int n) {
            while (n > 0) {
                stack[--stacksize] = null;
                n --;
            }
        }

        public void markInitialized(UninitializedType old) {
            Type newType = old.initializedType();

            for (int i = 0; i  < stacksize; i ++) {
                if (stack[i] == old) stack[i] = newType;
            }

            for (int i = 0; i  < lvar.length; i ++) {
                LocalVar lv = lvar[i];

                if (lv != null && lv.sym.type == old) {
                    Symbol.VarSymbol sym = lv.sym;
                    sym = sym.clone(sym.owner);
                    sym.type = newType;
                    LocalVar newlv = lvar[i] = new LocalVar(sym);
                    newlv.start_pc = lv.start_pc;
                }
            }
        }

        public Type peek() {
            return stack[stacksize-1];
        }
    }

    public static int width(Type t) {
        return t == null ? 1 : width(typecode(t));
    }

}
