package llvmir.value.instructions;
import llvmir.value.Value;
import llvmir.value.structure.BasicBlock;
import llvmir.value.structure.Instruction;
import llvmir.type.Integer32Type;
import java.util.HashMap;
import java.util.Map;

public class BinaryOp extends Instruction{
    private String op;
    public BinaryOp(Value left,Value right,String op,BasicBlock parentBlock){
        super(null,new Integer32Type(),parentBlock);
        this.op = op;
        operands.add(left);
        operands.add(right);
    }
    public String getOp(){
        return op;
    }
    Map<String,String> opMap=new HashMap<String,String>(){
        {
             // 算术运算
            put("+", "add");
            put("-", "sub");
            put("*", "mul");
            put("/", "sdiv"); // 有符号除法
            put("%", "srem"); // 有符号取模
            put("add","add");
            put("sub","sub");
            put("mul","mul");
            put("sdiv","sdiv");
            put("and","and");
            put("or","or");
            put("xor","xor");
        }
    };
    @Override
    public String toString() {
        return getPrintName() + " = " + opMap.get(op) + " " + this.getType() + " " + operands.get(0).getPrintName() + ", "
                + operands.get(1).getPrintName();
    }
}
