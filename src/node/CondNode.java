package node;
import parser.Parser;
import java.io.IOException;
public class CondNode extends Node{
    // Cond → LOrExp
    public final String name="<Cond>";
    private final LOrExpNode lOrExpNode;
    public CondNode(LOrExpNode lOrExpNode){
        this.lOrExpNode = lOrExpNode;
    }
    public LOrExpNode getlOrExpNode() {
        return lOrExpNode;
    }
    @Override
    public void show() throws IOException{
        try {
            lOrExpNode.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Parser.parseWriter.write(name+"\n");
    }
}
