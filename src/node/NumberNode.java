package node;
import java.io.IOException;
import lexer.LexerType;
import parser.Parser;
public class NumberNode extends Node{
    // Number → IntConst
    public final String name="<Number>";
    private final String intConstStr;
    public NumberNode(String intConstStr){
        this.intConstStr = intConstStr;
    }
    public String getIntConstStr() {
        return intConstStr;
    }
    @Override
    public void show() throws IOException{
        Parser.parseWriter.write(LexerType.INTCON.name()+" "+intConstStr+"\n");
        Parser.parseWriter.write(name+"\n");
    }
    public int calculate(){
        return Integer.parseInt(intConstStr);
    }
}
