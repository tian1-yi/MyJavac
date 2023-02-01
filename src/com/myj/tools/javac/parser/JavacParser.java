package com.myj.tools.javac.parser;

import com.myj.tools.javac.code.Flags;
import com.myj.tools.javac.code.TypeTags;
import com.myj.tools.javac.tree.JCTree;
import com.myj.tools.javac.tree.TreeInfo;
import com.myj.tools.javac.util.*;

import java.util.ArrayList;
import java.util.List;

public class JavacParser implements Parser{


    static final int EXPR = 0x1;
    static final int TYPE = 0x2;
    static final int NOPARAMS = 0x4;
    static final int TYPEARG = 0x8;
    static final int DIAMOND = 0x10;

    public int mode;

    public int lastmode;

    public Lexer lexer;

    private Names names;

    private TreeMaker treeMaker;

    public JCTree.JCErroneous errorTree;


    public List<JCTree.JCExpression[]> odStackSupply = new ArrayList<>();

    public List<Token[]> opStackSuply = new ArrayList<>();

    public List<int[]> posStackSupply = new ArrayList<>();



    public JavacParser(ParseFactory parseFactory, Lexer lexer) {
        this.lexer = lexer;
        lexer.nextToken();
        this.names = parseFactory.names;
        this.treeMaker = parseFactory.treeMaker;
        this.errorTree = treeMaker.Erroeous();

    }

    /**
     * 解析编译单元
     * @return
     * CompilationUnit = [ { "@" Annotation } PACKAGE Qualident ";"] {ImportDeclaration} {TypeDeclaration}
     *
     */
    @Override
    public JCTree.JCCompilationUnit parseCompilation() {
        int pos = lexer.pos();
        JCTree.JCExpression pid = null;
        JCTree.JCModifiers modifiers = null;
        List<JCTree.JCAnnotation> packageAnnotation = null;
        // 解析包声明
        if (lexer.token() == Token.PACKAGE) {
            lexer.nextToken();
            pid = qualident();
            accept(Token.SEMI);
        }

        List<JCTree> defs = new ArrayList<>();

        // 解析引入声明、类声明
        while (lexer.token() != Token.EOF) {
            if (lexer.token() == Token.IMPORT) {
                defs.add(importDeclaration());
            } else {
                defs.add(typeDeclaration(modifiers));
            }
        }
        JCTree.JCCompilationUnit compilationUnit = treeMaker.at(pos).TopLevel(packageAnnotation, pid, defs);

        return compilationUnit;
    }

    /**
     * 解析接口、类、元组
     * @param parital
     * @return
     */
    JCTree typeDeclaration(JCTree.JCModifiers parital) {

        return classOrInterfaceOrEnumDeclaration(modifiersOpt(parital));
    }

    /**
     * 解析接口、类、元组
     * @param modifiersOpt
     * @return
     */
    private JCTree.JCStatement classOrInterfaceOrEnumDeclaration(JCTree.JCModifiers modifiersOpt) {
        if (lexer.token() == Token.CLASS) {
            return classDeclaration(modifiersOpt);
        } else if (lexer.token() == Token.INTERFACE) {
            return interfaceDeclaration(modifiersOpt);
        } else {
            return enumDeclaration(modifiersOpt);
        }
    }

    /**
     * 解析元组具体实现
     * @param modifiersOpt
     * @return
     */
    private JCTree.JCClassDecl enumDeclaration(JCTree.JCModifiers modifiersOpt) {
        return null;
    }

    /**
     * 解析接口具体实现
     * @param modifiersOpt
     * @return
     */
    private JCTree.JCClassDecl interfaceDeclaration(JCTree.JCModifiers modifiersOpt) {
        return null;
    }

    /**
     * 解析类具体实现
     * ClassDeclaration = CLASS Ident TypeParametersOpt [EXTENDS Type]
     *                          [IMPLEMENTS TypeList] ClassBody
     * @param modifiersOpt
     * @return
     */
    private JCTree.JCClassDecl classDeclaration(JCTree.JCModifiers modifiersOpt) {
        int pos = lexer.pos();
        accept(Token.CLASS);
        Name name = ident();// 解析名称

        // 解析父类
        JCTree.JCExpression extending = null;
        if (lexer.token() == Token.EXTENDS) {
            lexer.nextToken();
            extending = parseType();
        }

        // 解析接口
        List<JCTree.JCExpression> implementing = new ArrayList<>();
        if (lexer.token() == Token.IMPLEMENTS) {
            lexer.nextToken();
            implementing = typeList();
        }

        // 解析body
        List<JCTree> defs = classOrInterfaceBody(name, false);
        JCTree.JCClassDecl result = treeMaker.at(pos).ClassDef(modifiersOpt, name, null, extending, implementing, defs);
        return result;
    }

