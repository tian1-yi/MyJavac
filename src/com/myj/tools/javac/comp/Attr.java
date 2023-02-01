package com.myj.tools.javac.comp;

import com.myj.tools.javac.code.Scope;
import com.myj.tools.javac.code.Symbol;
import com.myj.tools.javac.code.Type;
import com.myj.tools.javac.code.TypeTags;
import com.myj.tools.javac.jvm.ClassReader;
import com.myj.tools.javac.tree.JCTree;
import com.myj.tools.javac.tree.TreeInfo;
import com.myj.tools.javac.util.*;

import java.util.ArrayList;
import java.util.List;

import static com.myj.tools.javac.code.Flags.FINAL;
import static com.myj.tools.javac.code.Flags.UNATTRIBUTED;
import static com.myj.tools.javac.code.Kinds.*;
import static com.myj.tools.javac.code.TypeTags.*;

public class Attr extends JCTree.Visitor {

    public static final Context.Key<Attr> attrKey = new Context.Key<>();

    Env<AttrContext> env;


    int pkind;


    Type pt;


    String errKey;

    Check check;

    Type result;

    Symtab syms;

    ClassReader classReader;

    Enter enter;

    MemberEnter memberEnter;

    Types types;

    TreeInfo treeInfo;

    Names names;




    public Attr(Context context) {
        context.put(attrKey, this);

        syms = Symtab.instance(context);
        check = Check.instance(context);
        classReader = ClassReader.instance(context);
        enter = Enter.instance(context);
        memberEnter = MemberEnter.instance(context);
        types = Types.instance(context);
        treeInfo = TreeInfo.instance(context);
        names = Names.instance(context);
    }

    public static Attr instance(Context context) {
        Attr attr = context.get(attrKey);
        if (attr == null) {
            attr = new Attr(context);
        }
        return attr;
    }


    /**
     * 标注
     * @param tree
     * @param localEnv
     */
    public Type attribType(JCTree tree, Env<AttrContext> localEnv) {
        return attribType(tree, localEnv, Type.noType);
    }

    private Type attribType(JCTree tree, Env<AttrContext> localEnv, Type pt) {
        return attribType(tree, localEnv, TYP, pt);
    }

    private Type attribType(JCTree tree, Env<AttrContext> localEnv, int typ, Type pt) {
        return attribTree(tree, localEnv, typ, pt, "error");
    }

    private Type attribTree(JCTree tree, Env<AttrContext> env, int pkind, Type pt, String error) {
        // 保存原值
        Env<AttrContext> preEnv = this.env;
        int prePkind = this.pkind;
        Type prePt = this.pt;

        try {
            this.env = env;
            this.pkind = pkind;
            this.pt = pt;
            tree.accept(this);
            return result;
        } finally {
            this.env = preEnv;
            this.pkind = prePkind;
            this.pt = prePt;
        }
    }

    @Override
    public void visitTypeIdent(JCTree.JCPrimitiveType tree) {
        result = check(tree, syms.typeOfTag[tree.typetag], TYP, pkind, pt);
    }

    @Override
    public void visitIdent(JCTree.JCIdent tree) {
        Symbol sym;
        boolean varArgs = false;
        if (pt.tag == METHOD || pt.tag == FORALL) {// 查找方法
            env.info.varArgs = false;
            sym = resolveMethod(tree.pos, env, tree.name, pt.getParameterTypes(), pt.getTypeAvrguments());
        } else if (tree.symbol != null && tree.symbol.kind != VAR) {
            sym = null;
        } else {
            //  处理类型或者变量
            sym = resolveIdent(tree.pos, env, tree.name, pkind);
        }
        tree.symbol = sym;

        result = check(tree,  sym.type, sym.kind, pkind, pt);
    }

    private Symbol resolveIdent(int pos, Env<AttrContext> env, Name name, int pkind) {
        return access(findIdent(env, name, pkind), pos, env.enclClass.sym.type, name, false);
    }

    private Symbol access(Symbol ident, int pos, Type type, Name name, boolean b) {
        return ident;
    }

    /**
     * 查找name对应的symbol
     * @param env
     * @param name
     * @param pkind
     * @return
     */
    private Symbol findIdent(Env<AttrContext> env, Name name, int pkind) {
        if ((pkind & VAR) != 0) {
            return findVar(env, name);
        }
        if ((pkind & TYP) != 0) {
            return findType(env, name);
        }
        return null;
    }

