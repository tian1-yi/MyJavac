package com.myj.tools.javac.tree;

import com.myj.tools.javac.code.Symbol;
import com.myj.tools.javac.util.Context;
import com.myj.tools.javac.util.Name;
import com.myj.tools.javac.util.Names;

import java.util.List;

import static com.myj.tools.javac.tree.JCTree.*;

public class TreeInfo {

    protected static final Context.Key<TreeInfo> treeInfoKey =
            new Context.Key<TreeInfo>();

    public static TreeInfo instance(Context context) {
        TreeInfo instance = context.get(treeInfoKey);
        if (instance == null)
            instance = new TreeInfo(context);
        return instance;
    }

    private TreeInfo(Context context) {
        context.put(treeInfoKey, this);

        Names names = Names.instance(context);
        opname[JCTree.POS     - JCTree.POS] = names.fromString("+");
        opname[JCTree.NEG     - JCTree.POS] = names.hyphen;
        opname[JCTree.NOT     - JCTree.POS] = names.fromString("!");
        opname[JCTree.COMPL   - JCTree.POS] = names.fromString("~");
        opname[JCTree.PREINC  - JCTree.POS] = names.fromString("++");
        opname[JCTree.PREDEC  - JCTree.POS] = names.fromString("--");
        opname[JCTree.POSTINC - JCTree.POS] = names.fromString("++");
        opname[JCTree.POSTDEC - JCTree.POS] = names.fromString("--");
        opname[JCTree.NULLCHK - JCTree.POS] = names.fromString("<*nullchk*>");
        opname[JCTree.OR      - JCTree.POS] = names.fromString("||");
        opname[JCTree.AND     - JCTree.POS] = names.fromString("&&");
        opname[JCTree.EQ      - JCTree.POS] = names.fromString("==");
        opname[JCTree.NE      - JCTree.POS] = names.fromString("!=");
        opname[JCTree.LT      - JCTree.POS] = names.fromString("<");
        opname[JCTree.GT      - JCTree.POS] = names.fromString(">");
        opname[JCTree.LE      - JCTree.POS] = names.fromString("<=");
        opname[JCTree.GE      - JCTree.POS] = names.fromString(">=");
        opname[JCTree.BITOR   - JCTree.POS] = names.fromString("|");
        opname[JCTree.BITXOR  - JCTree.POS] = names.fromString("^");
        opname[JCTree.BITAND  - JCTree.POS] = names.fromString("&");
        opname[JCTree.SL      - JCTree.POS] = names.fromString("<<");
        opname[JCTree.SR      - JCTree.POS] = names.fromString(">>");
        opname[JCTree.USR     - JCTree.POS] = names.fromString(">>>");
        opname[JCTree.PLUS    - JCTree.POS] = names.fromString("+");
        opname[JCTree.MINUS   - JCTree.POS] = names.hyphen;
        opname[JCTree.MUL     - JCTree.POS] = names.asterisk;
        opname[JCTree.DIV     - JCTree.POS] = names.slash;
        opname[JCTree.MOD     - JCTree.POS] = names.fromString("%");
    }

    /** The names of all operators.
     */
    private Name[] opname = new Name[JCTree.MOD - JCTree.POS + 1];

    public static final int
            notExpression = -1,   // not an expression
            noPrec = 0,           // no enclosing expression
            assignPrec = 1,
            assignopPrec = 2,
            condPrec = 3,
            orPrec = 4,
            andPrec = 5,
            bitorPrec = 6,
            bitxorPrec = 7,
            bitandPrec = 8,
            eqPrec = 9,
            ordPrec = 10,
            shiftPrec = 11,
            addPrec = 12,
            mulPrec = 13,
            prefixPrec = 14,
            postfixPrec = 15,
            precCount = 16;

    public static int opPrec(int optag) {

        switch(optag) {
            case JCTree.POS:
            case JCTree.NEG:
            case JCTree.NOT:
            case JCTree.COMPL:
            case JCTree.PREINC:
            case JCTree.PREDEC: return prefixPrec;
            case JCTree.POSTINC:
            case JCTree.POSTDEC:
            case JCTree.NULLCHK: return postfixPrec;
            case JCTree.ASSIGN: return assignPrec;
            case JCTree.BITOR_ASG:
            case JCTree.BITXOR_ASG:
            case JCTree.BITAND_ASG:
            case JCTree.SL_ASG:
            case JCTree.SR_ASG:
            case JCTree.USR_ASG:
            case JCTree.PLUS_ASG:
            case JCTree.MINUS_ASG:
            case JCTree.MUL_ASG:
            case JCTree.DIV_ASG:
            case JCTree.MOD_ASG: return assignopPrec;
            case JCTree.OR: return orPrec;
            case JCTree.AND: return andPrec;
            case JCTree.EQ:
            case JCTree.NE: return eqPrec;
            case JCTree.LT:
            case JCTree.GT:
            case JCTree.LE:
            case JCTree.GE: return ordPrec;
            case JCTree.BITOR: return bitorPrec;
            case JCTree.BITXOR: return bitxorPrec;
            case JCTree.BITAND: return bitandPrec;
            case JCTree.SL:
            case JCTree.SR:
            case JCTree.USR: return shiftPrec;
            case JCTree.PLUS:
            case JCTree.MINUS: return addPrec;
            case JCTree.MUL:
            case JCTree.DIV:
            case JCTree.MOD: return mulPrec;
            case JCTree.TYPETEST: return ordPrec;
            default: throw new AssertionError();
        }
    }