    /**
     * 解析class或接口body
     * @param name
     * @param b
     * @return
     */
    private List<JCTree> classOrInterfaceBody(Name name, boolean b) {
        accept(Token.LBRACE);

        List<JCTree> defs = new ArrayList<>();
        while (lexer.token() != Token.RBRACE && lexer.token() != Token.EOF) {
            defs.addAll(classOrInterfaceBodyDeclaration(name, b));
        }

        accept(Token.RBRACE);
        return defs;
    }

    /**
     * body的解析
     * @param name
     * @param isInterface
     * @return
     */
    private List<JCTree> classOrInterfaceBodyDeclaration(Name name, boolean isInterface) {
        if (lexer.token() == Token.SEMI) {
            lexer.nextToken();
            return new ArrayList<>();
        } else {
            int pos = lexer.pos();
            ArrayList<JCTree> result = new ArrayList<>();
            JCTree.JCModifiers mods = modifiersOpt(); // 修饰符
            if (lexer.token() == Token.CLASS || lexer.token() == Token.INTERFACE || lexer.token() == Token.ENUM) {
                // 内部类解析
                result.add(classOrInterfaceOrEnumDeclaration(mods));
                return result;
            } else if (lexer.token() == Token.LBRACE){
                // 解析块
                result.add(block(pos, mods.flags));
                return result;
            } else {
                // 解析成员变量或方法
                pos = lexer.pos();
                // 泛型
                List<JCTree.JCTypeParameter> typarams = typeParameterOpt();
                Name vName = lexer.name();
                pos = lexer.pos();

                // 解析变量或方法类型
                JCTree.JCExpression type;
                boolean isVoid = lexer.token() == Token.VOID;
                if (isVoid) {
                    type = treeMaker.TypeIdent(TypeTags.VOID);
                    lexer.nextToken();
                } else {
                    type = parseType();
                }

                pos = lexer.pos();
                vName = ident();
                if (lexer.token() == Token.LPAREN) {
                    // 方法解析
                    JCTree jcStatement = methodDeclaratorRest(pos, mods, type, vName, typarams, isInterface, isVoid);
                    result.add(jcStatement);
                    return result;
                } else if (!isVoid && typarams.isEmpty()) {
                    ArrayList<JCTree.JCVariableDecl> jcTrees = new ArrayList<>();
                    // 变量解析
                    List<JCTree.JCVariableDecl> jcVariableDecls = variableDeclaratorsRest(pos, mods, type, vName, isInterface, jcTrees);
                    result.addAll(jcVariableDecls);
                    accept(Token.SEMI);
                    return result;
                } else {
                    return result;
                }


            }
        }
    }

    private List<JCTree.JCVariableDecl> variableDeclaratorsRest(int pos,
                                                                JCTree.JCModifiers mods,
                                                                JCTree.JCExpression type,
                                                                Name name,
                                                                boolean reqInit,
                                                                List<JCTree.JCVariableDecl> trees) {
        // 添加变量
        trees.add(variableDeclaratorsRest(pos, mods, type, name, reqInit));
        while (lexer.token() == Token.COMMA) {
            lexer.nextToken();
            trees.add(variableDeclaratorsRest(mods, type, reqInit));
        }
        return trees;
    }

    private JCTree.JCVariableDecl variableDeclaratorsRest(JCTree.JCModifiers mods, JCTree.JCExpression type, boolean reqInit) {
        return variableDeclaratorsRest(lexer.pos(), mods, type, ident(), reqInit);
    }

    private JCTree.JCVariableDecl variableDeclaratorsRest(int pos, JCTree.JCModifiers mods, JCTree.JCExpression type, Name name, boolean reqInit) {
        type = bracketsOpt(type);

        JCTree.JCExpression init = null;
        if (lexer.token() == Token.EQ) {
            lexer.nextToken();
            init = variableInitializer();
        } else if (reqInit) {

        }
        JCTree.JCVariableDecl result = treeMaker.at(pos).VarDef(mods, name, type, init);
        return result;
    }

