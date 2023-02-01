package com.myj.tools.javac.util;

import com.myj.tools.javac.code.Symbol;
import com.myj.tools.javac.code.Type;
import com.myj.tools.javac.tree.JCTree;

import java.util.List;

import static com.myj.tools.javac.code.Flags.STATIC;
import static com.myj.tools.javac.code.TypeTags.*;

public class Types {

    public static final Context.Key<Types> typesKey = new Context.Key<Types>();



    public static Types instance(Context context) {
        Types types = context.get(typesKey);
        if (null == types) {
            types = new Types(context);
        }
        return types;
    }

    private Types(Context context) {
        context.put(typesKey, this);
    }

    public Type erasure(Type type) {
        if (type.tag <= lastBaseTag) {
            return type;
        } else {
            return erasure.visit(type, false);
        }
    }

    SimpleVisitor<Type, Boolean> erasure = new SimpleVisitor<Type, Boolean>() {

        @Override
        public Type visitType(Type t, Boolean aBoolean) {
            if (t.tag <= lastBaseTag) {
                return t;
            } else {
                return t.map(aBoolean ? erasureRecFun : erasureFun);
            }
        }

        @Override
        public Type visitClassType(Type.ClassType classType, Boolean aBoolean) {
            return null;
        }


    };

    private Type.Mapping erasureRecFun = new Type.Mapping("erasureRecursive") {
        public Type apply(Type t) { return erasureRecursive(t); }
    };

    private Type.Mapping erasureFun = new Type.Mapping("erasure") {
        public Type apply(Type t) { return erasure(t); }
    };

    public Type erasureRecursive(Type t) {
        return erasure(t, true);
    }

    private Type erasure(Type t, boolean b) {
        if (t.tag <= lastBaseTag) {
            return t;
        } else {
            return erasure.visit(t, b);
        }
    }

    public Type capture(Type ownType) {
        if (ownType.tag != CLASS) {
            return ownType;
        }
        return null;
    }

    public boolean isAssignable(Type owntype, Type pt) {

        if (owntype.tag <= INT && owntype.constValue() != null) {
            int value = ((Number) owntype.constValue()).intValue();
            switch (pt.tag) {
                case BYTE:
                    if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE) {
                        return true;
                    }
                    break;
                case CHAR:
                    if (Character.MIN_VALUE <= value && value <= Character.MAX_VALUE)
                        return true;
                    break;
                case SHORT:
                    if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE)
                        return true;
                    break;
                case INT:
                    return true;
            }
        }
        return isConvertible(owntype, pt);
    }

    private boolean isConvertible(Type owntype, Type pt) {
        if (owntype.tag == ARRAY && pt.tag == ARRAY) {

        } else if (isSubtype(owntype, pt)) {
            return true;
        }
        return false;
    }

    private boolean isSubtype(Type owntype, Type pt) {
        if (owntype == pt) {
            return true;
        }

        return false;
    }

    public Type supertype(Type site) {
        return supertype.visit(site);
    }

    private UnaryVisitor<Type> supertype = new UnaryVisitor<Type>() {

        @Override
        public Type visitType(Type t, Void unused) {
            return null;
        }

        @Override
        public Type visitClassType(Type.ClassType t, Void ignored) {
            if (t.superType_field == null) {

            }
            return t.superType_field;
        }
    };

    public Type memberType(Type site, Symbol sym) {
        return (sym.flags() & STATIC) != 0 ? sym.type : sym.type;
    }


    public interface Visitor<R, S> {

        R visitType(Type t, S s);

        R visitMethodType(Type.MethodType methodType, S s);

        R visitClassType(Type.ClassType classType, S s);

        R visitForAll(Type.ForAll forAll, S s);
    }

    public static abstract class SimpleVisitor<R, S> extends DefaultTypeVisitor<R, S> {

        @Override
        public R visitForAll(Type.ForAll forAll, S s) {
            return visit(forAll.qtype, s);
        }
    }

    public static abstract class DefaultTypeVisitor<R, S> implements Types.Visitor<R, S> {
        final public R visit(Type t, S s) {return t.accept(this, s);}

        @Override
        public R visitMethodType(Type.MethodType methodType, S s) {
            return visitType(methodType, s);
        }
    }

    public static abstract class UnaryVisitor<R> extends SimpleVisitor<R,Void> {
        final public R visit(Type t) { return t.accept(this, null); }
    }




}
