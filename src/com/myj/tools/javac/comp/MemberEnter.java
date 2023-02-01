package com.myj.tools.javac.comp;

import com.myj.tools.javac.code.Scope;
import com.myj.tools.javac.code.Symbol;
import com.myj.tools.javac.code.Type;
import com.myj.tools.javac.jvm.ClassReader;
import com.myj.tools.javac.tree.JCTree;
import com.myj.tools.javac.tree.TreeInfo;
import com.myj.tools.javac.util.*;

import java.util.ArrayList;
import java.util.List;

import static com.myj.tools.javac.code.Flags.*;
import static com.myj.tools.javac.code.Kinds.PCK;
import static com.myj.tools.javac.code.Kinds.TYP;
import static com.myj.tools.javac.code.TypeTags.CLASS;

public class MemberEnter extends JCTree.Visitor implements Symbol.Completer {

    public static final Context.Key<MemberEnter> memberEnterKey = new Context.Key<>();

    ArrayList<Env<AttrContext>> halfcompleteed = new ArrayList<>();

    Enter enter;

    Names names;

    Env<AttrContext> env;

    Attr attr;

    Symtab syms;

    ClassReader classReader;

    Todo todo;

    TreeMaker treeMaker;

    Check check;

    public MemberEnter(Context context) {
        context.put(memberEnterKey, this);

        enter = Enter.instance(context);
        names = Names.instance(context);
        attr = Attr.instance(context);
        syms = Symtab.instance(context);
        classReader = ClassReader.instance(context);
        todo = Todo.instance(context);
        treeMaker = TreeMaker.instance(context);
        check = Check.instance(context);
    }

    public static MemberEnter instance(Context context) {
        MemberEnter memberEnter = context.get(memberEnterKey);
        if (memberEnter == null) {
            memberEnter = new MemberEnter(context);
        }
        return memberEnter;
    }



    @Override
    public void complete(Symbol sym) {

        Symbol.ClassSymbol c = (Symbol.ClassSymbol) sym;
        Type.ClassType ct = (Type.ClassType) c.type;
        Env<AttrContext> env = enter.typeEnvs.get(c);
        JCTree.JCClassDecl tree = (JCTree.JCClassDecl) env.tree;

        halfcompleteed.add(env);

        if (c.owner.kind == PCK) {
            memberEnter(env.toplevel, env.enclosing(JCTree.TOPLEVEL));
            todo.add(env);
        }

        Type superType = (tree.extending != null)
                ? (Type) attr.attribBase()
                : (c.fullName == names.java_lang_Object)
                ? Type.noType
                : syms.objectType;
        ct.superType_field = superType;



        // 添加构造器
        if ((c.flags() & INTERFACE) == 0 && !TreeInfo.hasConstructors(tree.defs)) {
            JCTree contructor = DefaultContructor(treeMaker.at(tree.pos), c, new ArrayList<Type>(), new ArrayList<Type>(), new ArrayList<Type>(), 0, false);
            tree.defs.add(contructor);
        }

        if ((c.flags_field & INTERFACE) == 0) {
            // 添加this符号
            Symbol.VarSymbol thisSym = new Symbol.VarSymbol(FINAL | HASINIT, names._this, c.type, c);
            env.info.scope.enter(thisSym);
            if (ct.superType_field.tag == CLASS) {
                // 添加super符号
                Symbol.VarSymbol superSym = new Symbol.VarSymbol(FINAL | HASINIT, names._super, ct.superType_field, c);
                env.info.scope.enter(superSym);
            }
        }

        int i = 0;
        while (i < halfcompleteed.size()) {
            finish(halfcompleteed.get(0));
            i ++;
        }
    }

    private JCTree DefaultContructor(TreeMaker make, Symbol.ClassSymbol c, ArrayList<Type> typarams, ArrayList<Type> arg, ArrayList<Type> thrown, long flags, boolean based) {

        List<JCTree.JCVariableDecl> params = make.Params(arg, syms.noSymbol);
        ArrayList<JCTree.JCStatement> stats = new ArrayList<>();
        if (c.type != syms.objectType) {
            stats.add(0, new JCTree.JCVariableDecl(
                    new JCTree.JCModifiers(0, null),
                    names.fromString("a"),
                    new JCTree.JCPrimitiveType(4),
                    new JCTree.JCBinary(
                            71,
                            new JCTree.JCLiteral(4, 1),
                            new JCTree.JCLiteral(4, 1),
                            null),
                    null));
            stats.add(1, SuperCall(make, typarams, params, based));
        }

        c.flags_field = 268435457;
        flags |= (c.flags() & AccessFlags) | GENERATEDCONSTR;


        if (c.name.isEmpty()) flags |= ANONCONSTR;
        return treeMaker.MethodDef(make.Modifiers(flags), names.init, null, null, params, null, make.Block(0, stats), null);
    }