    private JCTree.JCExpression variableInitializer() {
        return lexer.token() == Token.LBRACE ? arrayInitializer(lexer.pos(), null) : parseExpression();
    }

    private JCTree.JCExpression parseExpression() {
        return term(EXPR);
    }

    private JCTree.JCExpression arrayInitializer(int pos, JCTree.JCExpression t) {
        accept(Token.LBRACE);

        List<JCTree.JCExpression> elems = new ArrayList<>();
        if (lexer.token() == Token.COMMA) {
            lexer.nextToken();
        } else if (lexer.token() != Token.RBRACE) {
            elems.add(variableInitializer());
            while (lexer.token() == Token.COMMA) {
                lexer.nextToken();
                if (lexer.token() == Token.RBRACE) break;
                elems.add(variableInitializer());
            }
        }
        accept(Token.RBRACE);
        return treeMaker.at(pos).NewArray(t, new ArrayList<JCTree.JCExpression>(), elems);
    }

    private JCTree methodDeclaratorRest(int pos,
                                                    JCTree.JCModifiers mods,
                                                    JCTree.JCExpression type,
                                                    Name name, 
                                                    List<JCTree.JCTypeParameter> typarams,
                                                    boolean isInterface,
                                                    boolean isVoid) {

        List<JCTree.JCVariableDecl> params = formalParameters();

        List<JCTree.JCExpression> throwns = new ArrayList<>();
        if (lexer.token() == Token.THROWS) {
            lexer.nextToken();
            throwns = qualidentList();
        }

        JCTree.JCBlock body = null;

        if (lexer.token() == Token.LBRACE) {
            body = block();
        }

        JCTree.JCMethodDecl result = treeMaker.at(pos).MethodDef(mods, name, type, typarams, params, throwns, body, null);

        return result;
    }

    private JCTree.JCBlock block() {
        return block(lexer.pos(), 0);
    }


    /**
     * 解析多个标识符
     * @return
     */
    private List<JCTree.JCExpression> qualidentList() {
        List<JCTree.JCExpression> list = new ArrayList<>();
        list.add(qualident());
        while (lexer.token() == Token.COMMA) {
            list.add(qualident());
        }
        return list;
    }

    /**
     * 解析参数列表
     * @return
     */
    private List<JCTree.JCVariableDecl> formalParameters() {
        accept(Token.LPAREN);

        ArrayList<JCTree.JCVariableDecl> params = new ArrayList<>();

        if (lexer.token() != Token.RPAREN) {
            params.add(formalParameter());
            while (lexer.token() == Token.COMMA) {
                lexer.nextToken();
                params.add(formalParameter());
            }
        }
        accept(Token.RPAREN);
        return params;
    }

    /**
     * 解析参数
     * @return
     */
    private JCTree.JCVariableDecl formalParameter() {
        JCTree.JCModifiers mods = optFinal(Flags.PARAMETER);
        JCTree.JCExpression type = parseType();

        return variableDeclaratorId(mods, type);
    }

    private JCTree.JCModifiers optFinal(long flags) {
        JCTree.JCModifiers mods = modifiersOpt();

        mods.flags |= flags;
        return mods;
    }

    private JCTree.JCVariableDecl variableDeclaratorId(JCTree.JCModifiers mods, JCTree.JCExpression type) {
        int pos = lexer.pos();
        Name name = ident();
        type = bracketsOpt(type);
        return treeMaker.at(pos).VarDef(mods, name, type, null);
    }

    /** TypeParametersOpt = ["<" TypeParameter {"," TypeParameter} ">"]
     */
    private List<JCTree.JCTypeParameter> typeParameterOpt() {
        if (lexer.token() == Token.LT) {
            List<JCTree.JCTypeParameter> typarams = new ArrayList<>();
            lexer.nextToken();
            typarams.add(typeParameter());
            return typarams;
        } else {
            return new ArrayList<>();
        }
    }

    private JCTree.JCTypeParameter typeParameter() {
        int pos = lexer.pos();
        Name name = ident();
        return null;
    }

    /**
     * 解析块
     * @param pos
     * @param flags
     * @return
     */
    private JCTree.JCBlock block(int pos, long flags) {
        accept(Token.LBRACE);

        List<JCTree.JCStatement> stats = blockStatements();
        JCTree.JCBlock block = treeMaker.at(pos).Block(flags, stats);

        accept(Token.RBRACE);
        return block;
    }

