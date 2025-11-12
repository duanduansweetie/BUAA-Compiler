package node;

import java.io.IOException;

public class DeclNode extends Node {
    public String name = "<Decl>";
    // Decl → ConstDecl | VarDecl
    private ConstDeclNode constDeclNode;
    private VarDeclNode varDeclNode;

    public DeclNode(ConstDeclNode constDeclNode) {
        this.constDeclNode = constDeclNode;
    }

    public DeclNode(VarDeclNode varDeclNode) {
        this.varDeclNode = varDeclNode;
    }

    @Override
    public void show() throws IOException {
        if (constDeclNode != null) {
            constDeclNode.show();
        } else {
            varDeclNode.show();
        }
        // 不输出的语法成分
    }

    public ConstDeclNode getConstDeclNode() {
        return constDeclNode;
    }

    public VarDeclNode getVarDeclNode() {
        return varDeclNode;
    }

}
