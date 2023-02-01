package com.myj.tools.javac.code;

import com.myj.tools.javac.util.Filter;
import com.myj.tools.javac.util.Name;

import static com.myj.tools.javac.code.Kinds.TYP;

public class Scope {


    private static final int INITIAL_SIZE = 0x10;
    
    private static final Entry sentinel = new Entry(null, null, null, null);


    int nelems = 0;

    public Scope next;

    /** The scope's owner.
     */
    public Symbol owner;

    /** A hash table for the scope's entries.
     */
    Entry[] table;

    int shared;

    public Entry elems;

    int hashMask;
    
    static final Filter<Symbol> noFilter = new Filter<Symbol>() {
        @Override
        public boolean accepts(Symbol symbol) {
            return true;
        }
    };

    public Scope(Symbol owner) {
        this(null, owner, new Entry[INITIAL_SIZE]);
    }

    public Scope(Scope next, Symbol owner, Entry[] table) {
        this.next = next;
        this.owner = owner;
        this.table = table;
        this.hashMask = table.length - 1;
    }

    public Scope(Scope next, Symbol owner, Entry[] table, int nelems) {
        this(next, owner, table);
        this.nelems = nelems;
    }

    public Entry lookup(Name className) {
        return lookup(className, noFilter);
    }

    private Entry lookup(Name className, Filter<Symbol> noFilter) {
        Entry e = table[getIndex(className)];
        if (e == null || e == sentinel) {
            return sentinel;
        }
        while (e.scope != null && (e.sym.name != className || !noFilter.accepts(e.sym)))
            e = e.shadowed;
        return e;
    }

    private int getIndex(Name className) {

        int hashCode = className.hashCode();
        int i = hashCode & hashMask;
        int x = hashMask - ((hashCode + (hashCode >> 16)) << 1);
        int d = -1;

        for (;;) {
            Entry e = table[i];
            if (e == null) {
                return d >= 0 ? d : i;
            }
            
            if (e == sentinel) {
                if (d < 0) {
                    d = i;
                }
            } else if (e.sym.name == className) {
                return i;
            }
            i = (i + x) & hashMask;
        }

    }

    public void enter(Symbol sym) {
        enter(sym, this);
    }

    public void enter(Symbol sym, Scope s) {
        enter(sym, s, s);
    }

    private void enter(Symbol sym, Scope s, Scope origin) {
        if (nelems * 3 >= hashMask * 2)
            dble();

        int hash = getIndex(sym.name);
        Entry old = table[hash];

        if (old == null) {
            old = sentinel;
            nelems ++;
        }
        Entry e = makeEntry(sym, old, elems, s, origin);
        table[hash] = e;
        elems = e;
    }

    private Entry makeEntry(Symbol sym, Entry old, Entry elems, Scope s, Scope origin) {
        return new Entry(sym, old, elems, s);
    }

    /**
     * 扩容
     */
    private void dble() {

        Entry[] oldTable = table;
        Entry[] newTable = new Entry[oldTable.length * 2];

        for (Scope s = this; s != null; s = s.next) {
            if (s.table == oldTable) {
                s.table = newTable;
                s.hashMask = newTable.length - 1;
            }
        }

        int n = 0;
        for (int i = oldTable.length; --i >= 0;) {
            Entry e = oldTable[i];
            if (e != null && e != sentinel) {
                table[getIndex(e.sym.name)] = e;
                n ++;
            }
        }
        nelems = n;
    }

    public void enterIfAbsent(Symbol sym) {
        Entry e = lookup(sym.name);
        while (e.scope == this && e.sym.kind != sym.kind) e = e.next();
        if (e.scope != this) enter(sym);
    }

    public Scope dupUnshared() {
        return new Scope(this, this.owner, this.table.clone(), this.nelems);
    }

    public Scope leave() {
        if (table != next.table) return next;

        while (elems != null) {
            int hash = getIndex(elems.sym.name);
            Entry e = table[hash];

            table[hash] = elems.shadowed;
            elems = elems.sibling;
        }

        next.shared --;
        next.nelems = nelems;
        return next;
    }

    public Scope dup() {
        return dup(this.owner);
    }

    private Scope dup(Symbol owner) {
        Scope result = new Scope(this, owner, this.table, this.nelems);
        shared++;
        return result;
    }


    public static class ImportScope extends Scope {

        public ImportScope(Symbol owner) {
            super(owner);
        }
    }

    public static class StarImportScope extends ImportScope {

        public StarImportScope(Symbol owner) {
            super(owner);
        }

        public void importAll(Scope scope) {
            for (Entry e = scope.elems; e != null; e = e.sibling) {
                if (e.sym.kind == TYP && !includes(e.sym)) {
                    enter(e.sym, scope);
                }
            }
        }

        private boolean includes(Symbol sym) {
            for (Entry e = lookup(sym.name); e.scope == this; e = e.next()) {
                if (e.sym == sym) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class Entry {
        
        public Symbol sym;
        
        private Entry shadowed;
        
        public Entry sibling;
        
        public Scope scope;

        public Entry(Symbol sym, Entry shadowed, Entry sibling, Scope scope) {
            this.sym = sym;
            this.shadowed = shadowed;
            this.sibling = sibling;
            this.scope = scope;
        }

        public Entry next() {
            return shadowed;
        }
    }

    public static class ErrorScope extends Scope {
        public ErrorScope(Symbol error) {
            super(error);
        }
    }

    public static class DelegatedScope extends Scope {
        Scope delegatee;

        public static final Entry[] emptyTable = new Entry[0];

        public DelegatedScope(Scope outer) {
            super(outer, outer.owner, emptyTable);
            delegatee = outer;
        }
    }
}