    private List<JCTree.JCStatement> blockStatements() {
        List<JCTree.JCStatement> stats = new ArrayList<>();
        while (true) {
            int pos = lexer.pos();
            switch (lexer.token()) {
                case RBRACE: case CASE: case DEFAULT: case EOF:
                    return stats;
                case LBRACE: case IF: case FOR: case WHILE: case DO: case TRY:
                case SWITCH: case SYNCHRONIZED: case RETURN: case THROW: case BREAK:
                case CONTINUE: case SEMI: case ELSE: case FINALLY: case CATCH:
                    stats.add(parseStatement());
                    break;
                case INTERFACE:
                case CLASS:
                    stats.add(classOrInterfaceOrEnumDeclaration(modifiersOpt()));
                    break;

                default:
                    Name name = lexer.name();
                    JCTree.JCExpression t = term(EXPR | TYPE);
                    if (lexer.token() == Token.IDENTIFIER) {
                        // 解析变量
                        pos = lexer.pos();
                        JCTree.JCModifiers mods = treeMaker.at(Position.NOPOS).Modifiers(0);
                        treeMaker.at(pos);
                        stats.addAll(variableDeclaratorsRest(mods, t, new ArrayList<JCTree.JCVariableDecl>()));
                        accept(Token.SEMI);
                    } else {
                        // 语句
                        stats.add(treeMaker.at(pos).Exec(checkExprStat(t)));
                        accept(Token.SEMI);
                    }


            }

        }

    }

    private JCTree.JCExpression checkExprStat(JCTree.JCExpression t) {

        return t;
    }

    public List<JCTree.JCVariableDecl> variableDeclaratorsRest(JCTree.JCModifiers mods, JCTree.JCExpression type, List<JCTree.JCVariableDecl> defs) {
        return variableDeclaratorsRest(lexer.pos(), mods, type, ident(), false, defs);
    }

    private JCTree.JCStatement parseStatement() {
        int pos = lexer.pos();
        switch (lexer.token()) {
            case RETURN: {
                lexer.nextToken();
                JCTree.JCExpression result = lexer.token() == Token.SEMI ? null : parseExpression();
                JCTree.JCReturn t = treeMaker.at(pos).Return(result);
                accept(Token.SEMI);
                return t;
            }

        }
        return null;
    }

    private JCTree.JCModifiers modifiersOpt() {
        return modifiersOpt(null);
    }

    private List<JCTree.JCExpression> typeList() {
        List<JCTree.JCExpression> ts = new ArrayList<>();
        ts.add(parseType());
        while (lexer.token() == Token.COMMA) {
            lexer.nextToken();
            ts.add(parseType());
        }
        return ts;
    }

    private JCTree.JCExpression parseType() {
        return term(TYPE);
    }

    private JCTree.JCExpression term(int type) {
        int preMode = mode;
        mode = type;

        JCTree.JCExpression result = term();

        return result;
    }

    private JCTree.JCExpression term() {
        JCTree.JCExpression result = term1();
        if (lexer.token() == Token.EQ || Token.PLUSEQ.compareTo(lexer.token()) <= 0 && lexer.token().compareTo(Token.GTGTGTEQ) <= 0) {
            return termRest(result);
        }
        return result;
    }

    private JCTree.JCExpression termRest(JCTree.JCExpression result) {
        switch (lexer.token()) {
            case EQ: {
                int pos = lexer.pos();
                lexer.nextToken();
                JCTree.JCExpression expression = term();
                return treeMaker.at(pos).Assign(result, expression);
            }
            case PLUSEQ:
            case SUBEQ:
            case STAREQ:
            case SLASHEQ:
            case PERCENTEQ:
            case AMPEQ:
            case BAREQ:
            case CARETEQ:
            case LTLTEQ:
            case GTGTEQ:
            case GTGTGTEQ:
                int pos = lexer.pos();
                Token token = lexer.token();
                lexer.nextToken();
                JCTree.JCExpression term = term();
                return treeMaker.at(pos).Assignop(optag(token), result, term);
            default:
                return result;
        }
    }

