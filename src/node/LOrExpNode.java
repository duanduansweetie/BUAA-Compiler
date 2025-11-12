package node;

import lexer.LexerType;
import parser.Parser;

import java.io.IOException;
import java.util.List;

public class LOrExpNode extends Node {
    public final String name = "<LOrExp>";
    // LOrExp → LAndExp | LOrExp '||' LAndExp
    // 改写文法
    // // LOrExp → LAndExp { '||' LAndExp }
    private final List<LAndExpNode> lAndExpNodeList;

    public LOrExpNode(List<LAndExpNode> lAndExpNodeList) {
        this.lAndExpNodeList = lAndExpNodeList;
    }

    public List<LAndExpNode> getlAndExpNodeList() {
        return lAndExpNodeList;
    }

    @Override
    public void show() throws IOException {
        for (int i = 0; i < lAndExpNodeList.size(); i++) {
            if (i != 0) {
                Parser.parseWriter.write(LexerType.OR.name() + " " + "||" + "\n");
            }

            lAndExpNodeList.get(i).show();
            Parser.parseWriter.write(name + "\n");
        }
    }

}
