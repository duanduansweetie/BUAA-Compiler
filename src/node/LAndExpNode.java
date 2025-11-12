package node;

import lexer.LexerType;
import parser.Parser;

import java.io.IOException;
import java.util.List;

public class LAndExpNode extends Node {
    public final String name = "<LAndExp>";
    // LAndExp → EqExp | LAndExp '&&' EqExp
    // 改写文法
    // LAndExp → EqExp {'&&' EqExp}
    private List<EqExpNode> eqExpNodeList;

    public LAndExpNode() {
    }

    public LAndExpNode(List<EqExpNode> eqExpNodeList) {
        this.eqExpNodeList = eqExpNodeList;
    }

    public List<EqExpNode> getEqExpNodeList() {
        return eqExpNodeList;
    }

    @Override
    public void show() throws IOException {
        for (int i = 0; i < eqExpNodeList.size(); i++) {
            if (i != 0) {
                Parser.parseWriter.write(LexerType.AND.name() + " " + "&&" + "\n");
            }

            eqExpNodeList.get(i).show();
            Parser.parseWriter.write(name + "\n");
        }
    }

}