    private int optag(Token token) {
        switch (token) {
            case BARBAR:
                return JCTree.OR;
            case AMPAMP:
                return JCTree.AND;
            case BAR:
                return JCTree.BITOR;
            case BAREQ:
                return JCTree.BITOR_ASG;
            case CARET:
                return JCTree.BITXOR;
            case CARETEQ:
                return JCTree.BITXOR_ASG;
            case AMP:
                return JCTree.BITAND;
            case AMPEQ:
                return JCTree.BITAND_ASG;
            case EQEQ:
                return JCTree.EQ;
            case BANGEQ:
                return JCTree.NE;
            case LT:
                return JCTree.LT;
            case GT:
                return JCTree.GT;
            case LTEQ:
                return JCTree.LE;
            case GTEQ:
                return JCTree.GE;
            case LTLT:
                return JCTree.SL;
            case LTLTEQ:
                return JCTree.SL_ASG;
            case GTGT:
                return JCTree.SR;
            case GTGTEQ:
                return JCTree.SR_ASG;
            case GTGTGT:
                return JCTree.USR;
            case GTGTGTEQ:
                return JCTree.USR_ASG;
            case PLUS:
                return JCTree.PLUS;
            case PLUSEQ:
                return JCTree.PLUS_ASG;
            case SUB:
                return JCTree.MINUS;
            case SUBEQ:
                return JCTree.MINUS_ASG;
            case STAR:
                return JCTree.MUL;
            case STAREQ:
                return JCTree.MUL_ASG;
            case SLASH:
                return JCTree.DIV;
            case SLASHEQ:
                return JCTree.DIV_ASG;
            case PERCENT:
                return JCTree.MOD;
            case PERCENTEQ:
                return JCTree.MOD_ASG;
            case INSTANCEOF:
                return JCTree.TYPETEST;
            default:
                return -1;
        }
    }

    private JCTree.JCExpression term1() {
        JCTree.JCExpression result = term2();
        return result;
    }

    private JCTree.JCExpression term2() {
        JCTree.JCExpression result = term3();
        if (prec(lexer.token()) >= TreeInfo.orPrec) {
            return term2Rest(result, TreeInfo.orPrec);
        }
        return result;
    }

    private JCTree.JCExpression term2Rest(JCTree.JCExpression result, int orPrec) {

        List<JCTree.JCExpression[]> saveOd = odStackSupply;
        JCTree.JCExpression[] odStack = newOdStack();
        Token[] opStack = newOpStack();
        odStack[0] = result;
        int top = 0;
        Token topOp = Token.ERROR;
        while (prec(lexer.token()) >= orPrec) {
            opStack[top] = topOp;
            top ++;
            topOp = lexer.token();
            lexer.nextToken();
            odStack[top] = term3();

            while (top > 0 && prec(topOp) >= prec(lexer.token())) {
                odStack[top - 1] = makeOp(0, topOp, odStack[top - 1], odStack[top]);
                top --;
                topOp = opStack[top];
            }
        }
        result = odStack[0];
        return result;
    }

    private JCTree.JCExpression makeOp(int i, Token topOp, JCTree.JCExpression jcExpression, JCTree.JCExpression jcExpression1) {
        return treeMaker.at(i).Binary(optag(topOp), jcExpression, jcExpression1);
    }

    private JCTree.JCExpression[] newOdStack() {
        return new JCTree.JCExpression[11];
    }

    public Token[] newOpStack() {
        return new Token[11];
    }

    private int prec(Token token) {
        int optag = optag(token);
        return (optag >= 0) ? TreeInfo.opPrec(optag) : -1;
    }

