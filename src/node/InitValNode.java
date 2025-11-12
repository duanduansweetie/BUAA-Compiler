package node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lexer.LexerType;
import parser.Parser;

public class InitValNode extends Node{
    // InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
    public final String name="<InitVal>";
    private String stringConst;
    private ExpNode expNode;
    private List<ExpNode> expNodeList = new ArrayList<>();
    public InitValNode(List<ExpNode> expNodeList){
        this.expNodeList=expNodeList;
    }
    public InitValNode(ExpNode expNode){
        this.expNode=expNode;
    }
    public InitValNode(String stringConst){
        this.stringConst=stringConst;
    }
    public List<ExpNode> getExpNodeList() {
        return expNodeList;
    }
    public ExpNode getExpNode() {
        return expNode;
    }
    public String getStringConst() {
        return stringConst;
    }
    @Override
    public void show() throws IOException{
        if(stringConst!=null){
            Parser.parseWriter.write(LexerType.STRCON.name()+" "+stringConst+"\n");
        }
        else if(expNode!=null){
            expNode.show();
        }
        else{
            Parser.parseWriter.write(LexerType.LBRACE.name()+" {\n");
            for(int i=0;i<expNodeList.size();i++){
                expNodeList.get(i).show();
                if(i!=expNodeList.size()-1){
                    Parser.parseWriter.write(LexerType.COMMA.name() +" ,\n");
                }
            }
            Parser.parseWriter.write(LexerType.RBRACE.name()+" }\n");
        }
        Parser.parseWriter.write(name+"\n");
    }
}
