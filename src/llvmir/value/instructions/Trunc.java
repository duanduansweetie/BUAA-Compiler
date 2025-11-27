package llvmir.value.instructions;

import llvmir.type.Integer8Type;
import llvmir.value.Value;
import llvmir.value.structure.BasicBlock;
import llvmir.value.structure.Instruction;

public class Trunc extends Instruction {
    public Trunc(Value value, BasicBlock basicBlock) {
        super(null, new Integer8Type(), basicBlock);
        this.addOperand(value);
    }

    @Override
    public String toString() {
        return name + " = trunc " + operands.get(0).getType() + " " +
                this.getFirstOperand().getName() + " to i8";
    }

}
