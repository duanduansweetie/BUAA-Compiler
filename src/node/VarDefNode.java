package node;
import java.io.IOException;
import java.util.List;
import lexer.LexerType;
import parser.Parser;
public class VarDefNode extends Node{
    // VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
    public final String name="<VarDef>";
    private final String identNode;
    private int lineno;
    private final ConstExpNode constExpNode;
    private InitValNode initValNode;
    private boolean isGetint=false;
    public VarDefNode(String identNode, ConstExpNode constExpNode, InitValNode initValNode){
        this.identNode = identNode;
        this.constExpNode = constExpNode;
        this.initValNode = initValNode;
    }
    public VarDefNode(String identNode, ConstExpNode constExpNode){
        this.identNode = identNode;
        this.constExpNode = constExpNode;
    }
    //getint那种
    public VarDefNode(String identNode){
        this.identNode = identNode;
        this.constExpNode = null;
        this.isGetint=true;

    }
    public void setLineno(int lineno){
        this.lineno = lineno;
    }
    public int getLineno(){
        return lineno;
    }
    public InitValNode getInitValNode() {
        return initValNode;
    }
    public String getIdentNode() {
        return identNode;
    }
    public ConstExpNode getConstExpNode() {
        return constExpNode;
    }
    public boolean isArray(){
        return constExpNode!=null;
    }
    @Override
    public void show() throws IOException{
        Parser.parseWriter.write(LexerType.IDENFR.name()+" "+identNode+"\n");
        if(constExpNode!=null){
            Parser.parseWriter.write(LexerType.LBRACK.name()+" [\n");
            try {
                constExpNode.show();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Parser.parseWriter.write(LexerType.RBRACK.name()+" ]\n");
        }
        if(initValNode!=null){
            Parser.parseWriter.write(LexerType.ASSIGN.name()+" =\n");
            try {
                initValNode.show();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    if(isGetint){
        Parser.parseWriter.write(LexerType.ASSIGN.name()+" =\n");
        Parser.parseWriter.write("GETINT getint\n");
        Parser.parseWriter.write("LPARENT (\n");
        Parser.parseWriter.write("RPARENT )\n");
    }
    Parser.parseWriter.write(name + "\n");
    }
}
