package lexer;

public enum LexerType {
    IDENFR("Ident"),
    INTCON("IntConst"),
    STRCON("StringConst"),
    CONSTTK("const"),
    INTTK("int"),
    STATICTK("static"),
    BREAKTK("break"),
    CONTINUETK("continue"),
    IFTK("if"),
    MAINTK("main"),
    ELSETK("else"),
    NOT("!"),
    AND("&&"),
    OR("||"),
    FORTK("for"),
    RETURNTK("return"),
    VOIDTK("void"),
    PLUS("+"),
    MINU("-"),
    PRINTFTK("printf"),
    MULT("*"),
    DIV("/"),
    MOD("%"),
    LSS("<"),
    LEQ("<="),
    GRE(">"),
    GEQ(">="),
    EQL("=="),
    NEQ("!="),
    SEMICN(";"),
    COMMA(","),
    LPARENT("("),
    RPARENT(")"),
    LBRACK("["),
    RBRACK("]"),
    LBRACE("{"),
    RBRACE("}"),
    ASSIGN("="),
    ADDASSI("+="),
    MINUASSI("-="),
    MULTASSI("*="),
    DIVASSI("/="),
    COLON(":"),
    QUES("?"),
    MULPLUS("++"),
    BITAND("bitand");
    private final String value;

    LexerType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}