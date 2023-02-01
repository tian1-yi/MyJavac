package com.myj.tools.javac.code;

import com.myj.tools.javac.comp.AttrContext;
import com.myj.tools.javac.comp.Env;
import com.myj.tools.javac.jvm.Code;
import com.myj.tools.javac.jvm.Pool;
import com.myj.tools.javac.util.Element;
import com.myj.tools.javac.util.JavaFileObject;
import com.myj.tools.javac.util.Name;
import com.myj.tools.javac.util.Types;

import java.util.List;

import static com.myj.tools.javac.code.Flags.INTERFACE;
import static com.myj.tools.javac.code.Flags.NOOUTERTHIS;
import static com.myj.tools.javac.code.Kinds.*;
import static com.myj.tools.javac.code.TypeTags.CLASS;
import static com.myj.tools.javac.code.TypeTags.TYPEVAR;

public abstract class Symbol implements Element {

    public int kind;

    public Name name;

    public long flags_field;

    /** The type of this symbol.
     */
    public Type type;

    /** The owner of this symbol.
     */
    public Symbol owner;

    /** The completer of this symbol.
     */
    public Completer completer;

    /** A cache for the type erasure of this symbol.
     */
    public Type erasure_field;





    public Symbol(int kind, long flags, Name name, Type type, Symbol owner) {
        this.kind = kind;
        this.flags_field = flags;
        this.type = type;
        this.owner = owner;
        this.completer = null;
        this.erasure_field = null;

        this.name = name;
    }

    public void complete() {

        if (completer != null) {
            Completer c = completer;
            completer = null;
            c.complete(this);
        }
    }



    public boolean isLocal() {
        return
                (owner.kind & (VAR | MTH)) != 0 || (owner.kind == TYP && owner.isLocal());
    }

    public long flags() {
        return flags_field;
    }

    public Scope members() {
        return null;

    }

    public Type enasure(Types types) {
        if (erasure_field == null) {
            erasure_field = types.erasure(type);
        }
        return erasure_field;
    }

    public boolean isConstructor() {
        return name == name.table.names.init;
    }

    public Type externalType(Types types) {
        Type t = enasure(types);
        if (name == name.table.names.init && owner.hasOuterInstance()) {
            return null;
        } else {
            return t;
        }
    }

    private boolean hasOuterInstance() {
        return type.getEnclosingType().tag == CLASS && (flags() & (INTERFACE | NOOUTERTHIS)) == 0;
    }


    public static class TypeSymbol extends Symbol {

        public TypeSymbol(long flags, Name name, Type type, Symbol owner) {
            super(TYP, flags, name, type, owner);
        }

        static public Name formFullName(Name name, Symbol owner) {
            if (owner == null) return name;
            if (((owner.kind != ERR)) && ((owner.kind & (VAR | MTH)) != 0) || (owner.kind == TYP && owner.type.tag == TYPEVAR)) return name;
            Name qname = owner.getQualifiedName();
            if (qname == null || qname ==  qname.table.names.empty)
                return name;
            else return qname.append('.', name);
        }

        public static Name formFlatName(Name className, Symbol owner) {
            char sep = owner.kind == TYP ? '$' : '.';
            Name prefix = owner.flatName();
            if (prefix == null || prefix == prefix.table.names.empty) {
                return className;
            } else {
                return prefix.append(sep, className);
            }

        }


    }

    public Name flatName() {
        return getQualifiedName();
    }

    public Name getQualifiedName() {
        return name;
    }

    public static class PackageSymbol extends TypeSymbol {
        public Scope members_field;
        public Name fullname;
        public ClassSymbol package_info;

        public PackageSymbol(Name name, Type type, Symbol owner) {
            super(0, name, type, owner);
            this.kind = PCK;
            this.members_field = null;
            this.fullname = formFullName(name, owner);
        }

        public PackageSymbol(Name name, Symbol owner) {
            this(name, null, owner);
            this.type = new Type.PackageType(this);
        }


        public Name getQualifiedName() {
            return fullname;
        }

        public Scope members() {
            if (completer != null) complete();
            return members_field;
        }


    }

    public static class ClassSymbol extends TypeSymbol {

        public Scope members_field;

        public Name flatName;

        public JavaFileObject classFile;

        public Name fullName;
        public Pool pool;

        public ClassSymbol(long flags, Name name, Symbol owner) {
            this(flags, name, new Type.ClassType(Type.noType, null, null), owner);
            this.type.tsym = this;
        }

        public ClassSymbol(long flags, Name name, Type type, Symbol owner) {
            super(flags, name, type, owner);
            this.members_field = null;
            this.flatName = formFlatName(name, owner);
            this.fullName = formFullName(name, owner);
        }

        @Override
        public Name flatName() {
            return flatName;
        }

        @Override
        public Scope members() {
            if (completer != null) complete();
            return members_field;
        }
    }

    public static class VarSymbol extends Symbol {

        public int adr = - 1;

        public VarSymbol(long flags, Name name, Type type, Symbol owner) {
            super(VAR, flags, name, type, owner);
        }

        private Object data;

        public Object getConstValue() {

            return null;
        }

        public VarSymbol clone(Symbol newowner) {
            VarSymbol v = new VarSymbol(flags_field, name, type, newowner);
            v.adr = adr;
            v.data = data;
            return v;
        }
    }

    public static class DelegateSymbol extends Symbol {
        protected Symbol other;

        public DelegateSymbol(Symbol other) {
            super(other.kind, other.flags_field, other.name, other.type, other.owner);
            this.other = other;
        }
    }


    public static class MethodSymbol extends Symbol {

        public Code code;

        public List<VarSymbol> params = null;

        public MethodSymbol(long flags, Name name, Type type, Symbol owner) {
            super(MTH, flags, name, type, owner);
        }
    }

    public static interface Completer {

        void complete(Symbol sym);


    }

}
