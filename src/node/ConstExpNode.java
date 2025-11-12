package node;
import parser.Parser;

import java.io.IOException;
public class ConstExpNode extends Node{
    // ConstExp → AddExp
    public final String name="<ConstExp>";
    private final AddExpNode addExpNode;
    public ConstExpNode(AddExpNode addExpNode){
        this.addExpNode = addExpNode;
    }
    public AddExpNode getAddExpNode() {
        return addExpNode;
    }
    @Override
    public void show() throws IOException {
        addExpNode.show();
        Parser.parseWriter.write(name + "\n");
    }
    public int calculate(){
        return addExpNode.calculate();
    }
}
