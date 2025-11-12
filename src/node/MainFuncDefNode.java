package node;
import java.io.IOException;
import lexer.LexerType;
import parser.Parser;
public class MainFuncDefNode extends Node{
    // MainFuncDef → 'int' 'main' '(' ')' Block
    public final String name = "<MainFuncDef>";
    private final Node blockNode;
    public MainFuncDefNode(BlockNode blockNode) {
        this.blockNode = blockNode;
    }
    @Override
    public void show() throws IOException {
        Parser.parseWriter.write(LexerType.INTTK.name() + " int\n");
        Parser.parseWriter.write(LexerType.MAINTK.name() + " main\n");
        Parser.parseWriter.write(LexerType.LPARENT.name() + " (\n");
        Parser.parseWriter.write(LexerType.RPARENT.name() + " )\n");
        try {
            blockNode.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Parser.parseWriter.write(name + "\n");
    }
}
