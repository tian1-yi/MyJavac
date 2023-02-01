package com.myj.tools.javac.tree;

import com.myj.tools.javac.code.Scope;
import com.myj.tools.javac.code.Symbol;
import com.myj.tools.javac.code.Type;
import com.myj.tools.javac.jvm.Pool;
import com.myj.tools.javac.util.JavaFileObject;
import com.myj.tools.javac.util.Name;
import com.myj.tools.javac.util.Names;
import org.omg.CORBA.PUBLIC_MEMBER;

import java.util.List;

import static com.myj.tools.javac.code.Flags.PUBLIC;
import static com.myj.tools.javac.code.Flags.STATIC;

public abstract class JCTree implements Tree {

    public static final int  TOPLEVEL = 1;

    /** Import clauses, of type Import.
     */
    public static final int IMPORT = TOPLEVEL + 1;

    /** Class definitions, of type ClassDef.
     */
    public static final int CLASSDEF = IMPORT + 1;

    /** Method definitions, of type MethodDef.
     */
    public static final int METHODDEF = CLASSDEF + 1;

    /** Variable definitions, of type VarDef.
     */
    public static final int VARDEF = METHODDEF + 1;

    /** The no-op statement ";", of type Skip
     */
    public static final int SKIP = VARDEF + 1;

    /** Blocks, of type Block.
     */
    public static final int BLOCK = SKIP + 1;

    /** Do-while loops, of type DoLoop.
     */
    public static final int DOLOOP = BLOCK + 1;

    /** While-loops, of type WhileLoop.
     */
    public static final int WHILELOOP = DOLOOP + 1;

    /** For-loops, of type ForLoop.
     */
    public static final int FORLOOP = WHILELOOP + 1;

    /** Foreach-loops, of type ForeachLoop.
     */
    public static final int FOREACHLOOP = FORLOOP + 1;

    /** Labelled statements, of type Labelled.
     */
    public static final int LABELLED = FOREACHLOOP + 1;

    /** Switch statements, of type Switch.
     */
    public static final int SWITCH = LABELLED + 1;

    /** Case parts in switch statements, of type Case.
     */
    public static final int CASE = SWITCH + 1;

    /** Synchronized statements, of type Synchonized.
     */
    public static final int SYNCHRONIZED = CASE + 1;

    /** Try statements, of type Try.
     */
    public static final int TRY = SYNCHRONIZED + 1;

    /** Catch clauses in try statements, of type Catch.
     */
    public static final int CATCH = TRY + 1;

    /** Conditional expressions, of type Conditional.
     */
    public static final int CONDEXPR = CATCH + 1;

    /** Conditional statements, of type If.
     */
    public static final int IF = CONDEXPR + 1;

    /** Expression statements, of type Exec.
     */
    public static final int EXEC = IF + 1;

    /** Break statements, of type Break.
     */
    public static final int BREAK = EXEC + 1;

    /** Continue statements, of type Continue.
     */
    public static final int CONTINUE = BREAK + 1;

    /** Return statements, of type Return.
     */
    public static final int RETURN = CONTINUE + 1;

    /** Throw statements, of type Throw.
     */
    public static final int THROW = RETURN + 1;

    /** Assert statements, of type Assert.
     */
    public static final int ASSERT = THROW + 1;

    /** Method invocation expressions, of type Apply.
     */
    public static final int APPLY = ASSERT + 1;

    /** Class instance creation expressions, of type NewClass.
     */
    public static final int NEWCLASS = APPLY + 1;

    /** Array creation expressions, of type NewArray.
     */
    public static final int NEWARRAY = NEWCLASS + 1;

    /** Parenthesized subexpressions, of type Parens.
     */
    public static final int PARENS = NEWARRAY + 1;

    /** Assignment expressions, of type Assign.
     */
    public static final int ASSIGN = PARENS + 1;

    /** Type cast expressions, of type TypeCast.
     */
    public static final int TYPECAST = ASSIGN + 1;

    /** Type test expressions, of type TypeTest.
     */
    public static final int TYPETEST = TYPECAST + 1;

    /** Indexed array expressions, of type Indexed.
     */
    public static final int INDEXED = TYPETEST + 1;

