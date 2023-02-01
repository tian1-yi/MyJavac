package com.myj.tools.javac.tree;

public interface Tree {

    public enum Kind {

        COMPILATION_UNIT(CompilationUnitTree.class),

        IMPORT(ImportTree.class),

        ANNOTAION(AnnotaionTree.class),

        MODIFIERS(ModifiersTree.class),

        FIELDACCESS(FieldAccessTree.class),

        TYPE_PARAMETER(TypeParameterTree.class),

        BLOCK(BlockTree.class),

        PRIMITIVETYPE(PrimitiveTypeTree.class),

        VARIABLE(VariableTree.class),

        ArrayType(ArrayTypeTree.class),

        NEWARRAY(NewArrayTree.class),

        ERRONEOUS(ErroneousTree.class),

        LITERAL(LiteralTree.class),

        EXPRESSION_STATEMENT(ExpressionStatementTree.class),

        UNARY(UnaryTree.class),

        NEW_CLASS(NewClassTree.class),

        ASSIGN(AssignTree.class),

        POSTFIX_INCREMENT(UnaryTree.class),

        METHOD_INVOCATION(MethodInvocationTree.class),

        METHOD(MethodTree.class),

        /**
         * Used for instances of {@link UnaryTree} representing postfix
         * decrement operator {@code --}.
         */
        POSTFIX_DECREMENT(UnaryTree.class),

        /**
         * Used for instances of {@link UnaryTree} representing prefix
         * increment operator {@code ++}.
         */
        PREFIX_INCREMENT(UnaryTree.class),

        /**
         * Used for instances of {@link UnaryTree} representing prefix
         * decrement operator {@code --}.
         */
        PREFIX_DECREMENT(UnaryTree.class),

        /**
         * Used for instances of {@link UnaryTree} representing unary plus
         * operator {@code +}.
         */
        UNARY_PLUS(UnaryTree.class),

        /**
         * Used for instances of {@link UnaryTree} representing unary minus
         * operator {@code -}.
         */
        UNARY_MINUS(UnaryTree.class),

        /**
         * Used for instances of {@link UnaryTree} representing bitwise
         * complement operator {@code ~}.
         */
        BITWISE_COMPLEMENT(UnaryTree.class),

        /**
         * Used for instances of {@link UnaryTree} representing logical
         * complement operator {@code !}.
         */
        LOGICAL_COMPLEMENT(UnaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * multiplication {@code *}.
         */
        MULTIPLY(BinaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * division {@code /}.
         */
        DIVIDE(BinaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * remainder {@code %}.
         */
        REMAINDER(BinaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * addition or string concatenation {@code +}.
         */
        PLUS(BinaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * subtraction {@code -}.
         */
        MINUS(BinaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * left shift {@code <<}.
         */
        LEFT_SHIFT(BinaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * right shift {@code >>}.
         */
        RIGHT_SHIFT(BinaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * unsigned right shift {@code >>>}.
         */
        UNSIGNED_RIGHT_SHIFT(BinaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * less-than {@code <}.
         */
        LESS_THAN(BinaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * greater-than {@code >}.
         */
        GREATER_THAN(BinaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * less-than-equal {@code <=}.
         */
        LESS_THAN_EQUAL(BinaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * greater-than-equal {@code >=}.
         */
        GREATER_THAN_EQUAL(BinaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * equal-to {@code ==}.
         */
        EQUAL_TO(BinaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * not-equal-to {@code !=}.
         */
        NOT_EQUAL_TO(BinaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * bitwise and logical "and" {@code &}.
         */
        AND(BinaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * bitwise and logical "xor" {@code ^}.
         */
        XOR(BinaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * bitwise and logical "or" {@code |}.
         */
        OR(BinaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * conditional-and {@code &&}.
         */
        CONDITIONAL_AND(BinaryTree.class),

        /**
         * Used for instances of {@link BinaryTree} representing
         * conditional-or {@code ||}.
         */
        CONDITIONAL_OR(BinaryTree.class),

        MULTIPLY_ASSIGNMENT(CompoundAssignmentTree.class),

        /**
         * Used for instances of {@link CompoundAssignmentTree} representing
         * division assignment {@code /=}.
         */
        DIVIDE_ASSIGNMENT(CompoundAssignmentTree.class),

        /**
         * Used for instances of {@link CompoundAssignmentTree} representing
         * remainder assignment {@code %=}.
         */
        REMAINDER_ASSIGNMENT(CompoundAssignmentTree.class),

        /**
         * Used for instances of {@link CompoundAssignmentTree} representing
         * addition or string concatenation assignment {@code +=}.
         */
        PLUS_ASSIGNMENT(CompoundAssignmentTree.class),

        /**
         * Used for instances of {@link CompoundAssignmentTree} representing
         * subtraction assignment {@code -=}.
         */
        MINUS_ASSIGNMENT(CompoundAssignmentTree.class),

        /**
         * Used for instances of {@link CompoundAssignmentTree} representing
         * left shift assignment {@code <<=}.
         */
        LEFT_SHIFT_ASSIGNMENT(CompoundAssignmentTree.class),

        /**
         * Used for instances of {@link CompoundAssignmentTree} representing
         * right shift assignment {@code >>=}.
         */
        RIGHT_SHIFT_ASSIGNMENT(CompoundAssignmentTree.class),

        /**
         * Used for instances of {@link CompoundAssignmentTree} representing
         * unsigned right shift assignment {@code >>>=}.
         */
        UNSIGNED_RIGHT_SHIFT_ASSIGNMENT(CompoundAssignmentTree.class),

        /**
         * Used for instances of {@link CompoundAssignmentTree} representing
         * bitwise and logical "and" assignment {@code &=}.
         */
        AND_ASSIGNMENT(CompoundAssignmentTree.class),

        /**
         * Used for instances of {@link CompoundAssignmentTree} representing
         * bitwise and logical "xor" assignment {@code ^=}.
         */
        XOR_ASSIGNMENT(CompoundAssignmentTree.class),

        /**
         * Used for instances of {@link CompoundAssignmentTree} representing
         * bitwise and logical "or" assignment {@code |=}.
         */
        OR_ASSIGNMENT(CompoundAssignmentTree.class),

        OTHER(null),

        IDENTIFIER(IdentifierTree.class), RETURN(ReturnTree.class);



        Kind(Class<? extends Tree> inf) {
            associateInterface = inf;
        }

        public Class<? extends  Tree> asInterface() {return associateInterface; }
        private final Class<? extends Tree> associateInterface;

    }

    Kind getKind();
}
