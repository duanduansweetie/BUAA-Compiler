package node;
import java.io.IOException;
import lexer.LexerType;
import parser.Parser;
public class ConstDefNode extends Node{
    //常量定义
    // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
    public final String name="<ConstDef>";
    private final String ident;
    private int lineno;
    private final ConstExpNode constExpNode;
    private final ConstInitValNode constInitValNode;
    public ConstDefNode(String ident, ConstExpNode constExpNode, ConstInitValNode constInitValNode){
        this.ident = ident;
        this.constExpNode = constExpNode;
        this.constInitValNode = constInitValNode;
    }
    public void setLineno(int lineno) {
        this.lineno = lineno;
    }
    public ConstExpNode getConstExpNode() {
        return constExpNode;
    }
    public ConstInitValNode getConstInitValNode() {
        return constInitValNode;
    }
    public String getIdent() {
        return ident;
    }
    public int getLineno(){
        return lineno;
    }
    @Override
    public void show() throws IOException{
        Parser.parseWriter.write("IDENFR "+ident+"\n");
        if(constExpNode!=null){
            Parser.parseWriter.write("LBRACK [\n");
            constExpNode.show();
            Parser.parseWriter.write("RBRACK ]\n");
        }
        Parser.parseWriter.write("ASSIGN =\n");
        constInitValNode.show();
        Parser.parseWriter.write(name+"\n");
    }
}