    /** Selections, of type Select.
     */
    public static final int SELECT = INDEXED + 1;

    /** Simple identifiers, of type Ident.
     */
    public static final int IDENT = SELECT + 1;

    /** Literals, of type Literal.
     */
    public static final int LITERAL = IDENT + 1;

    /** Basic type identifiers, of type TypeIdent.
     */
    public static final int TYPEIDENT = LITERAL + 1;

    /** Array types, of type TypeArray.
     */
    public static final int TYPEARRAY = TYPEIDENT + 1;

    /** Parameterized types, of type TypeApply.
     */
    public static final int TYPEAPPLY = TYPEARRAY + 1;

    /** Union types, of type TypeUnion
     */
    public static final int TYPEUNION = TYPEAPPLY + 1;

    /** Formal type parameters, of type TypeParameter.
     */
    public static final int TYPEPARAMETER = TYPEUNION + 1;

    /** Type argument.
     */
    public static final int WILDCARD = TYPEPARAMETER + 1;

    /** Bound kind: extends, super, exact, or unbound
     */
    public static final int TYPEBOUNDKIND = WILDCARD + 1;

    /** metadata: Annotation.
     */
    public static final int ANNOTATION = TYPEBOUNDKIND + 1;

    /** metadata: Modifiers
     */
    public static final int MODIFIERS = ANNOTATION + 1;

    public static final int ANNOTATED_TYPE = MODIFIERS + 1;

    /** Error trees, of type Erroneous.
     */
    public static final int ERRONEOUS = ANNOTATED_TYPE + 1;

    /** Unary operators, of type Unary.
     */
    public static final int POS = ERRONEOUS + 1;             // +
    public static final int NEG = POS + 1;                   // -
    public static final int NOT = NEG + 1;                   // !
    public static final int COMPL = NOT + 1;                 // ~
    public static final int PREINC = COMPL + 1;              // ++ _
    public static final int PREDEC = PREINC + 1;             // -- _
    public static final int POSTINC = PREDEC + 1;            // _ ++
    public static final int POSTDEC = POSTINC + 1;           // _ --

    /** unary operator for null reference checks, only used internally.
     */
    public static final int NULLCHK = POSTDEC + 1;

    /** Binary operators, of type Binary.
     */
    public static final int OR = NULLCHK + 1;                // ||
    public static final int AND = OR + 1;                    // &&
    public static final int BITOR = AND + 1;                 // |
    public static final int BITXOR = BITOR + 1;              // ^
    public static final int BITAND = BITXOR + 1;             // &
    public static final int EQ = BITAND + 1;                 // ==
    public static final int NE = EQ + 1;                     // !=
    public static final int LT = NE + 1;                     // <
    public static final int GT = LT + 1;                     // >
    public static final int LE = GT + 1;                     // <=
    public static final int GE = LE + 1;                     // >=
    public static final int SL = GE + 1;                     // <<
    public static final int SR = SL + 1;                     // >>
    public static final int USR = SR + 1;                    // >>>
    public static final int PLUS = USR + 1;                  // +
    public static final int MINUS = PLUS + 1;                // -
    public static final int MUL = MINUS + 1;                 // *
    public static final int DIV = MUL + 1;                   // /
    public static final int MOD = DIV + 1;                   // %

    /** Assignment operators, of type Assignop.
     */
    public static final int BITOR_ASG = MOD + 1;             // |=
    public static final int BITXOR_ASG = BITOR_ASG + 1;      // ^=
    public static final int BITAND_ASG = BITXOR_ASG + 1;     // &=

    public static final int SL_ASG = SL + BITOR_ASG - BITOR; // <<=
    public static final int SR_ASG = SL_ASG + 1;             // >>=
    public static final int USR_ASG = SR_ASG + 1;            // >>>=
    public static final int PLUS_ASG = USR_ASG + 1;          // +=
    public static final int MINUS_ASG = PLUS_ASG + 1;        // -=
    public static final int MUL_ASG = MINUS_ASG + 1;         // *=
    public static final int DIV_ASG = MUL_ASG + 1;           // /=
    public static final int MOD_ASG = DIV_ASG + 1;           // %=

