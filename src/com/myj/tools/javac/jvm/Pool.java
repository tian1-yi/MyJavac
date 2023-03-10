package com.myj.tools.javac.jvm;

import com.myj.tools.javac.code.Symbol;

import java.util.HashMap;
import java.util.Map;

public class Pool {
    public static final int MAX_ENTRIES = 0xFFFF;
    public static final int MAX_STRING_LENGTH = 0xFFFF;

    /** Index of next constant to be entered.
     */
    int pp;

    /** The initial pool buffer.
     */
    Object[] pool;

    /** A hashtable containing all constants in the pool.
     */
    Map<Object,Integer> indices;

    /** Construct a pool with given number of elements and element array.
     */
    public Pool(int pp, Object[] pool) {
        this.pp = pp;
        this.pool = pool;
        this.indices = new HashMap<Object,Integer>(pool.length);
        for (int i = 1; i < pp; i++) {
            if (pool[i] != null) indices.put(pool[i], i);
        }
    }

    /** Construct an empty pool.
     */
    public Pool() {
        this(1, new Object[64]);
    }

    /** Return the number of entries in the constant pool.
     */
    public int numEntries() {
        return pp;
    }

    /** Remove everything from this pool.
     */
    public void reset() {
        pp = 1;
        indices.clear();
    }

    /** Double pool buffer in size.
     */
    private void doublePool() {
        Object[] newpool = new Object[pool.length * 2];
        System.arraycopy(pool, 0, newpool, 0, pool.length);
        pool = newpool;
    }

    /** Place an object in the pool, unless it is already there.
     *  If object is a symbol also enter its owner unless the owner is a
     *  package.  Return the object's index in the pool.
     */
    public int put(Object value) {
        if (value instanceof Symbol.MethodSymbol)
            value = new Method((Symbol.MethodSymbol)value);
        else if (value instanceof Symbol.VarSymbol)
            value = new Variable((Symbol.VarSymbol)value);
//      assert !(value instanceof Type.TypeVar);
        Integer index = indices.get(value);
        if (index == null) {
//          System.err.println("put " + value + " " + value.getClass());//DEBUG
            index = pp;
            indices.put(value, index);
            if (pp == pool.length) doublePool();
            pool[pp++] = value;
            if (value instanceof Long || value instanceof Double) {
                if (pp == pool.length) doublePool();
                pool[pp++] = null;
            }
        }
        return index.intValue();
    }

    /** Return the given object's index in the pool,
     *  or -1 if object is not in there.
     */
    public int get(Object o) {
        Integer n = indices.get(o);
        return n == null ? -1 : n.intValue();
    }

    static class Method extends Symbol.DelegateSymbol {
        Symbol.MethodSymbol m;
        Method(Symbol.MethodSymbol m) {
            super(m);
            this.m = m;
        }
        public boolean equals(Object other) {
            if (!(other instanceof Method)) return false;
            Symbol.MethodSymbol o = ((Method)other).m;
            return
                    o.name == m.name &&
                            o.owner == m.owner &&
                            o.type.equals(m.type);
        }
        public int hashCode() {
            return
                    m.name.hashCode() * 33 +
                            m.owner.hashCode() * 9 +
                            m.type.hashCode();
        }
    }

    static class Variable extends Symbol.DelegateSymbol {
        VarSymbol v;
        Variable(VarSymbol v) {
            super(v);
            this.v = v;
        }
        public boolean equals(Object other) {
            if (!(other instanceof Variable)) return false;
            VarSymbol o = ((Variable)other).v;
            return
                    o.name == v.name &&
                            o.owner == v.owner &&
                            o.type.equals(v.type);
        }
        public int hashCode() {
            return
                    v.name.hashCode() * 33 +
                            v.owner.hashCode() * 9 +
                            v.type.hashCode();
        }
    }
}
