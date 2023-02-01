package com.myj.tools.javac.code;

import com.myj.tools.javac.util.Types;
import com.sun.org.apache.regexp.internal.RE;
import sun.reflect.generics.visitor.Visitor;

import javax.lang.model.type.NoType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

import java.util.List;

import static com.myj.tools.javac.code.TypeTags.*;

public class Type {


    public static final JCNoType noType = new JCNoType(NONE);

    public int tag;

    public Symbol.TypeSymbol tsym;



    public Type(int tag, Symbol.TypeSymbol tsym) {
        this.tag = tag;
        this.tsym = tsym;
    }

    public List<Type> getParameterTypes() {
        return null;
    }



    public List<Type> getTypeAvrguments() {
        return null;
    }

    public Type getReturnType() {
        return null;
    }

    public <R, S> R accept(Types.Visitor<R,S> v, S s) {
        return v.visitType(this, s);
    }

    public Type map(Mapping mapping) {
        return this;
    }

    public Object constValue() {
        return null;
    }


    public Type constType(Object constValue) {
        final Object value = constValue;
        return new Type(tag, tsym) {
            @Override
            public Object constValue() {
                return value;
            }

            @Override
            public Type baseType() {
                return tsym.type;
            }
        };
    }

    public Type baseType() {
        return this;
    }

    public Type getEnclosingType() {
        return null;
    }

    public static abstract class Mapping {

        private String name;
        public Mapping(String name) {
            this.name = name;
        }

        public abstract Type apply(Type t);

        @Override
        public String toString() {
            return name;
        }
    }


    public static class JCNoType extends Type {

        public JCNoType(int tag) {
            super(tag, null);
        }


        public TypeKind getKind() {
            switch (tag) {
                case VOID: return TypeKind.VOID;
                case NONE: return TypeKind.NONE;
                default:
                    return null;
            }
        }


    }

    public static class ForAll extends DelegatedType {

        public List<Type> tvars;

        public ForAll(List<Type> tvars, Type qtype) {
            super(FORALL, qtype);
            this.tvars = tvars;
        }

        @Override
        public <R, S> R accept(Types.Visitor<R, S> v, S s) {
            return v.visitForAll(this, s);
        }
    }

    public static class PackageType extends Type {

        PackageType(Symbol.TypeSymbol tsym) {
            super(PACKAGE, tsym);
        }
    }

    public static class ClassType extends Type {

        public Type superType_field;

        private Type outer_field;

        public ClassType(Type outer, List<Type> typarams, Symbol.TypeSymbol tsym) {
            super(CLASS, tsym);
            this.outer_field = outer;
            this.superType_field = null;
        }

        @Override
        public <R, S> R accept(Types.Visitor<R, S> v, S s) {
            return v.visitClassType(this, s);
        }

        @Override
        public Type getEnclosingType() {
            return outer_field;
        }
    }

    public static class BottomType extends Type {
        public BottomType() {
            super(BOT, null);
        }
    }

    public static class MethodType extends Type {

        public List<Type> argTypes;
        public Type restType;
        public List<Type> thrown;

        public MethodType(List<Type> argTypes, Type restType, List<Type> thrown, Symbol.TypeSymbol methodClass) {
            super(METHOD, methodClass);
            this.argTypes = argTypes;
            this.restType = restType;
            this.thrown = thrown;
        }


        @Override
        public Type map(Mapping mapping) {

            return this;
        }

        @Override
        public List<Type> getParameterTypes() {
            return argTypes;
        }

        @Override
        public Type getReturnType() {
            return restType;
        }

        @Override
        public <R, S> R accept(Types.Visitor<R, S> v, S s) {
            return v.visitMethodType(this, s);
        }
    }

    public static class ArrayType extends Type {

        public Type elemtype;

        public ArrayType(Type elemtype, Symbol.TypeSymbol arrayClass) {
            super(ARRAY, arrayClass);
            this.elemtype = elemtype;
        }

        @Override
        public String toString() {
            return elemtype + "[]";
        }
    }

    public static abstract class DelegatedType extends Type {
        public Type qtype;

        public DelegatedType(int tag, Type qtype) {
            super(tag, qtype.tsym);
            this.qtype = qtype;
        }
    }
}
