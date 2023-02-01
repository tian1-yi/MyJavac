package com.myj.tools.javac.util;


import com.myj.tools.javac.code.Symbol;
import com.myj.tools.javac.code.Type;
import com.myj.tools.javac.tree.JCTree;

import java.util.ArrayList;
import java.util.List;

import static com.myj.tools.javac.code.TypeTags.*;

public class TreeMaker  {


    public static final Context.Key<TreeMaker> treeMakerKey = new Context.Key<TreeMaker>();

    // 文件位置
    public int pos = Position.NOPOS;

    // 编译单元
    public JCTree.JCCompilationUnit toplevel;

    //
    private Names names;

    // 符号表
    private Symtab symtab;

    // 类型
    private Types types;

    public TreeMaker(Context context) {
        context.put(treeMakerKey, this);
        this.pos = Position.NOPOS;
        this.toplevel = null;
        this.names = Names.instance(context);
        this.symtab = Symtab.instance(context);
        this.types = Types.instance(context);
    }

    public static TreeMaker instance(Context context) {
        TreeMaker treeMaker = context.get(treeMakerKey);
        if (null == treeMaker) {
            treeMaker = new TreeMaker(context);
        }
        return treeMaker;
    }

    /**
     * 解析编译单元
     * @param packageAnnotations
     * @param pid
     * @param defs
     * @return
     */
    public JCTree.JCCompilationUnit TopLevel(List<JCTree.JCAnnotation> packageAnnotations, JCTree.JCExpression pid, List<JCTree> defs) {
        JCTree.JCCompilationUnit tree = new JCTree.JCCompilationUnit(packageAnnotations, pid, defs, null, null, null, null);
        tree.pos = pos;
        return tree;
    }


    /**
     * 标志位置
     */
    public TreeMaker at(int pos) {
        this.pos = pos;
        return this;
    }

    /**
     * 修饰符
     * @param flags
     * @param annotations
     * @return
     */
    public JCTree.JCModifiers Modifiers(long flags, List<JCTree.JCAnnotation> annotations) {
        JCTree.JCModifiers modifiers = new JCTree.JCModifiers(flags, annotations);
        modifiers.pos = pos;
        return modifiers;
    }

    /**
     * 创建JCIdent对象
     * @param ident
     * @return
     */
    public JCTree.JCIdent Ident(Name ident) {
        JCTree.JCIdent jcIdent = new JCTree.JCIdent(ident, null);
        // 设置位置
        jcIdent.pos = pos;
        return jcIdent;
    }

    public JCTree.JCFieldAccess Select(JCTree.JCExpression i, Name ident) {
        JCTree.JCFieldAccess jcFieldAccess = new JCTree.JCFieldAccess(i, ident, null);
        jcFieldAccess.pos = pos;
        return jcFieldAccess;
    }

    public JCTree.JCImport Import(JCTree pid, boolean importStatic) {
        JCTree.JCImport jcImport = new JCTree.JCImport(pid, importStatic);
        jcImport.pos = pos;
        return jcImport;
    }

    public JCTree.JCClassDecl ClassDef(JCTree.JCModifiers modifiersOpt, Name name, List<JCTree.JCTypeParameter> typeParameters, JCTree.JCExpression extending, List<JCTree.JCExpression> implementing, List<JCTree> defs) {
        JCTree.JCClassDecl tree = new JCTree.JCClassDecl(modifiersOpt, name, typeParameters, extending, implementing, defs, null);
        tree.pos = pos;
        return tree;
    }


    public JCTree.JCBlock Block(long flags, List<JCTree.JCStatement> stats) {
        JCTree.JCBlock tree = new JCTree.JCBlock(flags, stats);
        tree.pos = pos;
        return tree;
    }

    public JCTree.JCPrimitiveType TypeIdent(int typetag) {
        JCTree.JCPrimitiveType tree = new JCTree.JCPrimitiveType(typetag);
        tree.pos = pos;
        return tree;
    }

    public JCTree.JCArrayTypeTree TypeArray(JCTree.JCExpression t) {
        JCTree.JCArrayTypeTree tree = new JCTree.JCArrayTypeTree(t);
        tree.pos = pos;
        return tree;
    }

    public JCTree.JCVariableDecl VarDef(JCTree.JCModifiers mods, Name name, JCTree.JCExpression type, JCTree.JCExpression init) {
        JCTree.JCVariableDecl tree = new JCTree.JCVariableDecl(mods, name, type, init, null);
        tree.pos = pos;
        return tree;
    }

    public JCTree.JCExpression NewArray(JCTree.JCExpression t, ArrayList<JCTree.JCExpression> dims, List<JCTree.JCExpression> elems) {
        JCTree.JCNewArray tree = new JCTree.JCNewArray(t, dims, elems);
        tree.pos = pos;
        return tree;
    }

    public JCTree.JCErroneous Erroeous() {
        JCTree.JCErroneous tree = new JCTree.JCErroneous(null);
        tree.pos = pos;
        return tree;
    }

    public JCTree.JCLiteral Literal(int tag, Object value) {

        JCTree.JCLiteral tree = new JCTree.JCLiteral(tag, value);
        tree.pos = pos;
        return tree;

    }

    public JCTree.JCModifiers Modifiers(long i) {
        return Modifiers(i, new ArrayList<JCTree.JCAnnotation>());
    }

    public JCTree.JCExpressionStatement Exec(JCTree.JCExpression expr) {
        JCTree.JCExpressionStatement tree = new JCTree.JCExpressionStatement(expr);
        tree.pos = pos;
        return tree;

    }

