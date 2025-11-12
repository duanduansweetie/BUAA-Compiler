package node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lexer.LexerType;
import parser.Parser;
public class FuncFParamsNode extends Node{
    // FuncFParams → FuncFParam { ',' FuncFParam }
    public final String name = "<FuncFParams>";
    private List<FuncFParamNode> funcFParamsNode = new ArrayList<>();
    public FuncFParamsNode(List<FuncFParamNode> funcFParamsNode){
        this.funcFParamsNode=funcFParamsNode;
    }
    public List<FuncFParamNode> getFuncFParamsNodes() {
        return funcFParamsNode;
    }
    @Override
    public void show() throws IOException{
        for(int i=0;i<funcFParamsNode.size();i++){
            funcFParamsNode.get(i).show();
            if(i!=funcFParamsNode.size()-1){
                Parser.parseWriter.write("COMMA ,\n");
            }

        }
        Parser.parseWriter.write(name+"\n");
    }

}