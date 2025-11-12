package node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lexer.LexerType;
import parser.Parser;
public class VarDeclNode extends Node{
    //  VarDecl → [ 'static' ] BType VarDef { ',' VarDef } ';' // i
    public final String name="<VarDecl>";
    private final boolean isStatic;
    private final BTypeNode bTypeNode;
    private List<VarDefNode> varDefNodeList = new ArrayList<>();
    public VarDeclNode(boolean isStatic,BTypeNode bTypeNode, List<VarDefNode> varDefNodeList){
        this.isStatic= isStatic;
        this.bTypeNode = bTypeNode;
        this.varDefNodeList=varDefNodeList;
    }
    public BTypeNode getbTypeNode() {
        return bTypeNode;
    }
    public List<VarDefNode> getVarDefNodeList() {
        return varDefNodeList;
    }
    @Override
    public void show() throws IOException{
        if (isStatic) {
            Parser.parseWriter.write("STATICTK " + "static" + "\n");
        }
        bTypeNode.show();
        for(int i=0;i<varDefNodeList.size();i++){
            varDefNodeList.get(i).show();
            if(i!=varDefNodeList.size()-1){
                Parser.parseWriter.write("COMMA ,\n");
            }
        }
        Parser.parseWriter.write(LexerType.SEMICN.name()+" ;\n");
        Parser.parseWriter.write(name+"\n");
    }
}
