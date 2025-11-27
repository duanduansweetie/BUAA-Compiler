package node;

import java.io.IOException;
import java.util.List;
import lexer.LexerType;
import parser.Parser;

public class AddExpNode extends Node {
    public final String name = "<AddExp>";
    // AddExp → MulExp | AddExp ('+' | '-') MulExp
    // 改写文法
    // AddExp → MulExp { ('+' | '-') MulExp}
    private final List<MulExpNode> mulExpNodeList;
    private final List<String> addOpList;

    public AddExpNode(List<MulExpNode> mulExpNodeList, List<String> addOpList) {
        this.mulExpNodeList = mulExpNodeList;
        this.addOpList = addOpList;
    }

    public List<String> getAddOpList() {
        return addOpList;
    }

    public List<MulExpNode> getMulExpNodeList() {
        return mulExpNodeList;
    }

    @Override
    public void show() throws IOException {
        for (int i = 0; i < mulExpNodeList.size(); i++) {
            if (i < addOpList.size() + 1 && i != 0) {
                if (addOpList.get(i - 1).equals("+"))
                    Parser.parseWriter.write(LexerType.PLUS.name() + " " + "+" + "\n");
                else
                    Parser.parseWriter.write(LexerType.MINU.name() + " " + "-" + "\n");
            }

            mulExpNodeList.get(i).show();
            Parser.parseWriter.write(name + "\n");
        }
    }
    public int calculate(){
        int result=mulExpNodeList.get(0).calculate();
        for(int i=1;i<mulExpNodeList.size();i++){
            if(addOpList.get(i-1).equals("+")){
                result+=mulExpNodeList.get(i).calculate();
            }else{
                result-=mulExpNodeList.get(i).calculate();
            }
        }
        return result;
    }
public boolean canCalculate() {
    for (MulExpNode mulExpNode : mulExpNodeList) {
        if (!mulExpNode.canCalculate()) {
            return false;
        }
    }
    return true;
}
}
