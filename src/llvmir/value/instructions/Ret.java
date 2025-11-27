package llvmir.value.instructions;

import llvmir.value.Value;
import llvmir.value.structure.BasicBlock;
import llvmir.value.structure.Instruction;

public class Ret extends Instruction {
    public Ret(Value value, BasicBlock basicBlock) {
        super("Ret", value == null ? null : value.getType(), basicBlock);
        this.operands.add(value);
    }

    @Override
    public String toString() {
        if (operands.size() == 0 || operands.get(0) == null)
            return "ret void";
        return "ret " + operands.get(0).getType() + " " + operands.get(0).getPrintName();
    }

}