    private JCTree.JCStatement SuperCall(TreeMaker make, ArrayList<Type> typarams, List<JCTree.JCVariableDecl> params, boolean based) {

        JCTree.JCExpression meth = make.Ident(names._super);

        return make.Exec(make.Apply(null, meth, make.Idents(params)));
    }

    private void finish(Env<AttrContext> env) {
        JCTree.JCClassDecl tree = (JCTree.JCClassDecl) env.tree;
        finishClass(tree, env);
    }

    private void finishClass(JCTree.JCClassDecl tree, Env<AttrContext> env) {
        memberEnter(tree.defs, env);
    }

    private void memberEnter(List<? extends JCTree> defs, Env<AttrContext> env) {
        for (JCTree def : defs) {
            memberEnter(def, env);
        }
    }

    public void memberEnter(JCTree tree, Env<AttrContext> env) {
        Env<AttrContext> preEnv = this.env;
        this.env = env;
        tree.accept(this);

        this.env = preEnv;
    }

    @Override
    public void visitVarDef(JCTree.JCVariableDecl tree) {
        Env<AttrContext> localEnv = env;
//        if (((tree.mods.flags & STATIC) != 0 || (env.info.scope.owner.flags() & INTERFACE) != 0)) {
//            localEnv.info.staticLevel++;
//        }

        attr.attribType(tree.vartype, localEnv);

        // 获取作用域
        Scope enclScope = enter.enterScope(env);
        Symbol.VarSymbol v = new Symbol.VarSymbol(0, tree.name, tree.vartype.type, enclScope.owner);// 生成符号
        v.flags_field = check.checkFlags(tree.mods.flags, v, tree);
        tree.sym = v;

        if (tree.init != null) {
            v.flags_field |= HASINIT;

        }

        enclScope.enter(v); // 将符号输入作用域中
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl tree) {
        Scope enclScope = enter.enterScope(env); //获取环境域
        Symbol.MethodSymbol m = new Symbol.MethodSymbol(0, tree.name, null, enclScope.owner);
        m.flags_field = check.checkFlags(tree.mods.flags, m, tree);

        tree.sym = m;

        Env<AttrContext> localEnv = methodEnv(tree, env);
        // 标注
        m.type = signature(tree.typarams, tree.params, tree.restype, tree.thrown, localEnv);

        ArrayList<Symbol.VarSymbol> params = new ArrayList<>();
        for (JCTree.JCVariableDecl param : tree.params) {
            params.add(param.sym);
        }
        m.params = params;

        localEnv.info.scope.leave();

        enclScope.enter(m);

    }

    private Type signature(List<JCTree.JCTypeParameter> typarams, List<JCTree.JCVariableDecl> params, JCTree.JCExpression res, List<JCTree.JCExpression> thrown, Env<AttrContext> localEnv) {


        // 参数标注
        ArrayList<Type> argbuf = new ArrayList<>();
        for (JCTree.JCVariableDecl param : params) {
            memberEnter(param, localEnv);
            argbuf.add(param.vartype.type);
        }

        Type restype = res == null ? syms.voidType : attr.attribType(res, env);
        Type.MethodType mtype = new Type.MethodType(argbuf, restype, null, syms.methodClass);
        return mtype;
    }

    public Env<AttrContext> methodEnv(JCTree.JCMethodDecl tree, Env<AttrContext> env) {
        Env<AttrContext> localEnv = env.dup(tree, env.info.dup(env.info.scope.dupUnshared()));
        localEnv.enclMethod = tree;
        localEnv.info.scope.owner = tree.sym;
        if ((tree.mods.flags & STATIC) != 0) localEnv.info.staticLevel++;
        return localEnv;
    }

    @Override
    public void visitTopLevel(JCTree.JCCompilationUnit tree) {

        importAll(tree.pos, classReader.enterPackage(names.java_lang), env);

        memberEnter(tree.defs, env);
    }

    private void importAll(int pos, Symbol.TypeSymbol tsym, Env<AttrContext> env) {
        env.toplevel.starImportScope.importAll(tsym.members());
    }

    public Env<AttrContext> initEnv(JCTree.JCVariableDecl tree, Env<AttrContext> env) {
        Env<AttrContext> localEnv = env.dupto(new AttrContextEnv(tree, env.info.dup()));
        if (tree.sym.owner.kind == TYP) {
            localEnv.info.scope = new Scope.DelegatedScope(env.info.scope);
            localEnv.info.scope.owner = tree.sym;
        }
        if ((tree.mods.flags & STATIC) != 0 || (env.enclClass.sym.flags() & INTERFACE) != 0) {
            localEnv.info.staticLevel ++;
        }
        return localEnv;
    }
}