    public JCTree.JCUnary Unary(int unoptag, JCTree.JCExpression t) {
        JCTree.JCUnary tree = new JCTree.JCUnary(unoptag, t);
        tree.pos = pos;
        return tree;
    }

    public JCTree.JCNewClass NewClass(JCTree.JCExpression encl, List<JCTree.JCExpression> typeArgs, JCTree.JCExpression t, List<JCTree.JCExpression> arguments, JCTree.JCClassDecl def) {
        JCTree.JCNewClass tree = new JCTree.JCNewClass(encl, typeArgs, t, arguments, def);
        tree.pos = pos;
        return tree;
    }

    public JCTree.JCAssign Assign(JCTree.JCExpression result, JCTree.JCExpression expression) {
        JCTree.JCAssign tree = new JCTree.JCAssign(result, expression);
        tree.pos = pos;
        return tree;
    }

    public JCTree.JCAssignOp Assignop(int opcode, JCTree.JCExpression result, JCTree.JCExpression term) {
        JCTree.JCAssignOp tree = new JCTree.JCAssignOp(opcode, result, term, null);
        tree.pos = pos;
        return tree;
    }

    public JCTree.JCBinary Binary(int optag, JCTree.JCExpression jcExpression, JCTree.JCExpression jcExpression1) {
        JCTree.JCBinary tree = new JCTree.JCBinary(optag, jcExpression, jcExpression1, null);
        tree.pos = pos;
        return tree;
    }

    public JCTree.JCMethodInvocation Apply(List<JCTree.JCExpression> typeargs, JCTree.JCExpression t, List<JCTree.JCExpression> args) {
        JCTree.JCMethodInvocation tree = new JCTree.JCMethodInvocation(typeargs, t, args);
        tree.pos = pos;
        return tree;
    }

    public JCTree.JCMethodDecl MethodDef(JCTree.JCModifiers mods, Name name, JCTree.JCExpression type, List<JCTree.JCTypeParameter> typarams, List<JCTree.JCVariableDecl> params, List<JCTree.JCExpression> throwns, JCTree.JCBlock body, JCTree.JCExpression defaultValue) {
        JCTree.JCMethodDecl tree = new JCTree.JCMethodDecl(mods, name, type, typarams, params, throwns, body, defaultValue, null);
        tree.pos = pos;
        return tree;
    }

    public List<JCTree.JCVariableDecl> Params(List<Type> arg, Symbol noSymbol) {
        ArrayList<JCTree.JCVariableDecl> params = new ArrayList<>();
        int i = 0;
        for (Type type : arg) {
            params.add(Param(names.fromString("x" + i), type, noSymbol));
        }
        return params;
    }

    private JCTree.JCVariableDecl Param(Name name, Type type, Symbol noSymbol) {
        return VarDef(new Symbol.VarSymbol(0, name, type, noSymbol), null);
    }

    private JCTree.JCVariableDecl VarDef(Symbol.VarSymbol varSymbol, JCTree.JCExpression o) {
        return (JCTree.JCVariableDecl) new JCTree.JCVariableDecl(Modifiers(varSymbol.flags(), null), varSymbol.name, Type(varSymbol.type), o, varSymbol).setPos(pos).setType(varSymbol.type);
    }

    public JCTree.JCExpression  Type(Type type) {
        if (type == null) return null;

        JCTree.JCExpression tp = null;
        switch (type.tag) {
            case BYTE: case CHAR: case SHORT: case INT: case LONG: case FLOAT:
            case DOUBLE: case BOOLEAN: case VOID:
                tp = TypeIdent(type.tag);
                break;
            case CLASS:
                break;
        }
        return tp;
    }

    public JCTree.JCStatement Assignment(Symbol sym, JCTree.JCExpression init) {
        return Exec(Assign(Ident(sym), init).setType(sym.type));
    }

    private JCTree.JCIdent Ident(Symbol sym) {
        return (JCTree.JCIdent) new JCTree.JCIdent((sym.name != names.empty) ? sym.name : sym.flatName(), sym).setPos(pos).setType(sym.type);
    }

    public List<JCTree.JCExpression> Idents(List<JCTree.JCVariableDecl> params) {
        ArrayList<JCTree.JCExpression> ids = new ArrayList<>();
        for (JCTree.JCVariableDecl param : params) {
            ids.add(Ident(param));
        }
        return ids;
    }

    private JCTree.JCExpression Ident(JCTree.JCVariableDecl param) {
        return Ident(param.sym);
    }

    public JCTree.JCMethodDecl MethodDef(Symbol.MethodSymbol clinit, JCTree.JCBlock block) {
        return MethodDef(clinit, clinit.type, block);
    }

    private JCTree.JCMethodDecl MethodDef(Symbol.MethodSymbol m, Type mtype, JCTree.JCBlock body) {
        return (JCTree.JCMethodDecl) new JCTree.JCMethodDecl(Modifiers(m.flags(), null),
                m.name,
                Type(mtype.getReturnType()),
                new ArrayList<JCTree.JCTypeParameter>(),
                Params(mtype.getParameterTypes(), m),
                new ArrayList<JCTree.JCExpression>(),
                body,
                null,
                m).setPos(pos).setType(mtype);
    }


    public JCTree.JCReturn Return(JCTree.JCExpression result) {
        JCTree.JCReturn tree = new JCTree.JCReturn(result);
        tree.pos = pos;
        return tree;
    }
}
