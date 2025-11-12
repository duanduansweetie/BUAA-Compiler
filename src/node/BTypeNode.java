package node;
import lexer.LexerType;
import parser.Parser;

import java.io.IOException;
public class BTypeNode extends Node{
    //BType → 'int'
    public final String name="<BType>";
    private final String type;
    public BTypeNode(String type){
        this.type = type;
    }
    public String getType() {
        return type;
    }
    @Override
    public void show() throws IOException{
        if("int".equals(type)){
            Parser.parseWriter.write("INTTK int\n");
        }
    }
}
