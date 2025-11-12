package lexer;
public class Token{
    private LexerType type;
    private String value;
    private int linenum;
    private int num;

    public Token(){}
    public Token(LexerType type, String value, int linenum, int num){
        this.type = type;
        this.value = value;
        this.linenum = linenum;
        this.num = num;
    }
    //token构造方法重载
    public Token(LexerType type, String value, int linenum){
        this.type = type;
        this.value = value;
        this.linenum = linenum;
    }

    public LexerType getType() {
        return type;
    }
    public String getValue() {
        return value;
    }
    public int getLinenum() {
        return linenum;
    }
    public int getNum() {
        return num;
    }
}