package lexer;
import error.ErrorType;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import error.ErrorManager;
import error.ErrorType;
public class LexerAnalyzer{
    private BufferedReader reader;
    private BufferedWriter writer;
    private String inputBuffer = "";
    private StringBuffer nowWord=new StringBuffer();
    private Token nowToken;
    private List<Token> tokenList=new ArrayList<>();
    private int nowLine=1;
    private char nowChar;
    private char nextChar;
    private int nowNum;
    private int index=-1;
    private final ErrorManager errorManager;

    public LexerAnalyzer(ErrorManager errorManager) {
        this.errorManager = errorManager;
    }
    private boolean hasError = false;
    public void setInput(String input) {

        this.reader = new BufferedReader(new StringReader(input)); // 使用 BufferedReader 包装 StringReader
    }
    public void init(String inputFilePath, String outputFilePath) throws IOException {
        reader = new BufferedReader(new FileReader(inputFilePath));
        writer = new BufferedWriter(new FileWriter(outputFilePath));
    }
    public void init(String inputFilePath) throws IOException {
        reader = new BufferedReader(new FileReader(inputFilePath));
    }
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
        if (writer != null) {
            writer.close();
        }
    }
    //读取操作
    public void getChar() throws IOException{
        if(inputBuffer.isEmpty()){
            StringBuffer sb=new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            inputBuffer = sb.toString();
        }
        index++;
        if(index<inputBuffer.length()){
            nowChar=inputBuffer.charAt(index);
            if (index + 1 < inputBuffer.length()) {
                nextChar = inputBuffer.charAt(index + 1);
            } else {
                nextChar = '\0';
            }
            if(nowChar=='\n'){
                nowLine++;
            }
        }
        else{
            nowChar = '\0';
            nextChar = '\0';
        }
    }
    //多读的回退
    public void retract(){
        if(index>0){
            index--;
            if(nowChar=='\n'){
                nowLine--;
            }
            nowChar = inputBuffer.charAt(index);
            if (index + 1 < inputBuffer.length()) {
                nextChar = inputBuffer.charAt(index + 1);
            } else {
                nextChar = '\0';
            }
        }
    }
    //为新的token做准备
    public void clearToken(){
        nowWord=new StringBuffer();
        nowNum=0;
    }
    //把当前字符加到字符串后
    public void addChar(){
        nowWord.append(nowChar);
    }
    //跳过无效字符
    public void skipSpace() throws IOException{
        while(nowChar==' '||nowChar=='\n'||nowChar=='\t'||nowChar=='\r'){
            getChar();
        }
    }

    //判断当前字符类型
    public enum CharType {
        SPACE, NEWLINE, TAB, LETTER, DIGIT, UNDERLINE, SINGLE_DELIM, DOUBLE_DELIM,
        ANNOTATION, SINGLE_QUOTE, DOUBLE_QUOTE, AND_OR, END, OTHER
    }
    public CharType getCharType(char c, char next) {
        if (c == ' ') return CharType.SPACE;
        if (c == '\n') return CharType.NEWLINE;
        if (c == '\t') return CharType.TAB;
        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) return CharType.LETTER;
        if (c >= '0' && c <= '9') return CharType.DIGIT;
        if (c == '_') return CharType.UNDERLINE;
        if ((c == '+'&& (next != '='&&next!='+'))|| (c == '-'&& next != '=' )|| c == '%' || (c == '*'&& next != '=' )|| c == ';' || c == ',' ||
                c == '(' || c == ')' || c == '[' || c == ']' || c == '{' || c == '}' ||c=='?'||c==':'||
                (c == '/' && next != '/' && next != '*'&& next != '=') ||
                (c == '<' && next != '=') || (c == '>' && next != '=') ||
                (c == '=' && next != '=') || (c == '!' && next != '=')) return CharType.SINGLE_DELIM;
        if (c == '<' || c == '>' || c == '=' || c == '!'||c=='+'||c=='-'||c=='*'||(c=='/'&&next=='=')) return CharType.DOUBLE_DELIM;
        if (c == '/') return CharType.ANNOTATION;