    /** Expression3    = PrefixOp Expression3
     *                 | "(" Expr | TypeNoParams ")" Expression3
     *                 | Primary {Selector} {PostfixOp}
     *  Primary        = "(" Expression ")"
     *                 | Literal
     *                 | [TypeArguments] THIS [Arguments]
     *                 | [TypeArguments] SUPER SuperSuffix
     *                 | NEW [TypeArguments] Creator
     *                 | Ident { "." Ident }
     *                   [ "[" ( "]" BracketsOpt "." CLASS | Expression "]" )
     *                   | Arguments
     *                   | "." ( CLASS | THIS | [TypeArguments] SUPER Arguments | NEW [TypeArguments] InnerCreator )
     *                   ]
     *                 | BasicType BracketsOpt "." CLASS
     *  PrefixOp       = "++" | "--" | "!" | "~" | "+" | "-"
     *  PostfixOp      = "++" | "--"
     *  Type3          = Ident { "." Ident } [TypeArguments] {TypeSelector} BracketsOpt
     *                 | BasicType
     *  TypeNoParams3  = Ident { "." Ident } BracketsOpt
     *  Selector       = "." [TypeArguments] Ident [Arguments]
     *                 | "." THIS
     *                 | "." [TypeArguments] SUPER SuperSuffix
     *                 | "." NEW [TypeArguments] InnerCreator
     *                 | "[" Expression "]"
     *  TypeSelector   = "." Ident [TypeArguments]
     *  SuperSuffix    = Arguments | "." Ident [Arguments]
     */
    private JCTree.JCExpression term3() {

        int pos = lexer.pos();
        JCTree.JCExpression t = null;


        switch (lexer.token()) {

            case PLUSPLUS: case SUBSUB: case BANG: case TILDE: case PLUS: case SUB:
                Token token = lexer.token();
                lexer.nextToken();
                if (token == Token.SUB && (lexer.token() == Token.INTLITERAL || lexer.token() == Token.DOUBLELITERAL) && lexer.radix() == 10) {
                    t = literal(names.hyphen);
                } else {
                    t = term3();
                    return treeMaker.at(pos).Unary(unoptag(token), t);
                }
                break;
            case IDENTIFIER:
                t = treeMaker.at(pos).Ident(ident());
                loop: while (true) {
                    pos = lexer.pos();
                    switch (lexer.token()) {
                        case LBRACKET:
                            //数组的解析
                            lexer.nextToken();
                            if (lexer.token() == Token.RBRACKET) {
                                lexer.nextToken();
                                // 数组解析
                                t = bracketsOpt(t);
                                t = treeMaker.at(pos).TypeArray(t);
                            } else {

                            }
                            break loop;
                        case LPAREN:
                            t = arguments(t);
                            break loop;
                        case DOT:

                            lexer.nextToken();

                            t = treeMaker.at(pos).Select(t, ident());
                            break;

                        default:
                            break loop;
                    }
                }
                break;
            case INTLITERAL: case LONGLITERAL: case FLOATLITERAL: case DOUBLELITERAL:
            case CHARLITERAL: case STRINGLITERAL:
            case TRUE: case FALSE: case NULL:
                t = literal(names.empty);
                break;
            case NEW:
                lexer.nextToken();
                t = creator(pos, null);
                break;
            case BYTE: case SHORT: case CHAR: case INT: case LONG: case FLOAT:
            case DOUBLE: case BOOLEAN:
                // 基本类型处理
                t = bracketShuffix(bracketsOpt(basicType()));
                break;

        }

        return t;

    }

    private JCTree.JCExpression term1Rest(JCTree.JCExpression term2Rest) {
        if (lexer.token() == Token.QUES) {
            return null;
        } else {
            return term2Rest;
        }
    }

    private JCTree.JCMethodInvocation arguments(JCTree.JCExpression t) {
        int pos = lexer.pos();
        List<JCTree.JCExpression> args = arguments();
        return treeMaker.at(pos).Apply(null, t, args);
    }

    private JCTree.JCExpression creator(int pos, List<JCTree.JCExpression> typeArgs) {
        // 创建数组
        switch (lexer.token()) {
            case BYTE: case SHORT: case CHAR: case INT: case LONG: case FLOAT:
            case DOUBLE: case BOOLEAN:
                return arrayCreatorRest(pos, basicType());

            default:
        }
        JCTree.JCExpression t = qualident();

        if (lexer.token() == Token.LPAREN) {
            return classCreatorRest(pos, null, typeArgs, t);
        } else {
            return null;
        }

    }

    private JCTree.JCNewClass classCreatorRest(int pos, JCTree.JCExpression encl, List<JCTree.JCExpression> typeArgs, JCTree.JCExpression t) {
        List<JCTree.JCExpression> arguments = arguments();

        return treeMaker.at(pos).NewClass(encl, typeArgs, t, arguments, null);
    }

    private List<JCTree.JCExpression> arguments() {

        ArrayList<JCTree.JCExpression> args = new ArrayList<>();
        if (lexer.token() == Token.LPAREN) {
            lexer.nextToken();
            if (lexer.token() != Token.RPAREN) {
                args.add(parseExpression());
                while (lexer.token() == Token.COMMA) {
                    lexer.nextToken();
                    args.add(parseExpression());
                }
            }
            accept(Token.RPAREN);
        }
        return args;
    }

