package node;
import java.io.IOException;
import lexer.LexerType;
import parser.Parser;

public class PrimaryExpNode extends Node{
    // PrimaryExp → '(' Exp ')' | LVal | Number
    public final String name="<PrimaryExp>";
    private ExpNode expNode;
    private LValNode lValNode;
    private NumberNode numberNode;
    public PrimaryExpNode(ExpNode expNode) {
        this.expNode = expNode;
    }

    public PrimaryExpNode(LValNode lValNode) {
        this.lValNode = lValNode;
    }

    public PrimaryExpNode(NumberNode numberNode) {
        this.numberNode = numberNode;
    }
    public LValNode getlValNode() {
        return lValNode;
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    public NumberNode getNumberNode() {
        return numberNode;
    }

    @Override
    public void show() throws IOException{
        if(expNode!=null){
            Parser.parseWriter.write("LPARENT (\n");
            expNode.show();
            Parser.parseWriter.write("RPARENT )\n");
    }
        else if(lValNode!=null){
            lValNode.show();
        }
        else if(numberNode!=null){
            numberNode.show();
        }
        Parser.parseWriter.write(name+"\n");
    }
    public int calculate(){
        if(expNode!=null){
            return expNode.getAddExpNode().calculate();
        }
        else if(lValNode!=null){
            return lValNode.calculate();
        }
        else{
            return numberNode.calculate();
        }
    }
    public boolean canCalculate(){
        if(expNode!=null){
            return expNode.canCalculate();
        }
        else if(lValNode!=null){
            return lValNode.canCalculate();
        }
        else{
            return true;
        }
    }
}
