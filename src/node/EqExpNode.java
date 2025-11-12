package node;

import lexer.LexerType;
import parser.Parser;

import java.io.IOException;
import java.util.List;

public class EqExpNode extends Node {
    public final String name = "<EqExp>";
    // EqExp → RelExp | EqExp ('\=\=' | '!=') RelExp
    // 改写文法
    // EqExp → RelExp {('==' | '!=') RelExp}
    private final List<RelExpNode> relExpNodeList;
    private final List<String> eqOpList;

    public EqExpNode(List<RelExpNode> relExpNodeList, List<String> eqOpList) {
        this.relExpNodeList = relExpNodeList;
        this.eqOpList = eqOpList;
    }

    public List<String> getEqOpList() {
        return eqOpList;
    }

    public List<RelExpNode> getRelExpNodeList() {
        return relExpNodeList;
    }

    @Override
    public void show() throws IOException {
        for (int i = 0; i < relExpNodeList.size(); i++) {
            if (i < eqOpList.size() + 1 && i != 0) {
                if (eqOpList.get(i - 1).equals("==")) {
                    Parser.parseWriter.write(LexerType.EQL.name() + " " + "==" + "\n");
                } else {
                    Parser.parseWriter.write(LexerType.NEQ.name() + " " + "!=" + "\n");
                }
            }

            relExpNodeList.get(i).show();
            Parser.parseWriter.write(name + "\n");
        }
    }

}
