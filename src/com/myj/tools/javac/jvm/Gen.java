package com.myj.tools.javac.jvm;

import com.myj.tools.javac.code.Symbol;
import com.myj.tools.javac.code.Type;
import com.myj.tools.javac.comp.AttrContext;
import com.myj.tools.javac.comp.Env;
import com.myj.tools.javac.tree.JCTree;
import com.myj.tools.javac.tree.TreeInfo;
import com.myj.tools.javac.util.*;

import java.util.ArrayList;
import java.util.List;

import static com.myj.tools.javac.code.Flags.FINAL;
import static com.myj.tools.javac.code.Flags.STATIC;
import static com.myj.tools.javac.code.Kinds.MTH;
import static com.myj.tools.javac.code.Kinds.VAR;
import static com.myj.tools.javac.code.TypeTags.VOID;
import static com.myj.tools.javac.jvm.ByteCodes.*;
import static com.myj.tools.javac.jvm.CRTFlags.CRT_STATEMENT;
import static com.myj.tools.javac.tree.JCTree.*;
import static com.myj.tools.javac.util.Position.NOPOS;

public class Gen extends JCTree.Visitor {
    
    public static final Context.Key<Gen> genKey = new Context.Key<>();

    Env<GenContext> env;

    Type pt;

    Items.Item result;

    Types types;

    TreeMaker treeMaker;

    Symtab syms;

    Items items;

    Code code;

    private final Type methodType;

    private final Code.StackMapFormat stackMap;

    private Pool pool = new Pool();

    Names names;

    int pendingStatPos = NOPOS;


    private Gen(Context context) {
        context.put(genKey, this);

        this.types = Types.instance(context);
        this.treeMaker = TreeMaker.instance(context);
        this.syms = Symtab.instance(context);
        this.stackMap = Code.StackMapFormat.JRS202;
        this.names = Names.instance(context);
        this.methodType = new Type.MethodType(null, null, null, syms.methodClass);
    }

    public static Gen instance(Context context) {
        Gen gen = context.get(genKey);
        if (gen == null) {
            gen = new Gen(context);
        }
        return gen;
    }


    public void genClass(Env<AttrContext> env, JCTree.JCClassDecl cdef) {

        Symbol.ClassSymbol c = cdef.sym;
        cdef.defs = norMalizeDefs(cdef.defs, c);
        Env<GenContext> localEnv = new Env<>(cdef, new GenContext());

        c.pool = pool;


        localEnv.toplevel = env.toplevel;
        localEnv.enclClass = cdef;

        for (JCTree def : cdef.defs) {
            genDef(def, localEnv);
        }


    }

    private List<JCTree> norMalizeDefs(List<JCTree> defs, Symbol.ClassSymbol c) {
        ArrayList<JCTree.JCStatement> initCode = new ArrayList<>();
        ArrayList<JCTree.JCStatement> clinitCode = new ArrayList<>();
        ArrayList<JCTree> methodDefs = new ArrayList<>();

        for (JCTree def : defs) {
            switch (def.getTag()) {
                case VARDEF:
                    JCTree.JCVariableDecl vdef = (JCTree.JCVariableDecl) def;
                    Symbol.VarSymbol sym = vdef.sym;
                    if (vdef.init != null) { //初始值不为空

                        if ((sym.flags() & STATIC) == 0) { // 非静态属性
                            JCTree.JCStatement init = treeMaker.at(vdef.pos).Assignment(sym, vdef.init);
                            initCode.add(init);
                        } else if (sym.getConstValue() == null) { // 值为空
                            JCTree.JCStatement init = treeMaker.at(vdef.pos).Assignment(sym, vdef.init);
                            clinitCode.add(init);
                        }
                    }
                    break;
                case METHODDEF:
                    methodDefs.add(def);
                    break;
                default:
            }
        }

        if (clinitCode.size() != 0) {
            Symbol.MethodSymbol clinit = new Symbol.MethodSymbol(STATIC, names.clinit, new Type.MethodType(new ArrayList<Type>(), syms.voidType, new ArrayList<Type>(), syms.methodClass), c);
            c.members().enter(clinit);
            JCTree.JCBlock block = treeMaker.at(clinitCode.get(0).pos).Block(0, clinitCode);
            methodDefs.add(treeMaker.MethodDef(clinit, block));
        }
        return methodDefs;
    }