//        if (c == '\'') return CharType.SINGLE_QUOTE;
        if (c == '\"') return CharType.DOUBLE_QUOTE;
        if (c == '&' || c == '|') return CharType.AND_OR;
        if (c == '\0') return CharType.END;
        return CharType.OTHER;
    }
    //判断是不是关键字
    public int reserver(){
        String word=nowWord.toString();
        for(LexerType type:LexerType.values()){
            if(type.toString().equals(word)){
                return type.ordinal();
            }
        }
        return 0;
    }
    //识别一个token并且分发到相应的处理方法
    public Token getsym() throws IOException{
        getChar();
        clearToken();
        skipSpace();

        CharType charType = getCharType(nowChar, nextChar);
        switch(charType){
            case LETTER:
            case UNDERLINE:
                handleIdent();
                break;
            case DIGIT:
                handleNumber();
                break;
            case SINGLE_DELIM:
                handleSingleDelim();
                break;
            case DOUBLE_DELIM:
                handleDoubleDelim();
                break;
//            case SINGLE_QUOTE:
//                handleCharConst();
//                break;
            case DOUBLE_QUOTE:
                handleStringConst();
                break;
            case ANNOTATION:
                handleAnnotation();
                break;
            case AND_OR:
                handleAndorOr();
                break;
            case SPACE:
            case NEWLINE:
            case TAB:
                skipSpace();
                return getsym(); // 跳过空白后递归调用
            case END:
                return null;
            default:
                //throw new RuntimeException("Unknown character: " + nowChar);
        }
        return nowToken;
    }
    private boolean isLetter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    public List<Token> getTokenList(){
        return tokenList;
    }
    public void setTokenList(List<Token> tokenList) {
        this.tokenList = tokenList;
    }

    public void addToken(Token token) {
        tokenList.add(token);
    }
    private void handleIdent() throws IOException {
        while (isLetter(nowChar) || isDigit(nowChar) || nowChar == '_') {
            addChar();
            getChar();
        }
        retract();
        int con=reserver();
        if(con==0){//不是关键字
            nowToken=new Token(LexerType.IDENFR,nowWord.toString(),nowLine);
            addToken(nowToken);
        }
        else{//是关键字，和列表里面的匹配
            nowToken=new Token(LexerType.values()[con],nowWord.toString(),nowLine);
            addToken(nowToken);
        }
    }

    private void handleNumber() throws IOException {
        while (isDigit(nowChar)) {
            addChar();
            getChar();
            nowNum=nowNum*10+nowChar-'0';
        }
        retract();
        nowToken=new Token(LexerType.INTCON,nowWord.toString(),nowLine,nowNum);
        addToken(nowToken);
    }

    private void handleSingleDelim() throws IOException {
        addChar();
        switch (nowChar) {
            case '+':
                if (nextChar == '=') {
                    getChar();
                    addChar();
                    nowToken = new Token(LexerType.ADDASSI, nowWord.toString(), nowLine);
                }
                else if(nextChar=='+'){
                    getChar();
                    addChar();
                    nowToken = new Token(LexerType.MULPLUS, nowWord.toString(), nowLine);

                }
                else {
                    nowToken = new Token(LexerType.PLUS, nowWord.toString(), nowLine);
                }
                break;
            case '-':
                if (nextChar == '=') {
                    getChar();
                    addChar();
                    nowToken = new Token(LexerType.MINUASSI,nowWord.toString(), nowLine);
                } else {
                    nowToken = new Token(LexerType.MINU, nowWord.toString(), nowLine);
                }
                break;
            case '*':
                if (nextChar == '=') {
                    getChar();
                    addChar();
                    nowToken = new Token(LexerType.MULTASSI, nowWord.toString(), nowLine);
                } else {
                    nowToken = new Token(LexerType.MULT, nowWord.toString(), nowLine);
                }
                break;
            case '/':
                if (nextChar == '=') {
                    getChar();
                    addChar();
                    nowToken = new Token(LexerType.DIVASSI, nowWord.toString(), nowLine);
                } else {
                    nowToken = new Token(LexerType.DIV, nowWord.toString(), nowLine);
                }
                break;
            case '%':
                nowToken = new Token(LexerType.MOD, nowWord.toString(), nowLine);
                break;
            case ';':
                nowToken = new Token(LexerType.SEMICN, nowWord.toString(), nowLine);
                break;
            case ',':
                nowToken = new Token(LexerType.COMMA, nowWord.toString(), nowLine);
                break;
            case '(':
                nowToken = new Token(LexerType.LPARENT, nowWord.toString(), nowLine);
                break;
            case ')':
                nowToken = new Token(LexerType.RPARENT, nowWord.toString(), nowLine);
                break;
            case '[':
                nowToken = new Token(LexerType.LBRACK, nowWord.toString(), nowLine);
                break;
            case ']':
                nowToken = new Token(LexerType.RBRACK, nowWord.toString(), nowLine);
                break;
            case '{':
                nowToken = new Token(LexerType.LBRACE, nowWord.toString(), nowLine);
                break;
            case '}':
                nowToken = new Token(LexerType.RBRACE, nowWord.toString(), nowLine);
                break;
            case '=': // 单个等号
                nowToken = new Token(LexerType.ASSIGN, nowWord.toString(), nowLine);
                break;
            case '<':
                if (nextChar == '=') {
                    getChar();
                    addChar();
                    nowToken = new Token(LexerType.LEQ, nowWord.toString(), nowLine);
                } else {
                    nowToken = new Token(LexerType.LSS, nowWord.toString(), nowLine);
                }
                break;
            case '>':
                if (nextChar == '=') {
                    getChar();
                    addChar();
                    nowToken = new Token(LexerType.GEQ, nowWord.toString(), nowLine);
                } else {
                    nowToken = new Token(LexerType.GRE, nowWord.toString(), nowLine);
                }
                break;
            case '!':
                if (nextChar == '=') {
                    retract(); // 回退，交由 handleDoubleDelim 处理
                    return;
                } else {
                    nowToken = new Token(LexerType.NOT, nowWord.toString(), nowLine);
                }
                break;
            case '?':
                nowToken = new Token(LexerType.QUES, nowWord.toString(), nowLine);
                break;
            case ':':
                nowToken = new Token(LexerType.COLON, nowWord.toString(), nowLine);
                break;
            default:
                break;
        }
        addToken(nowToken);
    }
    private void handleDoubleDelim() throws IOException {
        addChar();
        getChar();
        if (nowWord.toString().equals("!")) { // 处理感叹号
            if (nowChar == '=') {
                addChar();
                nowToken = new Token(LexerType.NEQ, nowWord.toString(), nowLine); // 处理 !=
            } else {
                retract();
                nowToken = new Token(LexerType.NOT, nowWord.toString(), nowLine); // 处理单个 !
            }
        } else if (nowWord.toString().equals("=")) { // 处理等号
            if (nowChar == '=') {
                addChar();
                nowToken = new Token(LexerType.EQL, nowWord.toString(), nowLine); // 处理 ==
            } else {
                retract();
                nowToken = new Token(LexerType.ASSIGN, nowWord.toString(), nowLine); // 处理单个 =
            }
        } else if (nowWord.toString().equals("<")) { // 处理小于号
            if (nowChar == '=') {
                addChar();
                nowToken = new Token(LexerType.LEQ, nowWord.toString(), nowLine); // 处理 <=
            } else {
                retract();
                nowToken = new Token(LexerType.LSS, nowWord.toString(), nowLine); // 处理 <
            }
        } else if (nowWord.toString().equals(">")) { // 处理大于号
            if (nowChar == '=') {
                addChar();
                nowToken = new Token(LexerType.GEQ, nowWord.toString(), nowLine); // 处理 >=
            } else {
                retract();
                nowToken = new Token(LexerType.GRE, nowWord.toString(), nowLine); // 处理 >
            }
        }
        else if(nowWord.toString().equals("+")){
            if(nowChar=='='){
                addChar();
                nowToken=new Token(LexerType.ADDASSI,nowWord.toString(),nowLine);
            }
            else if(nowChar=='+'){
                addChar();
                nowToken=new Token(LexerType.MULPLUS,nowWord.toString(),nowLine);

            }
            else{
                retract();
                nowToken=new Token(LexerType.PLUS,nowWord.toString(),nowLine);
            }
        }
        else if(nowWord.toString().equals("-")){
            if(nowChar=='='){
                addChar();
                nowToken=new Token(LexerType.MINUASSI,nowWord.toString(),nowLine);
            }
            else{
                retract();
                nowToken=new Token(LexerType.MINU,nowWord.toString(),nowLine);
            }
        }
        else if(nowWord.toString().equals("*")){
            if(nowChar=='='){
                addChar();
                nowToken=new Token(LexerType.MULTASSI,nowWord.toString(),nowLine);
            }
            else{
                retract();
                nowToken=new Token(LexerType.MULT,nowWord.toString(),nowLine);
            }
        }
        else if(nowWord.toString().equals("/")){
            if(nowChar=='='){
                addChar();
                nowToken=new Token(LexerType.DIVASSI,nowWord.toString(),nowLine);
            }
            else{
                retract();
                nowToken=new Token(LexerType.DIV,nowWord.toString(),nowLine);
            }
        }
        addToken(nowToken);
    }

    private void handleStringConst() throws IOException{
        addChar();
        getChar();
        while(nowChar!='\"'){
            if(nowChar=='\\'){
                addChar();
                getChar();
                addChar();
            }
            else{
                addChar();
            }
            getChar();
        }
        addChar();
        nowToken=new Token(LexerType.STRCON,nowWord.toString(),nowLine);
        addToken(nowToken);
    }
    private void handleAnnotation() throws IOException {
        if (nextChar == '/') { // 单行注释
            getChar(); // 跳过 '/'
            while (nowChar!='\n' && nowChar != '\0') { // 跳过直到换行符或文件结束
                getChar();
            }
        } else if (nextChar == '*') { // 多行注释
            getChar(); // 跳过 '*'
            getChar();
            while (!(nowChar == '*' && nextChar == '/') && nowChar != '\0') { // 跳过直到检测到 */
                getChar();
            }
            if (nowChar == '*' && nextChar == '/') { // 跳过 */
                getChar();
                getChar();
            }
        }
        // 处理完注释后，返回一个空Token，继续解析
        nowToken = new Token();
    }
    private void handleAndorOr() throws IOException{
//        addChar();
//        getChar();
//        if(nowChar==nowWord.charAt(0)){
//            addChar();
//            if(nowWord.charAt(0)=='&'){
//                nowToken=new Token(LexerType.AND,nowWord.toString(),nowLine);
//            }
//            else{
//                nowToken=new Token(LexerType.OR,nowWord.toString(),nowLine);
//            }
//            addToken(nowToken);
//        }
//        else{
//            errorManager.addError(nowLine, ErrorType.A);
//            hasError=true;
//            retract();
//        }
        if((nowChar=='&'&&nextChar=='&')||(nowChar=='|'&&nextChar=='|')){
            addChar();
            getChar();

        }else{
            errorManager.addError(nowLine, ErrorType.A);
            hasError=true;
           addChar();
        }
        addChar();
        if(nowWord.charAt(0)=='&'){
            nowToken=new Token(LexerType.AND,nowWord.toString(),nowLine);
        }
        else{
            nowToken=new Token(LexerType.OR,nowWord.toString(),nowLine);
        }
        addToken(nowToken);
    }
    public void showLexer() throws IOException{
        if(hasError)
        {
            return;
        }
        for (Token token : tokenList) {
            writer.write(token.getType().name() + " " + token.getValue() + "\n");
        }
    }



}