    private JCTree.JCExpression arrayCreatorRest(int pos, JCTree.JCExpression basicType) {
        accept(Token.LBRACKET);
        if (lexer.token() == Token.RBRACKET) {
            //
            accept(Token.RBRACKET);
            basicType = bracketsOpt(basicType);
            if (lexer.token() == Token.LBRACE) {
                return arrayInitializer(pos, basicType);
            } else {
                return null;
            }
        } else {
            // 维度
            ArrayList<JCTree.JCExpression> dims = new ArrayList<>();
            dims.add(parseExpression());
            accept(Token.RBRACKET);
            while (lexer.token() == Token.LBRACKET) {
                int pos1 = lexer.pos();
                lexer.nextToken();
                if (lexer.token() == Token.RBRACKET) {
                    basicType = bracketsOptCont(basicType, pos1);
                } else {
                    dims.add(parseExpression());
                    accept(Token.RBRACKET);
                }
            }
            return treeMaker.at(pos).NewArray(basicType, dims, null);
        }
    }

    private int unoptag(Token token) {
        switch (token) {
            case PLUS:
                return JCTree.POS;
            case SUB:
                return JCTree.NEG;
            case BANG:
                return JCTree.NOT;
            case TILDE:
                return JCTree.COMPL;
            case PLUSPLUS:
                return JCTree.PREINC;
            case SUBSUB:
                return JCTree.PREDEC;
            default:
                return -1;
        }
    }

    /**
     * 解析基本数据类型
     * @param prefix
     * @return
     */
    private JCTree.JCExpression literal(Name prefix) {
        int pos = lexer.pos();
        JCTree.JCExpression t = errorTree;
        switch (lexer.token()) {
            case INTLITERAL:
                t =  treeMaker.at(pos).Literal(TypeTags.INT, Convert.string2int(strval(prefix), lexer.radix()));
                break;
            case LONGLITERAL:
                t = treeMaker.at(pos).Literal(TypeTags.LONG, new Long(Convert.string2int(strval(prefix), lexer.radix())));
                break;
            case FLOATLITERAL: {
                String proper = lexer.radix() == 16 ? ("0x" + lexer.stringVal()) : lexer.stringVal();
                Float n = Float.valueOf(proper);
                t = treeMaker.at(pos).Literal(TypeTags.FLOAT, n);
                break;
            }
            case DOUBLELITERAL: {
                String proper = lexer.radix() == 16 ? ("0x" + lexer.stringVal()) : lexer.stringVal();
                Float n = Float.valueOf(proper);
                t = treeMaker.at(pos).Literal(TypeTags.DOUBLE, n);
                break;
            }
            case CHARLITERAL:
                t = treeMaker.at(pos).Literal(TypeTags.CHAR, lexer.stringVal().charAt(0) + 0);
                break;
            case STRINGLITERAL:
                t = treeMaker.at(pos).Literal(TypeTags.CLASS, lexer.stringVal());
                break;
            case TRUE: case FALSE:
                t = treeMaker.at(pos).Literal(TypeTags.BOOLEAN, (lexer.token() == Token.TRUE ? 1 : 0));
                break;
            case NULL:
                t = treeMaker.at(pos).Literal(TypeTags.BOT, null);
                break;
            default:
        }
        lexer.nextToken();
        return t;
    }

    private String strval(Name prefix) {
        String s = lexer.stringVal();
        return prefix.isEmpty() ? s : prefix + s;
    }

    private JCTree.JCExpression basicType() {
        JCTree.JCPrimitiveType t = treeMaker.at(lexer.pos()).TypeIdent(typetag(lexer.token()));
        lexer.nextToken();
        return t;
    }

    static int typetag(Token token) {
        switch (token) {
            case BYTE:
                return TypeTags.BYTE;
            case CHAR:
                return TypeTags.CHAR;
            case SHORT:
                return TypeTags.SHORT;
            case INT:
                return TypeTags.INT;
            case LONG:
                return TypeTags.LONG;
            case FLOAT:
                return TypeTags.FLOAT;
            case DOUBLE:
                return TypeTags.DOUBLE;
            case BOOLEAN:
                return TypeTags.BOOLEAN;
            default:
                return -1;
        }
    }

