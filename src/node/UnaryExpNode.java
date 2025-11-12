package node;
import java.io.IOException;
import lexer.LexerType;
import parser.Parser;
public class UnaryExpNode extends Node{
    // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    private PrimaryExpNode primaryExpNode;
    public final String name = "<UnaryExp>";
    private String identNode;
    private FuncRParamsNode funcRParamsNode;
    private UnaryOpNode unaryOpNode;
    private UnaryExpNode unaryExpNode;
    public UnaryExpNode(PrimaryExpNode primaryExpNode) {
        this.primaryExpNode = primaryExpNode;
    }
    public UnaryExpNode(String identNode, FuncRParamsNode funcRParamsNode) {
        this.identNode = identNode;
        this.funcRParamsNode = funcRParamsNode;
    }
    public UnaryExpNode(UnaryOpNode unaryOpNode, UnaryExpNode unaryExpNode) {
        this.unaryOpNode = unaryOpNode;
        this.unaryExpNode = unaryExpNode;
    }
    public PrimaryExpNode getPrimaryExpNode() {
        return primaryExpNode;
    }
    public String getIdentNode() {
        return identNode;
    }
    public FuncRParamsNode getFuncRParamsNode() {
        return funcRParamsNode;
    }
    public UnaryOpNode getUnaryOpNode() {
        return unaryOpNode;
    }
    public UnaryExpNode getUnaryExpNode() {
        return unaryExpNode;
    }
    @Override
    public void show() throws IOException{
        if(primaryExpNode!=null){
            primaryExpNode.show();
        }else if(identNode!=null){
            Parser.parseWriter.write(LexerType.IDENFR.name()+" "+identNode+"\n");
            Parser.parseWriter.write("LPARENT (\n");
            if(funcRParamsNode!=null){
                funcRParamsNode.show();
            }
            Parser.parseWriter.write("RPARENT )\n");
        }else if(unaryOpNode!=null && unaryExpNode!=null){
            unaryOpNode.show();
            unaryExpNode.show();
        }
        Parser.parseWriter.write(name+"\n");
    }
    public int calculate(){
        if(primaryExpNode!=null){
            return primaryExpNode.calculate();
        }else if(unaryOpNode!=null){
            int res=unaryExpNode.calculate();
            if(unaryOpNode.getOp().equals("+")){
                return res;}
            else if(unaryOpNode.getOp().equals("-")) {
                return -res;
            }
            else if(unaryOpNode.getOp().equals("!")){
                return res==0?1:0;
            }
            else{
                throw new RuntimeException("Unknown unary operator: "+unaryOpNode.getOp());
            }
        }
        else{
            return  0;
        }
    }
    public boolean canCalculate(){
        if(primaryExpNode!=null){
            return primaryExpNode.canCalculate();
        }else if(unaryOpNode!=null){
            return unaryExpNode.canCalculate();
        }
        else{
            return false;
        }
    }

}