    private Symbol findVar(Env<AttrContext> env, Name name) {

        Symbol sym = null;
        Env<AttrContext> env1 = env;

        while (env1.outer != null) {
            Scope.Entry e = env1.info.scope.lookup(name);
            while (e.scope != null && e.sym.kind != VAR) {
                e = e.next();
            }
            sym = e.sym;
            if (sym != null) {
                return sym;
            }
            env1 = env1.outer;
        }
        return sym;
    }

    private Symbol findType(Env<AttrContext> env, Name name) {

        Symbol sym;

        for (Env<AttrContext> env1 = env; env1.outer != null; env1 = env1.outer) {
            for (Scope.Entry e = env1.info.scope.lookup(name); e.scope != null; e = e.next()) {
                if (e.sym.kind == TYP) {
                    return e.sym;
                }
            }
            sym = findMemberType(env1, env1.enclMethod.sym.type, name, env1.enclClass.sym);
        }
        if (env.tree.getTag() != JCTree.IMPORT) {
            sym = findGlobalType(env, env.toplevel.starImportScope, name);
            return sym;
        }
        return null;
    }

    private Symbol findGlobalType(Env<AttrContext> env, Scope scope, Name name) {
        Symbol bestSoFar = null;
        for (Scope.Entry e = scope.lookup(name); e.scope != null; e = e.next()) {
            Symbol sym = loadClass(env, e.sym.flatName());
            if (bestSoFar == null) {
                bestSoFar = sym;
            }
        }

        return bestSoFar;
    }

    private Symbol loadClass(Env<AttrContext> env, Name flatName) {
        return classReader.loadClass(flatName);
    }

    private Symbol findMemberType(Env<AttrContext> env1, Type type, Name name, Symbol.ClassSymbol sym) {
        return sym;
    }

    private Symbol resolveMethod(int pos, Env<AttrContext> env, Name name, List<Type> parameterTypes, List<Type> typeAvrguments) {

        return null;
    }

    @Override
    public void visitTypeArray(JCTree.JCArrayTypeTree tree) {
        Type etype = attribType(tree.element, env);
        Type type = new Type.ArrayType(etype, syms.arrayClass);
        result = check(tree, type, TYP, pkind, pt);
    }

    private Type check(JCTree tree, Type owntype, int ownkind, int pkind, Type pt) {
        if (owntype.tag != ERROR && pt.tag != METHOD && pt.tag != FORALL) {
            if ((ownkind & ~pkind) == 0) {
                owntype = check.checkType(tree.pos, owntype, pt);
            }
        }
        tree.type = owntype;
        return owntype;
    }

    /**
     * 标注方法
     * @param tree
     */
    @Override
    public void visitMethodDef(JCTree.JCMethodDecl tree) {
        Symbol.MethodSymbol m = tree.sym;

        Env<AttrContext> localEnv = memberEnter.methodEnv(tree, env);

        for (JCTree.JCVariableDecl param : tree.params) {
            // 标注参数
            attribStat(param, localEnv);
        }

        // 标注body体
        attribStat(tree.body, localEnv);

        result = tree.type = m.type;
    }

    /**
     * 标注参数
     * @param tree
     */
    @Override
    public void visitVarDef(JCTree.JCVariableDecl tree) {

        if (env.info.scope.owner.kind == MTH) {
            if (tree.sym != null) {
                env.info.scope.enter(tree.sym);
            } else {
                // 类型符号填充
                memberEnter.memberEnter(tree, env);
            }
        }

        Symbol.VarSymbol v = tree.sym;

        if (tree.init != null) {
            if ((v.flags_field & FINAL) != 0 && tree.init.getTag() != JCTree.NEWCLASS) {
                v.getConstValue();
            } else {
                Env<AttrContext> initEnv = memberEnter.initEnv(tree, env);

                initEnv.info.enclVar = v;

                attribExpr(tree.init, initEnv, v.type);
            }
        }

        result = tree.type = v.type;
    }

    private Type attribExpr(JCTree tree, Env<AttrContext> env, Type pt) {
        return attribTree(tree, env, VAL, pt.tag != ERROR ? pt : Type.noType);
    }

    @Override
    public void visitLiteral(JCTree.JCLiteral tree) {
        result = check(tree, litType(tree.typetag).constType(tree.value), VAL, pkind, pt);
    }

    private Type litType(int typetag) {
        return (typetag == CLASS) ? syms.stringType : syms.typeOfTag[typetag];
    }

    public Type attribBase() {
        return null;
    }

