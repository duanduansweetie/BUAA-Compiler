package node;

import java.io.IOException;

public class BlockItemNode extends Node {
    public String name = "<BlockItem>";
    // BlockItem → Decl | Stmt
    private DeclNode declNode;
    private StmtNode stmtNode;

    public BlockItemNode(DeclNode declNode) {
        this.declNode = declNode;
    }

    public BlockItemNode(StmtNode stmtNode) {
        this.stmtNode = stmtNode;
    }

    @Override
    public void show() throws IOException {
        if (declNode != null) {
            declNode.show();
        } else {
            stmtNode.show();
        }
    }

    public StmtNode getStmtNode() {
        return stmtNode;
    }

    public DeclNode getDeclNode() {
        return declNode;
    }

}
