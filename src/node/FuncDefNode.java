package node;
import java.io.IOException;
import lexer.LexerType;
import parser.Parser;
public class FuncDefNode extends Node{
    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    public final String name="<FuncDef>";
    private final FuncTypeNode funcType;
    private final String ident;
    private final FuncFParamsNode funcFParams; // can be null
    private BlockNode block;
    public FuncDefNode(FuncTypeNode funcType, String ident, FuncFParamsNode funcFParams, BlockNode block){
        this.funcType = funcType;
        this.ident = ident;
        this.funcFParams = funcFParams;
        this.block = block;
    }
    public FuncDefNode(FuncTypeNode funcType, String ident, FuncFParamsNode funcFParams){
        this.funcType = funcType;
        this.ident = ident;
        this.funcFParams = funcFParams;
    }
    public FuncTypeNode getFuncType() {
        return funcType;
    }
    public String getIdent() {
        return ident;
    }
    public FuncFParamsNode getFuncFParams() {
        return funcFParams;
    }
    public BlockNode getBlock() {
        return block;
    }
    @Override
    public void show() throws IOException{

        funcType.show();
        Parser.parseWriter.write("IDENFR "+ident+"\n");
        Parser.parseWriter.write("LPARENT (\n");
        if(funcFParams!=null){
            funcFParams.show();
        }
        Parser.parseWriter.write("RPARENT )\n");
        block.show();
        Parser.parseWriter.write(name+"\n");
    }
}
