package node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import parser.Parser;
public class CompUnitNode extends Node{
    // CompUnit → {Decl} {FuncDef} MainFuncDef
    public final String name = "<CompUnit>";
    private List<DeclNode> declNodes = new ArrayList<>();
    private List<FuncDefNode> funcDefNodes = new ArrayList<>();
    private MainFuncDefNode mainFuncDefNode;
    public CompUnitNode(List<DeclNode> declNodes, List<FuncDefNode> funcDefNodes, MainFuncDefNode mainFuncDefNode) {
        this.declNodes = declNodes;
        this.funcDefNodes = funcDefNodes;
        this.mainFuncDefNode = mainFuncDefNode;
    }
    @Override
    public void show() throws IOException {
        for(DeclNode declNode:declNodes){
            declNode.show();
        }
        for(FuncDefNode funcDefNode:funcDefNodes){
            funcDefNode.show();
        }
        if (mainFuncDefNode != null)
            mainFuncDefNode.show();
        Parser.parseWriter.write(name+"\n");
    }
}
