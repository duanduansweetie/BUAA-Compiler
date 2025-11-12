package node;

import lexer.LexerType;
import parser.Parser;

import java.io.IOException;
import java.util.List;

public class MulExpNode extends Node {
    public final String name = "<MulExp>";
    // MulExp → UnaryExp | MulExp ('\*' | '/' | '%') UnaryExp
    // 改写文法
    // MulExp → UnaryExp {('\*' | '/' | '%') UnaryExp}
    private final List<UnaryExpNode> unaryExpNodeList;
    private final List<String> mulOpList;

    public MulExpNode(List<UnaryExpNode> unaryExpNodeList, List<String> mulOpList) {
        this.unaryExpNodeList = unaryExpNodeList;
        this.mulOpList = mulOpList;
    }

    public List<String> getMulOpList() {
        return mulOpList;
    }

    @Override
    public void show() throws IOException {
        for (int i = 0; i < unaryExpNodeList.size(); i++) {
            if (i < mulOpList.size() + 1 && i != 0) {
                if (mulOpList.get(i - 1).equals("*")) {
                    Parser.parseWriter.write(LexerType.MULT.name() + " " + "*" + "\n");
                } else if (mulOpList.get(i - 1).equals("/")) {
                    Parser.parseWriter.write(LexerType.DIV.name() + " " + "/" + "\n");
                } else if( mulOpList.get(i - 1).equals("%")) {
                    Parser.parseWriter.write(LexerType.MOD.name() + " " + "%" + "\n");
                }
                else{
                    Parser.parseWriter.write(LexerType.BITAND.name()+" "+"bitand"+"\n");

                }
            }

            unaryExpNodeList.get(i).show();
            Parser.parseWriter.write(name + "\n");
        }
    }

    public List<UnaryExpNode> getUnaryExpNodeList() {
        return unaryExpNodeList;
    }
    public int calculate(){
        int result=unaryExpNodeList.get(0).calculate();
        for(int i=1;i<unaryExpNodeList.size();i++){
            if(mulOpList.get(i-1).equals("*")){
                result*=unaryExpNodeList.get(i).calculate();
            }else if(mulOpList.get(i-1).equals("/")){
                result/=unaryExpNodeList.get(i).calculate();
            }else if(mulOpList.get(i-1).equals("%")){
                result%=unaryExpNodeList.get(i).calculate();
            }
        }
        return result;
    }
    public boolean canCalculate(){
        for(UnaryExpNode unaryExpNode:unaryExpNodeList){
            if(!unaryExpNode.canCalculate()){
                return false;
            }
        }
        return true;
    }
}
