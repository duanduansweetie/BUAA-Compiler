package node;
import java.io.IOException;
import lexer.LexerType;
import parser.Parser;
public class FuncFParamNode extends Node{
    // FuncFParam → BType Ident ['[' ']']
    public final String name="<FuncFParam>";
    private final BTypeNode bTypeNode;
    private final String identNode;
    private int lineno;
    public boolean isArray=false;
    public FuncFParamNode(BTypeNode bTypeNode, String identNode, boolean isArray) {
        this.bTypeNode = bTypeNode;
        this.identNode = identNode;
        this.isArray = isArray;
    }
    public FuncFParamNode(BTypeNode bTypeNode, String identNode){
        this.bTypeNode = bTypeNode;
        this.identNode = identNode;
    }
    public String getIdentNode() {
        return identNode;
    }

    public BTypeNode getbTypeNode() {
        return bTypeNode;
    }

    public int getLineno() {
        return lineno;
    }

    public void setLineno(int lineno) {
        this.lineno = lineno;
    }
@Override
    public void show() throws IOException{
        try {
            bTypeNode.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Parser.parseWriter.write("IDENFR "+identNode+"\n");
        if(isArray){
            Parser.parseWriter.write("LBRACK [\n");
            Parser.parseWriter.write("RBRACK ]\n");
        }
        Parser.parseWriter.write(name+"\n");
}

}
