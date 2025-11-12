package node;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lexer.LexerType;
import parser.Parser;


public class StmtNode extends Node {
    public final String name = "<Stmt>";
    //新增
    private BTypeNode bTypeNode;
    private String ident;
    private InitValNode initValNode;
    private boolean isNewIf=false;
    // 标志位
    private boolean isGetint = false;
    private boolean isGetchar = false;
    private boolean isBreak = false;
    private boolean isContinue = false;
    private boolean isReturn = false;
    private boolean isIf = false;
    private boolean isFor = false;
    private boolean isCond=false;
    private String operater;
    // 子节点
    private LValNode lValNode;
    private ExpNode expNode;
    private BlockNode blockNode;
    private CondNode condNode;
    private ForStmtNode forStmtNode1;
    private ForStmtNode forStmtNode2;
    private StmtNode stmtNode1;
    private StmtNode stmtNode2;
    private String stringConst;
    private ExpNode trueExpNode;
    private ExpNode falseExpNode;
    private List<ExpNode> expNodeList = new ArrayList<>();

    public StmtNode(CondNode condNode,ExpNode trueExpNode,ExpNode falseExpNode){
        this.condNode=condNode;
        this.trueExpNode=trueExpNode;
        this.falseExpNode=falseExpNode;
        this.isCond=true;
    }
    // 构造方法
    public StmtNode(LValNode lValNode, String operater,ExpNode expNode) {
        this.lValNode = lValNode;
        this.operater=operater;
        this.expNode = expNode;
    }
    public StmtNode(){

    }
    public StmtNode(ExpNode expNode) {
        this.expNode = expNode;
    }

    public StmtNode(ExpNode expNode, boolean isReturn) {
        this.expNode = expNode;
        this.isReturn = isReturn;
    }

    public StmtNode(BlockNode blockNode) {
        this.blockNode = blockNode;
    }

    public StmtNode(CondNode condNode, StmtNode stmtNode1, StmtNode stmtNode2) {
        this.condNode = condNode;
        this.stmtNode1 = stmtNode1;
        this.stmtNode2 = stmtNode2;
        this.isIf = true;
    }
    public StmtNode(BTypeNode bTypeNode,String ident,InitValNode initValNode,StmtNode stmtNode1,StmtNode stmtNode2){
        this.bTypeNode=bTypeNode;
        this.ident=ident;
        this.initValNode=initValNode;
        this.stmtNode1=stmtNode1;
        this.stmtNode2=stmtNode2;
        this.isNewIf=true;
        this.isIf=true;
    }
    public StmtNode(ForStmtNode forStmtNode1, CondNode condNode, ForStmtNode forStmtNode2, StmtNode stmtNode) {
        this.forStmtNode1 = forStmtNode1;
        this.condNode = condNode;
        this.forStmtNode2 = forStmtNode2;
        this.stmtNode1 = stmtNode;
        this.isFor = true;
    }

    public StmtNode(String stringConst, List<ExpNode> expNodeList) {
        this.stringConst = stringConst;
        this.expNodeList = expNodeList;
    }

    public StmtNode(LValNode lValNode, String getString) {
        this.lValNode = lValNode;
        if ("getint".equals(getString)) {
            this.isGetint = true;
        } else if ("getchar".equals(getString)) {
            this.isGetchar = true;
        }
    }

    public StmtNode(String type) {
        if ("break".equals(type)) {
            this.isBreak = true;
        } else if ("continue".equals(type)) {
            this.isContinue = true;
        }
    }