    public void attrib(Env<AttrContext> env) {
        if (env.tree.getTag() == JCTree.TOPLEVEL) {
            attribTopLevel(env);
        } else {
            attribClass(env.tree.pos, env.enclClass.sym);
        }
    }

    private void attribClass(int pos, Symbol.ClassSymbol sym) {
        attribClass(sym);
    }

    private void attribClass(Symbol.ClassSymbol c) {

        Env<AttrContext> env = enter.typeEnvs.get(c);

        if ((c.flags_field & UNATTRIBUTED) !=  0) {
            c.flags_field &= ~UNATTRIBUTED;
        }

        attribClassBody(env,c);

    }

    @Override
    public void visitReturn(JCTree.JCReturn tree) {
        Symbol m = env.enclMethod.sym;
        attribExpr(tree.expr, env, m.type.getReturnType());
        result = null;
    }

    private void attribClassBody(Env<AttrContext> env, Symbol.ClassSymbol c) {
        JCTree.JCClassDecl tree = (JCTree.JCClassDecl) env.tree;
        
        tree.type = c.type;

        for (JCTree def : tree.defs) {
            attribStat(def, env);
        }
    }

    private Type attribStat(JCTree tree, Env<AttrContext> env) {
        return attribTree(tree, env, NIL, Type.noType);
    }

    private Type attribTree(JCTree tree, Env<AttrContext> env, int pkind, Type pt) {
        return attribTree(tree, env, pkind, pt, "");
    }

    /**
     * 标注编译单元
     * @param env
     */
    private void attribTopLevel(Env<AttrContext> env) {

    }

    @Override
    public void visitBlock(JCTree.JCBlock tree) {
        if (env.info.scope.owner.kind == TYP) {

        } else {
            Env<AttrContext> localEnv = env.dup(tree, env.info.dup(env.info.scope.dup()));
            attribStats(tree.stats, localEnv);
            localEnv.info.scope.leave();
        }
        result = null;
    }

    private <T extends JCTree> void attribStats(List<T> trees, Env<AttrContext> localEnv) {
        for (T tree : trees) {
            attribStat(tree, localEnv);
        }
    }


    @Override
    public void visitExec(JCTree.JCExpressionStatement tree) {
        Env<AttrContext> localEnv = env.dup(tree);
        attribExpr(tree.expr, localEnv);
        result = null;
    }

    private Type attribExpr(JCTree tree, Env<AttrContext> env) {

        return attribTree(tree, env, VAL, Type.noType);
    }

    @Override
    public void visitAssign(JCTree.JCAssign tree) {
        Type ownType = attribTree(tree.lhs, env.dup(tree), VAR, Type.noType);
        Type captureType = capture(ownType);
        attribExpr(tree.rhs, env, ownType);
        result = check(tree, captureType, VAL, pkind, pt);
    }

    private Type capture(Type ownType) {
        return types.capture(ownType);
    }

    @Override
    public void visitBinary(JCTree.JCBinary tree) {
        attribExpr(tree.lhs, env);
        attribExpr(tree.rhs, env);

        Symbol operator = tree.operator = resolveOperator(tree.getTag(), env, tree.lhs.type, tree.rhs.type);

        Type ownType = null;
        if (operator.kind == MTH) {
            ownType = operator.type.getReturnType();
        }

        result = check(tree, ownType, VAL, pkind, pt);
    }

    @Override
    public void visitApply(JCTree.JCMethodInvocation tree) {
        Env<AttrContext> localEnv = env.dup(tree, env.info.dup());

        Name methName = TreeInfo.name(tree.meth);

        ArrayList<Type> argTypes = new ArrayList<>();
        ArrayList<Type> typeargtypes = new ArrayList<>();

        boolean isConstructorCall = methName == names._this || methName == names._super;

        if (isConstructorCall) {
            localEnv.info.isSelfCall = true;
            argTypes = attribArgs(tree.args, localEnv);
            typeargtypes = attribTypes(tree.typeargs, localEnv);

            Type site = env.enclClass.sym.type;
            if (methName == names._super) {
                if (site == syms.objectType) {

                } else {
                    site = types.supertype(site);
                }
            }

            if (site.tag == CLASS) {
                Type encl = site.getEnclosingType();

                if (encl.tag == CLASS) {

                } else if (tree.meth.getTag() == JCTree.SELECT) {

                }
            }

            boolean selectSuperPre = localEnv.info.selectSuper;
            localEnv.info.selectSuper = true;
            localEnv.info.varArgs =false;

            Symbol symbol = resolveConstructor(localEnv, site, argTypes);
            localEnv.info.selectSuper = selectSuperPre;

            TreeInfo.setSymbol(tree.meth, symbol);

            Type mpt = newMethTemplate(argTypes, typeargtypes);
            checkId(tree.meth, site, symbol, localEnv, MTH, mpt);
            result = tree.type = syms.voidType;
        }


    }