    public static Tree.Kind tagToKind(int tag) {
        switch (tag) {
            // Postfix expressions
            case JCTree.POSTINC:           // _ ++
                return Tree.Kind.POSTFIX_INCREMENT;
            case JCTree.POSTDEC:           // _ --
                return Tree.Kind.POSTFIX_DECREMENT;

            // Unary operators
            case JCTree.PREINC:            // ++ _
                return Tree.Kind.PREFIX_INCREMENT;
            case JCTree.PREDEC:            // -- _
                return Tree.Kind.PREFIX_DECREMENT;
            case JCTree.POS:               // +
                return Tree.Kind.UNARY_PLUS;
            case JCTree.NEG:               // -
                return Tree.Kind.UNARY_MINUS;
            case JCTree.COMPL:             // ~
                return Tree.Kind.BITWISE_COMPLEMENT;
            case JCTree.NOT:               // !
                return Tree.Kind.LOGICAL_COMPLEMENT;

            // Binary operators

            // Multiplicative operators
            case JCTree.MUL:               // *
                return Tree.Kind.MULTIPLY;
            case JCTree.DIV:               // /
                return Tree.Kind.DIVIDE;
            case JCTree.MOD:               // %
                return Tree.Kind.REMAINDER;

            // Additive operators
            case JCTree.PLUS:              // +
                return Tree.Kind.PLUS;
            case JCTree.MINUS:             // -
                return Tree.Kind.MINUS;

            // Shift operators
            case JCTree.SL:                // <<
                return Tree.Kind.LEFT_SHIFT;
            case JCTree.SR:                // >>
                return Tree.Kind.RIGHT_SHIFT;
            case JCTree.USR:               // >>>
                return Tree.Kind.UNSIGNED_RIGHT_SHIFT;

            // Relational operators
            case JCTree.LT:                // <
                return Tree.Kind.LESS_THAN;
            case JCTree.GT:                // >
                return Tree.Kind.GREATER_THAN;
            case JCTree.LE:                // <=
                return Tree.Kind.LESS_THAN_EQUAL;
            case JCTree.GE:                // >=
                return Tree.Kind.GREATER_THAN_EQUAL;

            // Equality operators
            case JCTree.EQ:                // ==
                return Tree.Kind.EQUAL_TO;
            case JCTree.NE:                // !=
                return Tree.Kind.NOT_EQUAL_TO;

            // Bitwise and logical operators
            case JCTree.BITAND:            // &
                return Tree.Kind.AND;
            case JCTree.BITXOR:            // ^
                return Tree.Kind.XOR;
            case JCTree.BITOR:             // |
                return Tree.Kind.OR;

            // Conditional operators
            case JCTree.AND:               // &&
                return Tree.Kind.CONDITIONAL_AND;
            case JCTree.OR:                // ||
                return Tree.Kind.CONDITIONAL_OR;

            // Assignment operators
            case JCTree.MUL_ASG:           // *=
                return Tree.Kind.MULTIPLY_ASSIGNMENT;
            case JCTree.DIV_ASG:           // /=
                return Tree.Kind.DIVIDE_ASSIGNMENT;
            case JCTree.MOD_ASG:           // %=
                return Tree.Kind.REMAINDER_ASSIGNMENT;
            case JCTree.PLUS_ASG:          // +=
                return Tree.Kind.PLUS_ASSIGNMENT;
            case JCTree.MINUS_ASG:         // -=
                return Tree.Kind.MINUS_ASSIGNMENT;
            case JCTree.SL_ASG:            // <<=
                return Tree.Kind.LEFT_SHIFT_ASSIGNMENT;
            case JCTree.SR_ASG:            // >>=
                return Tree.Kind.RIGHT_SHIFT_ASSIGNMENT;
            case JCTree.USR_ASG:           // >>>=
                return Tree.Kind.UNSIGNED_RIGHT_SHIFT_ASSIGNMENT;
            case JCTree.BITAND_ASG:        // &=
                return Tree.Kind.AND_ASSIGNMENT;
            case JCTree.BITXOR_ASG:        // ^=
                return Tree.Kind.XOR_ASSIGNMENT;
            case JCTree.BITOR_ASG:         // |=
                return Tree.Kind.OR_ASSIGNMENT;

            // Null check (implementation detail), for example, __.getClass()
            case JCTree.NULLCHK:
                return Tree.Kind.OTHER;

            default:
                return null;
        }
    }


    public static Name fullName(JCTree tree) {
        switch (tree.getTag()) {
            case IDENT:
                return ((JCTree.JCIdent) tree).name;
            case JCTree.SELECT:
                Name name = fullName(((JCTree.JCFieldAccess) tree).selected);
                return name == null ? null : name.append('.', name(tree));
            default:
                return null;
        }
    }

    public static Name name(JCTree tree) {
        switch (tree.getTag()) {
            case IDENT:
                return ((JCTree.JCIdent) tree).name;
            case SELECT:
                return ((JCTree.JCFieldAccess) tree).name;
            default:
                return null;
        }
    }

    public static boolean hasConstructors(List<JCTree> defs) {
        for (JCTree def : defs) {
            if (isConstructor(def)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isConstructor(JCTree def) {
        if (def.getTag() == METHODDEF) {
            Name name = ((JCMethodDecl) def).name;
            return name == name.table.names.init;
        } else {
            return false;
        }
    }

    public static void setSymbol(JCTree tree, Symbol sym) {
        switch (tree.getTag()) {
            case IDENT:
                ((JCIdent) tree).symbol = sym; break;
            case SELECT:
                ((JCFieldAccess) tree).sym = sym; break;
            default:
        }
    }

    public static Symbol symbol(JCTree tree) {
        switch (tree.getTag()) {
            case IDENT:
                return ((JCIdent) tree).symbol;
            case SELECT:
                return ((JCFieldAccess) tree).sym;
            default:
                return null;
        }
    }

    public Name operatorName(int optag) {
        return opname[optag - POS];
    }
}