    @Override
    public void visitExec(JCTree.JCExpressionStatement tree) {
        JCTree.JCExpression e = tree.expr;

        switch (e.getTag()) {
            case POSTINC:
                ((JCUnary) e).setTag(PREINC);
                break;
            case POSTDEC:
                ((JCUnary) e).setTag(PREDEC);
        }
        genExpr(tree.expr, tree.expr.type).drop();
    }

    @Override
    public void visitAssign(JCAssign tree) {
        Items.Item l = genExpr(tree.lhs, tree.lhs.type);
        genExpr(tree.rhs, tree.lhs.type).load();
        result = items.makeAssignItem(l);
    }


    @Override
    public void visitBinary(JCBinary tree) {
        OperatorSymbol operator = (OperatorSymbol) tree.operator;

        if (operator.opcode == string_add) {

        } else {
            Items.Item od = genExpr(tree.lhs, operator.type.getParameterTypes().get(0));
            od.load();
            result = completeBinop(tree.lhs, tree.rhs, operator);
        }
    }

    private Items.Item completeBinop(JCTree lhs, JCTree rhs, OperatorSymbol operator) {

        Type.MethodType optype = (Type.MethodType) operator.type;

        int opcode = operator.opcode;
        if (opcode >= if_icmpeq && opcode <= if_icmple && rhs.type.constValue() instanceof Number && ((Number) rhs.type.constValue()).intValue() == 0) {
            opcode = opcode + (ifeq - if_icmpeq);
        } else {
            List<Type> parameterTypes = operator.enasure(types).getParameterTypes();
            Type rtype = parameterTypes.get(parameterTypes.size() - 1);
            if (opcode >= ishll && opcode <= lushrl) {
                opcode = opcode + (ishl - ishll);
                rtype = syms.intType;
            }

            genExpr(rhs, rtype).load();

            if (opcode >= (1 << preShift)) {
                code.emitop0(opcode >> preShift);
                opcode = opcode & 0xFF;
            }
        }

        if (opcode >= ifeq && opcode <= if_acmpne ||
                opcode == if_acmp_null || opcode == if_acmp_nonnull) {
            return null;
        } else {
            code.emitop0(opcode);
            return items.makeStackItem(optype.restType);
        }

    }

    private void genDef(JCTree tree, Env<GenContext> localEnv) {
        Env<GenContext> preEnv = this.env;

        try {
            this.env = localEnv;
            tree.accept(this);
        } finally {
            this.env = preEnv;
        }
    }

    @Override
    public void visitBlock(JCTree.JCBlock tree) {
        int limit = code.nextreg;
        Env<GenContext> localEnv = env.dup(tree, new GenContext());
        getStat(tree.stats, localEnv);

        if (env.tree.getTag() != METHODDEF) {
            code.pendingStatPos = NOPOS;
        }
    }

    private void getStat(List<? extends JCTree> stats, Env<GenContext> localEnv) {
        for (JCTree stat : stats) {
            getStat(stat, env, CRT_STATEMENT);
        }
    }

    private void getStat(JCTree tree, Env<GenContext> env, int crtStatement) {
        getStat(tree, env);
        return;
    }

    @Override
    public void visitVarDef(JCTree.JCVariableDecl tree) {
        Symbol.VarSymbol v = tree.sym;
        code.newLocal(v);

        if (tree.init != null) {
            if (v.getConstValue() == null) {
                // load
                genExpr(tree.init, v.enasure(types)).load();
                // 存储
                items.makeLocalItem(v).store();
            }
        }
    }

