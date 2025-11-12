package node;

import java.io.IOException;
import lexer.LexerType;
import parser.Parser;

public class FuncTypeNode extends Node{
    // FuncType → 'void' | 'int'
    public final String name="<FuncType>";
    private final String type;
    public FuncTypeNode(String type){
        this.type = type;
    }
    public String getType() {
        return type;
    }
    @Override
    public void show() throws IOException{
        if("int".equals(type))
        Parser.parseWriter.write("INTTK int\n");
        else if("void".equals(type))
        Parser.parseWriter.write("VOIDTK void\n");

        Parser.parseWriter.write(name + "\n");
    }
}