    /** A synthetic let expression, of type LetExpr.
     */
    public static final int LETEXPR = MOD_ASG + 1;           // ala scheme


    /** The offset between assignment operators and normal operators.
     */
    public static final int ASGOffset = BITOR_ASG - BITOR;


    public int pos;

    public Type type;

    public JCTree setPos(int pos) {
        this.pos = pos;
        return this;
    }

    public JCTree setType(Type type) {
        this.type = type;
        return this;
    }

    public abstract int getTag();

    public abstract void accept(Visitor visitor);


    /**
     * 编译单元
     */
    public static class JCCompilationUnit extends JCTree {

        /**
         * 包注解
         */
        public List<JCAnnotation> packageAnnotations;
        /**
         * 包声明
         */
        public JCExpression pid;
        /**
         * 编译单元的jctree
         */
        public List<JCTree> defs;
        public JavaFileObject sourceFile;
        public Symbol.PackageSymbol packageSymbol;
        public Scope.ImportScope namedImportScope;
        public Scope.StarImportScope starImportScope;




        public JCCompilationUnit(List<JCAnnotation> packageAnnotations,
                                 JCExpression pid,
                                 List<JCTree> defs,
                                 JavaFileObject sourceFile,
                                 Symbol.PackageSymbol packageSymbol,
                                 Scope.ImportScope namedImportScope,
                                 Scope.StarImportScope starImportScope) {
            this.packageAnnotations = packageAnnotations;
            this.pid = pid;
            this.defs = defs;
            this.sourceFile = sourceFile;
            this.packageSymbol = packageSymbol;
            this.namedImportScope = namedImportScope;
            this.starImportScope = starImportScope;
        }

        @Override
        public int getTag() {
            return TOPLEVEL;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitTopLevel(this);
        }

        @Override
        public Kind getKind() {
            return Kind.COMPILATION_UNIT;
        }
    }

    public static class JCMethodDecl extends JCTree implements MethodTree {

        public JCModifiers mods;
        public Name name;
        public JCExpression restype;
        public List<JCTypeParameter> typarams;
        public List<JCVariableDecl> params;
        public List<JCExpression> thrown;
        public JCBlock body;
        public JCExpression defaultValue; // for annotation types
        public Symbol.MethodSymbol sym;

        public JCMethodDecl(JCModifiers mods, Name name, JCExpression restype, List<JCTypeParameter> typarams, List<JCVariableDecl> params, List<JCExpression> thrown, JCBlock body, JCExpression defaultValue, Symbol.MethodSymbol sym) {
            this.mods = mods;
            this.name = name;
            this.restype = restype;
            this.typarams = typarams;
            this.params = params;
            this.thrown = thrown;
            this.body = body;
            this.defaultValue = defaultValue;
            this.sym = sym;
        }

        @Override
        public int getTag() {
            return METHODDEF;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitMethodDef(this);
        }

        @Override
        public Kind getKind() {
            return Kind.METHOD;
        }
    }

    public static abstract class JCStatement extends JCTree implements StatementTree {

        @Override
        public void accept(Visitor visitor) {

        }

        @Override
        public Kind getKind() {
            return null;
        }
    }

    public static class JCExpressionStatement extends JCStatement implements ExpressionStatementTree {

        public JCExpression expr;

        public JCExpressionStatement(JCExpression expr) {
            this.expr = expr;
        }

        @Override
        public Kind getKind() {
            return Kind.EXPRESSION_STATEMENT;
        }

        @Override
        public int getTag() {
            return EXEC;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitExec(this);
        }
    }

    public static class JCBlock extends JCStatement implements BlockTree {
        public long flags;
        public List<JCStatement> stats;

        public JCBlock(long flags, List<JCStatement> stats) {
            this.flags = flags;
            this.stats = stats;
        }

        @Override
        public Kind getKind() {
            return Kind.BLOCK;
        }

        @Override
        public int getTag() {
            return BLOCK;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBlock(this);
        }
    }

    public static class JCReturn extends JCStatement implements ReturnTree {

        public JCExpression expr;

        public JCReturn(JCExpression expr) {
            this.expr = expr;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitReturn(this);
        }