    /**
     * 生成执行语句
     * @param tree
     * @param pt
     * @return
     */
    private Items.Item genExpr(JCTree tree, Type pt) {

        Type prept = this.pt;

        try {
            if (tree.type.constValue() != null) {
                result = items.makeImmediateItem(tree.type, tree.type.constValue());
            } else {
                this.pt = pt;
                tree.accept(this);
            }
            return result.coerce(pt);
        } finally {
            this.pt = prept;
        }

    }

    @Override
    public void visitReturn(JCReturn tree) {
        int nextreg = code.nextreg;

        if (tree.expr != null) {
            Items.Item r = genExpr(tree.expr, pt).load();
            r.load();
            code.emitop0(ireturn + Code.truncate(Code.typeCode(pt)));
        } else {
            code.emitop0(return_);
        }
        code.alive = false;
    }

    @Override
    public void visitApply(JCMethodInvocation tree) {
        Items.Item m = genExpr(tree.meth, methodType);

        genArgs(tree.args, TreeInfo.symbol(tree.meth).externalType(types).getParameterTypes());
        result = m.invoke();

    }

    /**
     * 对标识符的处理
     * @param tree
     */
    @Override
    public void visitIdent(JCIdent tree) {
        Symbol sym = tree.symbol;

        if (tree.name == names._this || tree.name == names._super) {
            Items.Item res = tree.name == names._this
                    ? items.makeThisItem()
                    : items.makeSuperItem();
            if (sym.kind == MTH) {
                // Generate code to address the constructor.
                res.load();
                res = items.makeMemberItem(sym, true);
            }
            result = res;
        } else if (sym.kind == VAR && sym.owner.kind == MTH) {
            result = items.makeLocalItem((Symbol.VarSymbol)sym);
        } else if ((sym.flags() & STATIC) != 0) {
            result = items.makeStaticItem(sym);
        } else {

        }
    }

    private void genArgs(List<JCExpression> trees, List<Type> pts) {
        for (int i = 0;i < trees.size(); i ++) {
            genExpr(trees.get(i), pts.get(i));
        }
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl tree) {
        Env<GenContext> localEnv = env.dup(tree);

        localEnv.enclMethod = tree;

        this.pt = tree.sym.enasure(types).getReturnType();

        genMethod(tree, localEnv, false);
    }

    /**
     * 生成方法的字节码
     * @param tree
     * @param localEnv
     * @param fatcode
     */
    private void genMethod(JCTree.JCMethodDecl tree, Env<GenContext> localEnv, boolean fatcode) {

        // 初始化code
        initCode(tree, env, fatcode);



        getStat(tree.body, env);

        if (code.isAlive()) {
            if (env.enclMethod == null || env.enclMethod.sym.type.getReturnType().tag == VOID) {
                code.emitop0(return_);
            } else {

            }
        }

    }

    /**
     * 生成语句
     * @param body
     * @param env
     */
    private void getStat(JCTree body, Env<GenContext> env) {
        code.setBegin(body.pos);
        genDef(body, env);
    }

    /**
     * 初始化代码生成
     * @param tree
     * @param env
     * @param fatcode
     * @return
     */
    private int initCode(JCTree.JCMethodDecl tree, Env<GenContext> env, boolean fatcode) {

        Symbol.MethodSymbol meth = tree.sym;

        meth.code = code = new Code(meth, fatcode, stackMap, syms, types, pool);

        items = new Items(pool, code, syms, types);

        // 添加this变量
        if ((tree.mods.flags & STATIC) == 0) {
            Type selfType = meth.owner.type;
            if (meth.isConstructor() && selfType != syms.objectType) {
                selfType = UninitializedType.uninitializedThis(selfType);
            }
            code.setDefined(code.newLocal(new Symbol.VarSymbol(FINAL, names._this, selfType, meth.owner)));
        }

        // 将参数添加到本地变量表
        for (JCTree.JCVariableDecl param : tree.params) {
            code.setDefined(code.newLocal(param.sym));
        }


        return 0;
    }

    static class GenContext {

    }



}
