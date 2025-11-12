package node;
import lexer.LexerType;
import parser.Parser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConstDeclNode extends Node{
    // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    public final String name="<ConstDecl>";
    //private final String constKeyword;
    private final BTypeNode bTypeNode;
    private final List<ConstDefNode> constDefNodeList;
    public ConstDeclNode(BTypeNode bTypeNode, List<ConstDefNode> constDefNodeList){
        //this.constKeyword = constKeyword;
        this.bTypeNode = bTypeNode;
        this.constDefNodeList = constDefNodeList;
    }
//    public String getConstKeyword() {
//        return constKeyword;
//    }
    public BTypeNode getBTypeNode() {
        return bTypeNode;
    }
    public List<ConstDefNode> getConstDefNodeList() {
        return constDefNodeList;
    }
    @Override
    public void show() throws IOException{
        Parser.parseWriter.write(LexerType.CONSTTK.name()+" const\n");
        bTypeNode.show();
        for(int i=0;i<constDefNodeList.size();i++){
            if(i==0)
                constDefNodeList.get(i).show();
            else{
                Parser.parseWriter.write(LexerType.COMMA.name()+" ,\n");
                constDefNodeList.get(i).show();
            }
        }
        Parser.parseWriter.write("SEMICN ;\n");
        Parser.parseWriter.write(name+"\n");
    }
}