        @Override
        public Kind getKind() {
            return Kind.RETURN;
        }

        @Override
        public int getTag() {
            return RETURN;
        }
    }

    public static class JCClassDecl extends JCStatement implements ClassDeclTree {
        public JCModifiers mods;
        public Name name;
        public List<JCTypeParameter> typarams;
        public JCExpression extending;
        public List<JCExpression> implementing;
        public List<JCTree> defs;
        public Symbol.ClassSymbol sym;


        public JCClassDecl(JCModifiers mods, Name name, List<JCTypeParameter> typarams, JCExpression extending, List<JCExpression> implementing, List<JCTree> defs, Symbol.ClassSymbol sym) {
            this.mods = mods;
            this.name = name;
            this.typarams = typarams;
            this.extending = extending;
            this.implementing = implementing;
            this.defs = defs;
            this.sym = sym;
        }

        @Override
        public int getTag() {
            return CLASSDEF;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitClassDef(this);
        }
    }

    public static class JCVariableDecl extends JCStatement implements VariableTree {

        // 修饰
        public JCModifiers mods;
        // 变量名称
        public Name name;
        // 变量类型
        public JCExpression vartype;
        // 变量初始值
        public JCExpression init;
        //
        public Symbol.VarSymbol sym;

        public JCVariableDecl(JCModifiers mods, Name name, JCExpression vartype, JCExpression init, Symbol.VarSymbol sym) {
            this.mods = mods;
            this.name = name;
            this.vartype = vartype;
            this.init = init;
            this.sym = sym;
        }

        @Override
        public Kind getKind() {
            return Kind.VARIABLE;
        }

        @Override
        public int getTag() {
            return VARDEF;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitVarDef(this);
        }
    }

    public static class JCTypeParameter extends JCTree implements TypeParameterTree {

        public Name name;
        public List<JCExpression> bounds;

        public JCTypeParameter(Name name, List<JCExpression> bounds) {
            this.name = name;
            this.bounds = bounds;
        }

        @Override
        public int getTag() {
            return TYPEPARAMETER;
        }

        @Override
        public void accept(Visitor visitor) {

        }

        @Override
        public Kind getKind() {
            return Kind.TYPE_PARAMETER;
        }
    }

    /**
     * 导入单元对象
     */
    public static class JCImport extends JCTree implements ImportTree {

        public JCTree pid;

        public boolean staticImport;

        public JCImport(JCTree pid, boolean staticImport) {
            this.pid = pid;
            this.staticImport = staticImport;
        }

        @Override
        public int getTag() {
            return IMPORT;
        }

        @Override
        public void accept(Visitor visitor) {

        }

        @Override
        public Kind getKind() {
            return Kind.IMPORT;
        }
    }

    public static abstract class JCExpression extends JCTree {

        @Override
        public JCExpression setPos(int pos) {
            super.setPos(pos);
            return this;
        }

        @Override
        public JCExpression setType(Type type) {
            super.setType(type);
            return this;
        }
    }

    public static class JCLiteral extends JCExpression implements LiteralTree {
        public int typetag;
        public Object value;

        public JCLiteral(int typetag, Object value) {
            this.typetag = typetag;
            this.value = value;
        }

        @Override
        public int getTag() {
            return LITERAL;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitLiteral(this);
        }

        @Override
        public Kind getKind() {
            return Kind.LITERAL;
        }
    }

    public static class JCMethodInvocation extends JCExpression implements MethodInvocationTree {
        public List<JCExpression> typeargs;
        public JCExpression meth;
        public List<JCExpression> args;
        public Type varargsElement;

        public JCMethodInvocation(List<JCExpression> typeargs, JCExpression meth, List<JCExpression> args) {
            this.typeargs = typeargs;
            this.meth = meth;
            this.args = args;
        }

        @Override
        public int getTag() {
            return (APPLY);
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitApply(this);
        }

        @Override
        public Kind getKind() {
            return Kind.METHOD_INVOCATION;
        }
    }

    public static class JCErroneous extends JCExpression implements ErroneousTree {

        public List<? extends JCTree> errs;

        public JCErroneous(List<? extends JCTree> errs) {
            this.errs = errs;
        }

