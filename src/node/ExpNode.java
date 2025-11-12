package node;
import lexer.LexerType;
import parser.Parser;
import java.io.IOException;
import java.util.List;
import java.util.List;

public class ExpNode extends Node{
    // Exp → AddExp
    public final String name="<Exp>";
    private final AddExpNode addExpNode;
    public ExpNode(AddExpNode addExpNode){
        this.addExpNode = addExpNode;
    }
    public static final LexerType[] first = { LexerType.IDENFR, LexerType.PLUS,
            LexerType.MINU, LexerType.NOT, LexerType.LPARENT, LexerType.INTCON};
    public AddExpNode getAddExpNode() {
        return addExpNode;
    }
    public static boolean contains(LexerType lexType) {
        for (LexerType type : first) {
            if (type == lexType) {
                return true;
            }
        }
        return false;
    }
    @Override
    public void show() throws IOException{
        addExpNode.show();
        Parser.parseWriter.write(name+"\n");
    }
    public boolean canCalculate() {
        return addExpNode.canCalculate();
    }
    public int calculate(){
        return addExpNode.calculate();
    }
    public String getIdent(){
        // Exp → AddExp
        // AddExp → MulExp { ('+' | '-') MulExp}
        // MulExp → UnaryExp { ('*' | '/') UnaryExp}
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        // PrimaryExp → '(' Exp ')' | LVal | Number
        // LVal → Ident ['[' Exp ']']
        // 当且仅当，Exp为Ident时，返回Ident，否则返回null
        if(this.getAddExpNode()==null){
            return null;
        }
        List<MulExpNode> mulExpNodeList=this.getAddExpNode().getMulExpNodeList();
        if(mulExpNodeList==null||mulExpNodeList.isEmpty()){
            return null;
        }
        MulExpNode firstMulExpNode=mulExpNodeList.get(0);
        List<UnaryExpNode> unaryExpNodes=firstMulExpNode.getUnaryExpNodeList();
        if(unaryExpNodes==null||unaryExpNodes.isEmpty()){
            return null;
        }
        UnaryExpNode firstUnaryExpNode=unaryExpNodes.get(0);
        PrimaryExpNode primaryExpNode=firstUnaryExpNode.getPrimaryExpNode();
        if(primaryExpNode==null||primaryExpNode.getlValNode()==null){
            return null;
        }
        String identNode=primaryExpNode.getlValNode().getIdentNode();
        boolean isMayArray=primaryExpNode.getlValNode().isMayArray();
        if(identNode!=null&&isMayArray){
            return identNode;
        }
        return null;
    }

}