    private Type checkId(JCTree tree, Type site, Symbol sym, Env<AttrContext> env, int pkind, Type pt) {

        Type owntype;

        switch (sym.kind) {
            case MTH:
                JCTree.JCMethodInvocation app = (JCTree.JCMethodInvocation) env.tree;
                owntype = checkMethod(site, sym, env, app.args, pt.getParameterTypes(), pt.getTypeAvrguments(), env.info.varArgs);
                break;
            default:
                return null;
        }

        return check(tree, owntype, sym.kind, pkind, pt);
    }

    private Type checkMethod(Type site, Symbol sym, Env<AttrContext> env, List<JCTree.JCExpression> args, List<Type> parameterTypes, List<Type> typeAvrguments, boolean varArgs) {
        return types.memberType(site, sym);
    }

    private Type newMethTemplate(ArrayList<Type> argTypes, ArrayList<Type> typeargtypes) {
        Type.MethodType mt = new Type.MethodType(argTypes, null, null, syms.methodClass);
        return (Type) new Type.ForAll(typeargtypes, mt);
    }

    private ArrayList<Type> attribTypes(List<JCTree.JCExpression> typeargs, Env<AttrContext> localEnv) {
        if (typeargs == null) {
            return null;
        }
        ArrayList<Type> argtypes = new ArrayList<>();
        for (JCTree.JCExpression typearg : typeargs) {
            argtypes.add(attribType(typearg, localEnv));
        }
        return argtypes;
    }

    private Symbol resolveConstructor(Env<AttrContext> env, Type site, ArrayList<Type> argTypes) {
        Symbol sym = findMethod(env, site, names.init, argTypes, false);

        return sym;
    }

    private ArrayList<Type> attribArgs(List<JCTree.JCExpression> args, Env<AttrContext> localEnv) {
        ArrayList<Type> argtypes = new ArrayList<>();
        for (JCTree.JCExpression arg : args) {
            argtypes.add(attribTree(arg, env, VAL, Infer.anyPoly));
        }
        return argtypes;
    }

    Symbol resolveOperator(int optag, Env<AttrContext> env, Type left, Type right) {
        Name name = treeInfo.operatorName(optag);
        ArrayList<Type> argsTypes = new ArrayList<>();
        argsTypes.add(left);
        argsTypes.add(right);
        Symbol sym = findMethod(env, syms.predefClass.type, name, argsTypes, true);
        return sym;
    }

    private Symbol findMethod(Env<AttrContext> env, Type type, Name name, ArrayList<Type> argsTypes, boolean operator) {
        Symbol best = null;
        if (operator) {
            Symbol.ClassSymbol c = (Symbol.ClassSymbol) type.tsym;

            for (Scope.Entry e = c.members().lookup(name); e.scope != null; e = e.next()) {
                if (e.sym.kind == MTH) {
                    Symbol sym = e.sym;
                    Type.MethodType methodType = (Type.MethodType) sym.type;
                    if (argsTypes.size() == methodType.argTypes.size()) {
                        int i = 0;
                        int match = 0;
                        int lenght = argsTypes.size();
                        for (; i < lenght; i ++) {
                            if (argsTypes.get(i).tsym.name == methodType.argTypes.get(i).tsym.name) {
                                match ++;
                            }
                        }
                        if (match == lenght) {
                            best = sym;
                            return best;
                        }
                    }
                }
            }
        } else {

            Symbol.ClassSymbol c = (Symbol.ClassSymbol) type.tsym;

            for (Scope.Entry e = c.members().lookup(name); e.scope != null; e = e.next()) {
                if (e.sym.kind == MTH) {
                    Symbol sym = e.sym;
                    Type.MethodType methodType = (Type.MethodType) sym.type;

                    if (argsTypes.size() == methodType.argTypes.size()) {
                        int i = 0;
                        int match = 0;
                        int lenght = argsTypes.size();
                        for (; i < lenght; i ++) {
                            if (argsTypes.get(i).tsym.name == methodType.argTypes.get(i).tsym.name) {
                                match ++;
                            }
                        }
                        if (match == lenght) {
                            best = sym;
                            return best;
                        }
                    }
                }
            }
        }

        return best;
    }

}