    @Override
    public void show() throws IOException {
        if (isLValAndExp()) {
            showLValAndExp(operater);
        } else if (isBlock()) {
            try {
                blockNode.show();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (isIf) {
            showIf();
        } else if (isFor) {
            showFor();
        } else if (isBreak) {
            showBreak();
        } else if (isContinue) {
            showContinue();
        } else if (isReturn) {
            showReturn();
        } else if (isPrintf()) {
            showPrintf();
        }
        else if(isCond){
            showCond();
        }
        else {
            showExpOrEmpty();
        }
        Parser.parseWriter.write(name + "\n");
    }
    private void showCond() throws IOException {
        condNode.show();
        Parser.parseWriter.write(LexerType.QUES.name() + " " + "?" + "\n");
        trueExpNode.show();
        Parser.parseWriter.write(LexerType.COLON.name() + " " + ":" + "\n");
        falseExpNode.show();
        Parser.parseWriter.write(LexerType.SEMICN.name() + " " + ";" + "\n");
    }
    // 分解 show 方法的逻辑
    private void showLValAndExp(String operater) throws IOException {
        lValNode.show();
        LexerType op;
        switch (operater) {
            case "=":
                op= LexerType.ASSIGN;
                break;
            case "+=":
                op = LexerType.ADDASSI;
                break;
            case "-=" :
                op = LexerType.MINUASSI;
                break;
            case "*=" :
                op = LexerType.MULTASSI;
                break;
            case "/=":
                op = LexerType.DIVASSI;
                break;
            default :
                throw new IllegalArgumentException("Unexpected operator: " + operater);
        }
        Parser.parseWriter.write(op.name() + " " + op.toString() + "\n");
        //System.out.println(op.name()+" "+op.toString());
        expNode.show();
        Parser.parseWriter.write(LexerType.SEMICN.name() + " " + ";" + "\n");
    }
    private void showIf() throws IOException {
        //System.out.println("333");
        if(isNewIf==false){ Parser.parseWriter.write(LexerType.IFTK.name() + " " + "if" + "\n");
            Parser.parseWriter.write(LexerType.LPARENT.name() + " " + "(" + "\n");
            condNode.show();
            Parser.parseWriter.write(LexerType.RPARENT.name() + " " + ")" + "\n");
            stmtNode1.show();
            if (stmtNode2 != null) {
                Parser.parseWriter.write(LexerType.ELSETK.name() + " " + "else" + "\n");
                stmtNode2.show();
            }}
       else if(isNewIf=true){
           //System.out.println("222");
            Parser.parseWriter.write(LexerType.IFTK.name() + " " + "if" + "\n");
            Parser.parseWriter.write(LexerType.LPARENT.name() + " " + "(" + "\n");
           bTypeNode.show();
            Parser.parseWriter.write(LexerType.IDENFR.name()+" "+ident+"\n");
            Parser.parseWriter.write(LexerType.ASSIGN.name()+" "+"="+"\n");
              initValNode.show();
            Parser.parseWriter.write(LexerType.RPARENT.name() + " " + ")" + "\n");
            stmtNode1.show();
            if (stmtNode2 != null) {
                Parser.parseWriter.write(LexerType.ELSETK.name() + " " + "else" + "\n");
                stmtNode2.show();
            }
        }
    }

    private void showFor() throws IOException {
        Parser.parseWriter.write(LexerType.FORTK.name() + " " + "for" + "\n");
        Parser.parseWriter.write(LexerType.LPARENT.name() + " " + "(" + "\n");
        if (forStmtNode1 != null) {
            forStmtNode1.show();
        }
        Parser.parseWriter.write(LexerType.SEMICN.name() + " " + ";" + "\n");
        if (condNode != null) {
            condNode.show();
        }
        Parser.parseWriter.write(LexerType.SEMICN.name() + " " + ";" + "\n");
        if (forStmtNode2 != null) {
            forStmtNode2.show();
        }
        Parser.parseWriter.write(LexerType.RPARENT.name() + " " + ")" + "\n");
        if(stmtNode1!=null){
            stmtNode1.show();
        }
    }

    private void showBreak() throws IOException {
        Parser.parseWriter.write(LexerType.BREAKTK.name() + " " + "break" + "\n");
        Parser.parseWriter.write(LexerType.SEMICN.name() + " " + ";" + "\n");
    }

    private void showContinue() throws IOException {
        Parser.parseWriter.write(LexerType.CONTINUETK.name() + " " + "continue" + "\n");
        Parser.parseWriter.write(LexerType.SEMICN.name() + " " + ";" + "\n");
    }

    private void showReturn() throws IOException {
        Parser.parseWriter.write(LexerType.RETURNTK.name() + " " + "return" + "\n");
        if (expNode != null) {
            expNode.show();
        }
        Parser.parseWriter.write(LexerType.SEMICN.name() + " " + ";" + "\n");
    }

    private void showPrintf() throws IOException {
        Parser.parseWriter.write(LexerType.PRINTFTK.name() + " " + "printf" + "\n");
        Parser.parseWriter.write(LexerType.LPARENT.name() + " " + "(" + "\n");
        Parser.parseWriter.write(LexerType.STRCON.name() + " " + stringConst + "\n");
        for (ExpNode expNode : expNodeList) {
            Parser.parseWriter.write(LexerType.COMMA.name() + " " + "," + "\n");
            expNode.show();
        }
        Parser.parseWriter.write(LexerType.RPARENT.name() + " " + ")" + "\n");
        Parser.parseWriter.write(LexerType.SEMICN.name() + " " + ";" + "\n");
    }

    private void showExpOrEmpty() throws IOException {
        if (isExp()) {
            expNode.show();
        }
        Parser.parseWriter.write(LexerType.SEMICN.name() + " " + ";" + "\n");
    }

    // 判断方法
    public boolean isReturn() {
        return isReturn;
    }

    public boolean isBreak() {
        return isBreak;
    }

    public boolean isContinue() {
        return isContinue;
    }

    public boolean isFor() {
        return isFor;
    }

    public boolean isIf() {
        return isIf;
    }

    public boolean isGetchar() {
        return isGetchar;
    }

    public boolean isGetint() {
        return isGetint;
    }

    public boolean isLValAndExp() {
        return lValNode != null && expNode != null;
    }

    public boolean isBlock() {
        return blockNode != null;
    }

    public boolean isPrintf() {
        return stringConst != null;
    }

    public boolean isExp() {
        return expNode != null && !isReturn && !isLValAndExp();
    }

    // Getter 方法
    public BlockNode getBlockNode() {
        return blockNode;
    }

    public CondNode getCondNode() {
        return condNode;
    }

    public ExpNode getExpNode() {
        return expNode;
    }

    public List<ExpNode> getExpNodeList() {
        return expNodeList;
    }

    public ForStmtNode getForStmtNode1() {
        return forStmtNode1;
    }

    public ForStmtNode getForStmtNode2() {
        return forStmtNode2;
    }

    public StmtNode getStmtNode1() {
        return stmtNode1;
    }

    public StmtNode getStmtNode2() {
        return stmtNode2;
    }

    public String getStringConst() {
        return stringConst;
    }

    public LValNode getLValNode() {
        return lValNode;
    }
}