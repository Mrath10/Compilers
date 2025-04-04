package parse;

/**
 * enumeration Token - Defines the basic tokens returned from the lexical analyzer.
 */
public enum Token {
    EOF("End-of-file"),
    PLUS("+"),
    MINUS("-"),
    TIMES("*"),
    DIVIDE("/"),
    LPAREN("("),
    RPAREN(")"),
    LBRACKET("["),
    RBRACKET("]"),
    SEMICOLON(";"),
    COLON(":"),
    ASSIGN(":="),
    COMMA(","),
    RANGE(".."),
    EQUALS("="),
    NEQUALS("!="),
    LEQUALS("<="),
    LESS("<"),
    GEQUALS(">="),
    GREATER(">"),
    BAR("|"),
    SEPARATOR("[]"),
    KW_BEGIN("begin"),
    KW_CALL("call"),
    KW_CONST("const"),
    KW_DO("do"),
    KW_ELSE("else"),
    KW_END("end"),
    KW_FI("fi"),
    KW_IF("if"),
    KW_IFE("ife"),
    KW_PROCEDURE("procedure"),
    KW_READ("read"),
    KW_THEN("then"),
    KW_TYPE("type"),
    KW_VAR("var"),
    KW_WHILE("while"),
    KW_WRITE("write"),
    IDENTIFIER("identifier"),
    NUMBER("number"),
    ILLEGAL("illegal");

    /**
     * The name of the token
     */
    private final String name;

    Token(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
