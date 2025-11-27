package llvmir.value.instructions;
import llvmir.value.Value;
import llvmir.value.structure.BasicBlock;
import llvmir.value.structure.Instruction;
public class Br extends Instruction{
    public Br(BasicBlock b1, BasicBlock b2, Value condition,BasicBlock own) {
        super("Br",null,own);
        operands.add(b1);
        operands.add(b2);
        operands.add(condition);
    }
    public Br(BasicBlock b1,BasicBlock own) {
        super("Br",null,own);
        operands.add(b1);
    }
    @Override
    public String toString(){
        if(operands.size()==1){
            return "br label " + operands.get(0).getPrintName();
        }
        else {
            return "br i1 " + operands.get(2).getPrintName() + ", label " +
                    operands.get(0).getPrintName() + ", label " +
                    operands.get(1).getPrintName();
        }
    }
}
