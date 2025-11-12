package node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lexer.LexerType;
import parser.Parser;
public class ConstInitValNode extends Node{
    // ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
    public final String name="<ConstInitVal>";
    private final List<ConstExpNode> constExpNodes=new ArrayList<>();
    private ConstExpNode constExpNode;
    private String stringConst;
    public ConstInitValNode(ConstExpNode constExpNode){
        this.constExpNode = constExpNode;
    }
    public ConstInitValNode(List<ConstExpNode> constExpNodes){
        this.constExpNodes.addAll(constExpNodes);
    }
    public ConstInitValNode(String stringConst){
        this.stringConst = stringConst;
    }
    @Override
    public void show() throws IOException {
        if (constExpNode != null) {
            constExpNode.show();

        }
        else if (stringConst != null) {
            Parser.parseWriter.write( "STRCON " + stringConst + "\n");

        }
        else  {
            Parser.parseWriter.write("LBRACE {\n");
            for (int i = 0; i < constExpNodes.size(); i++) {
                constExpNodes.get(i).show();
                if (i != constExpNodes.size() - 1) {
                    Parser.parseWriter.write("COMMA ,\n");
                }
            }
            Parser.parseWriter.write( "RBRACE }\n");

        }
        Parser.parseWriter.write(name + "\n");
    }
    public int calculate(){
        if(constExpNode!=null){
            return constExpNode.calculate();
        }
        else{
            return constExpNodes.size();
        }
    }
    public List<Integer> calculateArray() {
        List<Integer> res = new ArrayList<>();
        for (ConstExpNode constExpNode : constExpNodes) {
            res.add(constExpNode.calculate());
        }
        return res;
    }
}