        @Override
        public int getTag() {
            return ERRONEOUS;
        }

        @Override
        public void accept(Visitor visitor) {

        }

        @Override
        public Kind getKind() {
            return Kind.ERRONEOUS;
        }
    }

    public static class JCNewArray extends JCExpression implements NewArrayTree {
        public JCExpression elemtype;
        public List<JCExpression> dims;
        public List<JCExpression> elems;

        public JCNewArray(JCExpression elemtype, List<JCExpression> dims, List<JCExpression> elems) {
            this.elemtype = elemtype;
            this.dims = dims;
            this.elems = elems;
        }

        @Override
        public int getTag() {
            return NEWARRAY;
        }

        @Override
        public void accept(Visitor visitor) {

        }

        @Override
        public Kind getKind() {
            return Kind.NEWARRAY;
        }
    }

    public static class JCArrayTypeTree extends JCExpression implements ArrayTypeTree {

        public JCExpression element;

        public JCArrayTypeTree(JCExpression element) {
            this.element = element;
        }

        @Override
        public int getTag() {
            return TYPEARRAY;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitTypeArray(this);
        }

        @Override
        public Kind getKind() {
            return Kind.ArrayType;
        }
    }

    /**
     * 标识符
     */
    public static class JCIdent extends JCExpression implements IdentifierTree {

        public Name name;

        public Symbol symbol;

        public JCIdent(Name name, Symbol symbol) {
            this.name = name;
            this.symbol = symbol;
        }

        @Override
        public int getTag() {
            return IDENT;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitIdent(this);
        }

        @Override
        public Kind getKind() {
            return Kind.IDENTIFIER;
        }
    }

    /**
     * 赋值
     */
    public static class JCAssign extends JCExpression implements AssignTree {

        public JCExpression lhs;
        public JCExpression rhs;

        public JCAssign(JCExpression lhs, JCExpression rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public int getTag() {
            return ASSIGN;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitAssign(this);
        }

        @Override
        public Kind getKind() {
            return Kind.ASSIGN;
        }
    }

    public static class JCBinary extends JCExpression implements BinaryTree {

        private int opcode;
        public JCExpression lhs;
        public JCExpression rhs;
        public Symbol operator;

        public JCBinary(int opcode, JCExpression lhs, JCExpression rhs, Symbol operator) {
            this.opcode = opcode;
            this.lhs = lhs;
            this.rhs = rhs;
            this.operator = operator;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitBinary(this);
        }

        @Override
        public Kind getKind() {
            return TreeInfo.tagToKind(getTag());
        }

        public int getTag() {
            return opcode;
        }
    }

    public static class JCAssignOp extends JCExpression implements CompoundAssignmentTree {

        private int opcode;
        public JCExpression lhs;
        public JCExpression rhs;
        public Symbol operator;

        public JCAssignOp(int opcode, JCExpression lhs, JCExpression rhs, Symbol operator) {
            this.opcode = opcode;
            this.lhs = lhs;
            this.rhs = rhs;
            this.operator = operator;
        }

        @Override
        public int getTag() {
            return opcode;
        }

        @Override
        public void accept(Visitor visitor) {

        }

        @Override
        public Kind getKind() {
            return null;
        }
    }


    /**
     * 注解
     */
    public static class JCAnnotation extends JCExpression implements AnnotaionTree {

        @Override
        public int getTag() {
            return ANNOTATION;
        }

        @Override
        public void accept(Visitor visitor) {

        }

        @Override
        public Kind getKind() {
            return Kind.ANNOTAION;
        }
    }

    public static class JCPrimitiveType extends JCExpression implements PrimitiveTypeTree {

        public int typetag;

        public JCPrimitiveType(int typetag) {
            this.typetag = typetag;
        }

        @Override
        public int getTag() {
            return TYPEIDENT;
        }

        @Override
        public void accept(Visitor visitor) {
            visitor.visitTypeIdent(this);
        }

        @Override
        public Kind getKind() {
            return Kind.PRIMITIVETYPE;
        }
    }

    /**
     * 用于修饰
     */
    public static class JCModifiers extends JCTree implements ModifiersTree {

        public long flags;

        public List<JCAnnotation> annotations;

