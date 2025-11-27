package llvmir.value.instructions;
import llvmir.type.Integer1Type;
import llvmir.value.Value;
import llvmir.value.structure.BasicBlock;
import llvmir.value.structure.Instruction;
import java.util.HashMap;
import java.util.Map;
public class Icmp extends Instruction{
    private String op;
    public String getOp() {
        return op;
    }
    public Icmp(Value left, Value right, String op, BasicBlock basicBlock) {
        super(null, new Integer1Type(), basicBlock);
        operands.add(left);
        operands.add(right);
        this.op = op;
    }
    // 可能出现的操作符有：eq, ne, slt, sgt, sle, sge，构建一个map存储这些操作符对应的字符串
    Map<String, String> opMap = new HashMap<String, String>() {
        {
            put("==", "eq");
            put("!=", "ne");
            put("<", "slt");
            put(">", "sgt");
            put("<=", "sle");
            put(">=", "sge");
        }
    };

    @Override
    public String toString() {
        return name + " = icmp " + opMap.get(op) + " " + operands.get(0).getType() + " " + operands.get(0).getName()
                + ", "
                + operands.get(1).getName();
    }
}
