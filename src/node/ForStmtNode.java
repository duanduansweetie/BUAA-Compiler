package node;

import lexer.LexerType;
import parser.Parser;

import java.io.IOException;
import java.util.List;

public class ForStmtNode extends Node {
    public final String name = "<ForStmt>";
    // ForStmt → LVal '=' Exp { ',' LVal '=' Exp }
    private final List<LValNode> lValNodes; // 支持多个 LVal
    private final List<ExpNode> expNodes;   // 支持多个 Exp
    // 新的构造方法，接收多个 LVal 和 Exp
    public ForStmtNode(List<LValNode> lValNodes, List<ExpNode> expNodes) {
        if (lValNodes.size() != expNodes.size()) {
            throw new IllegalArgumentException("The number of LVal and Exp must be the same.");
        }
        this.lValNodes = lValNodes;
        this.expNodes = expNodes;
    }
    public List<ExpNode> getExpNode() {
        return expNodes;
    }

    public List<LValNode> getlValNode() {
        return lValNodes;
    }
    // Getter 方法
    public List<LValNode> getLValNodes() {
        return lValNodes;
    }

    public List<ExpNode> getExpNodes() {
        return expNodes;
    }

    @Override
    public void show() throws IOException {
        for (int i = 0; i < lValNodes.size(); i++) {
            lValNodes.get(i).show(); // 显示 LVal
            Parser.parseWriter.write(LexerType.ASSIGN.name() + " " + "=" + "\n"); // 显示 '='
            expNodes.get(i).show(); // 显示 Exp
            if (i < lValNodes.size() - 1) {
                Parser.parseWriter.write(LexerType.COMMA.name() + " " + "," + "\n"); // 显示 ','
            }
        }
        Parser.parseWriter.write(name + "\n"); // 显示 ForStmt 的语法成分
    }
}