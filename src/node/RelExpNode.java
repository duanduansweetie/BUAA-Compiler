package node;

import lexer.LexerType;
import parser.Parser;

import java.io.IOException;
import java.util.List;

public class RelExpNode extends Node {
    public final String name = "<RelExp>";
    // RelExp → AddExp | RelExp ('<' | '<=' | '>' | '>=') AddExp
    // 改写文法
    // RelExp → AddExp {('<' | '<=' | '>' | '>=') AddExp}
    private final List<AddExpNode> addExpNodeList;
    private final List<String> relOpList;

    public RelExpNode(List<AddExpNode> addExpNodeList, List<String> relOpList) {
        this.addExpNodeList = addExpNodeList;
        this.relOpList = relOpList;
    }

    public List<AddExpNode> getAddExpNodeList() {
        return addExpNodeList;
    }

    public List<String> getRelOpList() {
        return relOpList;
    }

    @Override
    public void show() throws IOException {
        for (int i = 0; i < addExpNodeList.size(); i++) {
            if (i < relOpList.size() + 1 && i != 0) {
                switch (relOpList.get(i - 1)) {
                    case "<" -> Parser.parseWriter.write(LexerType.LSS.name() + " " + "<" + "\n");
                    case "<=" -> Parser.parseWriter.write(LexerType.LEQ.name() + " " + "<=" + "\n");
                    case ">" -> Parser.parseWriter.write(LexerType.GRE.name() + " " + ">" + "\n");
                    default -> Parser.parseWriter.write(LexerType.GEQ.name() + " " + ">=" + "\n");
                }
            }

            addExpNodeList.get(i).show();
            Parser.parseWriter.write(name + "\n");
        }
    }

}
