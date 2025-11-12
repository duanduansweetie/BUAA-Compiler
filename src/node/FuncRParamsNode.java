package node;
import lexer.LexerType;
import parser.Parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class FuncRParamsNode extends Node{
    // FuncRParams → Exp { ',' Exp }
    public final String name="<FuncRParams>";
    private List<ExpNode> expNodes=new ArrayList<>();
    public FuncRParamsNode(List<ExpNode> expNodes){
        this.expNodes = expNodes;
    }
    public List<ExpNode> getExpNodes() {
        return expNodes;
    }
    @Override
    public void show() throws IOException{
        for(int i=0;i<expNodes.size();i++){
            expNodes.get(i).show();
            if(i!=expNodes.size()-1){
                Parser.parseWriter.write("COMMA ,\n");
            }
        }
        Parser.parseWriter.write(name+"\n");
    }
}