    private JCTree.JCExpression bracketShuffix(JCTree.JCExpression bracketsOpt) {
        return bracketsOpt;
    }

    /** BracketsOpt = {"[" "]"}
     */
    private JCTree.JCExpression bracketsOpt(JCTree.JCExpression t) {
        // 如果是[则解析数组
        if (lexer.token() == Token.LBRACKET) {
            int pos = lexer.pos();
            lexer.nextToken();
            // 解析
            t = bracketsOptCont(t, pos);
            treeMaker.at(pos);
        }
        return t;
    }

    private JCTree.JCArrayTypeTree bracketsOptCont(JCTree.JCExpression t, int pos) {
        accept(Token.RBRACKET);
        // 继续解析多维数组
        t = bracketsOpt(t);
        return treeMaker.at(pos).TypeArray(t);
    }

    /**
     * 修改修饰符
     * @param parital
     * @return
     */
    JCTree.JCModifiers modifiersOpt(JCTree.JCModifiers parital) {

            long flags;
            int pos;
            if (parital == null) {
                flags = 0;
                pos = lexer.pos();
            } else {
                flags = parital.flags;
                pos = parital.pos;
            }

            List<JCTree.JCAnnotation> annotations = new ArrayList<>();
        loop:
            while (true) {
                long flag;
                switch (lexer.token()) {
                    case PRIVATE     : flag = Flags.PRIVATE; break;
                    case PROTECTED   : flag = Flags.PROTECTED; break;
                    case PUBLIC      : flag = Flags.PUBLIC; break;
                    case STATIC      : flag = Flags.STATIC; break;
                    case TRANSIENT   : flag = Flags.TRANSIENT; break;
                    case FINAL       : flag = Flags.FINAL; break;
                    case ABSTRACT    : flag = Flags.ABSTRACT; break;
                    case NATIVE      : flag = Flags.NATIVE; break;
                    case VOLATILE    : flag = Flags.VOLATILE; break;
                    case SYNCHRONIZED: flag = Flags.SYNCHRONIZED; break;
                    case STRICTFP    : flag = Flags.STRICTFP; break;
                    case MONKEYS_AT  : flag = Flags.ANNOTATION; break;
                    default: break loop;
                }
                lexer.nextToken();
                flags |= flag;
            }
            switch (lexer.token()) {
                case ENUM: flags |= Flags.ENUM; break;
                case INTERFACE: flags |= Flags.INTERFACE; break;
                default:
                    break;
            }
            JCTree.JCModifiers mods = treeMaker.at(pos).Modifiers(flags, annotations);
            return mods;
    }

    /** ImportDeclaration = IMPORT [ STATIC ] Ident { "." Ident } [ "." "*" ] ";"
     * 导入声明
     */
    private JCTree importDeclaration() {
        int pos = lexer.pos();
        lexer.nextToken();
        boolean importStatic = false;
        // 静态的
        if (lexer.token() == Token.STATIC) {
            importStatic = true;
            lexer.nextToken();
        }
        JCTree.JCExpression pid = treeMaker.at(lexer.pos()).Ident(ident());
        do {
            int pos1 = lexer.pos();
            accept(Token.DOT);
            if (lexer.token() == Token.STAR) {
                // "." "*"
                pid = treeMaker.at(pos1).Select(pid, names.asterisk);
                break;
            } else {
                // "." Ident
                pid = treeMaker.at(pos1).Select(pid, ident());
            }
        } while (lexer.token() == Token.DOT);
        accept(Token.SEMI);
        return treeMaker.at(pos).Import(pid, importStatic);
    }



    /**
     *
     */
    private JCTree.JCExpression qualident() {
        TreeMaker at = treeMaker.at(lexer.pos());
        Name ident = ident();
        JCTree.JCExpression i = at.Ident(ident);
        // .
        while (lexer.token() == Token.DOT) {
            int pos = lexer.pos();
            lexer.nextToken();
            i = treeMaker.at(pos).Select(i, ident());
        }
        return i;
    }

    Name ident() {
        if (lexer.token() == Token.IDENTIFIER) {
            Name name = lexer.name();
            lexer.nextToken();
            return name;
        } else {
            accept(Token.IDENTIFIER);
            return names.error;
        }
    }

    public void accept(Token token) {
        if (lexer.token() == token) {
            lexer.nextToken();
        } else {

        }
    }
}
