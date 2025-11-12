package node;
import lexer.LexerType;
import java.io.IOException;
import parser.Parser;
public class UnaryOpNode extends Node{
    // UnaryOp → '+' | '−' | '!'
    public final String name="<UnaryOp>";
    public final String op;
    public UnaryOpNode(String op){
        this.op = op;
    }
    public String getOp() {
        return op;
    }
    @Override
    public void show() throws IOException{
        if(op=="+"){
            Parser.parseWriter.write(LexerType.PLUS.name()+" +\n");
        }else if(op=="-"){
            Parser.parseWriter.write(LexerType.MINU.name()+" -\n");
        }else if(op=="!"){
            Parser.parseWriter.write(LexerType.NOT.name()+" !\n");
        }
        else if(op=="++"){
            Parser.parseWriter.write(LexerType.MULPLUS.name()+" ++\n");
        }
        Parser.parseWriter.write(name + "\n");
    }

}
