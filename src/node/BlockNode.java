package node;
import java.io.IOException;
import java.util.List;
import lexer.LexerType;
import parser.Parser;
public class BlockNode extends Node{
    // Block → '{' { BlockItem } '}'
    public final String name = "<Block>";
    private List<BlockItemNode> blockItemNodes;
    public int symbolTableIndex;
    public BlockNode(List<BlockItemNode> blockItemNodes,int symbolTableIndex) {
        this.blockItemNodes = blockItemNodes;
        this.symbolTableIndex = symbolTableIndex;
    }
    public List<BlockItemNode> getBlockItemNodes() {
        return blockItemNodes;
    }
    @Override
    public void show() throws IOException {
        Parser.parseWriter.write(LexerType.LBRACE.name() + " {\n");
        for (BlockItemNode blockItemNode : blockItemNodes) {
            blockItemNode.show();
        }
        Parser.parseWriter.write(LexerType.RBRACE.name() + " }\n");
        Parser.parseWriter.write(name + "\n");
    }

}
