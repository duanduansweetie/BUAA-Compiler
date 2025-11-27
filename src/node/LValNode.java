package node;
import java.io.IOException;
import lexer.LexerType;
import parser.Parser;
import symbol.ArraySymbol;
import symbol.BasicSymbol;
import symbol.ScopeStack;
import symbol.Symbol;
public class LValNode extends Node{
    // LVal → Ident ['[' Exp ']']
    public final String name="<LVal>";
    private final String identNode;
    private ExpNode expNode;
    private ScopeStack scopeStack;
    private int lineno;
    public LValNode(String identNode, ExpNode expNode){
        this.identNode = identNode;
        this.expNode = expNode;
    }
    public void setScopeStack(ScopeStack scopeStack) {
        this.scopeStack = scopeStack;
    }
    public LValNode(String identNode){
        this.identNode = identNode;
    }
    public String getIdentNode() {
        return identNode;
    }
    public ExpNode getExpNode() {
        return expNode;
    }
    public int getLineno(){
        return lineno;
    }
    public void setLineno(int lineno) {
        this.lineno = lineno;
    }
    @Override
    public void show() throws IOException{
        Parser.parseWriter.write(LexerType.IDENFR.name()+" "+identNode+"\n");
        if(expNode!=null){
            Parser.parseWriter.write(LexerType.LBRACK.name()+" [\n");
            try {
                expNode.show();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Parser.parseWriter.write(LexerType.RBRACK.name()+" ]\n");
        }
        Parser.parseWriter.write(name+"\n");
    }
    public int calculate(){
        Symbol symbol = scopeStack.lookup(identNode);
        if (symbol instanceof BasicSymbol) {
            return ((BasicSymbol) symbol).getValueNum();
        } else if (symbol instanceof ArraySymbol) {
            if (expNode != null) {
                int index = expNode.calculate();
                return ((ArraySymbol) symbol).getValue(index);
            }
        }
        throw new RuntimeException("Cannot calculate value for " + identNode);
    }
    public boolean isMayArray(){
        return expNode==null;
    }
    public boolean canCalculate(){
        Symbol symbol=scopeStack.lookup(identNode);
        if(symbol instanceof BasicSymbol){
            if (((BasicSymbol) symbol).isCanCalculate()) {
                return true;
            }
        }
        else if(symbol instanceof ArraySymbol){
            if(((ArraySymbol) symbol).isCanCal() && expNode != null && expNode.canCalculate()){
                return true;
            }
        }
        return false;
    }
}