        public JCModifiers(long flags, List<JCAnnotation> annotations) {
            this.flags = flags;
            this.annotations = annotations;
        }

        @Override
        public int getTag() {
            return MODIFIERS;
        }

        @Override
        public void accept(Visitor visitor) {

        }

        @Override
        public Kind getKind() {
            return Kind.MODIFIERS;
        }
    }

    public static class JCUnary extends JCExpression implements UnaryTree {


        private int opcode;
        public JCExpression arg;
        public Symbol operator;

        public JCUnary(int opcode, JCExpression arg) {
            this.opcode = opcode;
            this.arg = arg;
        }

        @Override
        public int getTag() {
            return opcode;
        }

        @Override
        public void accept(Visitor visitor) {

        }

        @Override
        public Kind getKind() {
            return Kind.UNARY;
        }

        public void setTag(int tag) {
            opcode = tag;
        }
    }

    public static class JCNewClass extends JCExpression implements NewClassTree {

        public JCExpression encl;
        public List<JCExpression> typeargs;
        public JCExpression clazz;
        public List<JCExpression> args;
        public JCClassDecl def;
        public Symbol constructor;
        public Type varargsElement;
        public Type constructorType;

        public JCNewClass(JCExpression encl, List<JCExpression> typeargs, JCExpression clazz, List<JCExpression> args, JCClassDecl def) {
            this.encl = encl;
            this.typeargs = typeargs;
            this.clazz = clazz;
            this.args = args;
            this.def = def;
        }

        @Override
        public int getTag() {
            return NEWCLASS;
        }

        @Override
        public void accept(Visitor visitor) {

        }

        @Override
        public Kind getKind() {
            return Kind.NEW_CLASS;
        }
    }

    /**
     *
     */
    public static class JCFieldAccess extends JCExpression implements FieldAccessTree {


        public JCExpression selected;

        public Name name;
        public Symbol sym;

        public JCFieldAccess(JCExpression selected, Name name, Symbol sym) {
            this.selected = selected;
            this.name = name;
            this.sym = sym;
        }

        @Override
        public int getTag() {
            return SELECT;
        }

        @Override
        public void accept(Visitor visitor) {

        }

        @Override
        public Kind getKind() {
            return Kind.FIELDACCESS;
        }
    }




    public static abstract class Visitor {

        public void visitTopLevel(JCCompilationUnit jcCompilationUnit) {

        }

        public void visitClassDef(JCClassDecl jcClassDecl) {
        }

        public void visitVarDef(JCVariableDecl jcVariableDecl) {
            visitTree(jcVariableDecl);
        }

        public void visitTree(JCTree tree) {

        }

        public void visitMethodDef(JCMethodDecl jcMethodDecl) {
            visitTree(jcMethodDecl);
        }

        public void visitTypeIdent(JCPrimitiveType jcPrimitiveType) {
            visitTree(jcPrimitiveType);
        }

        public void visitTypeArray(JCArrayTypeTree jcArrayTypeTree) {
            visitTree(jcArrayTypeTree);
        }

        public void visitIdent(JCIdent jcIdent) {
            visitTree(jcIdent);
        }

        public void visitBlock(JCBlock jcBlock) {
            visitTree(jcBlock);
        }

        public void visitLiteral(JCLiteral jcLiteral) {
            visitTree(jcLiteral);
        }

        public void visitExec(JCExpressionStatement jcExpressionStatement) {
            visitTree(jcExpressionStatement);
        }

        public void visitAssign(JCAssign jcAssign) {
            visitTree(jcAssign);
        }

        public void visitBinary(JCBinary jcBinary) {
            visitTree(jcBinary);
        }

        public void visitApply(JCMethodInvocation jcMethodInvocation) {
            visitTree(jcMethodInvocation);
        }

        public void visitReturn(JCReturn jcReturn) {
            visitTree(jcReturn);
        }
    }

    public static class OperatorSymbol extends Symbol.MethodSymbol {

        public int opcode;

        public OperatorSymbol(Name name, Type type, int opcode, Symbol owner) {
            super(PUBLIC | STATIC, name, type, owner);
            this.opcode = opcode;
        }


    }

}
