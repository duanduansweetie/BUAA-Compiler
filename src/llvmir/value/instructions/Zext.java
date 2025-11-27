package llvmir.value.instructions;

import llvmir.type.Integer32Type;
import llvmir.value.Value;
import llvmir.value.structure.BasicBlock;
import llvmir.value.structure.Instruction;

public class Zext extends Instruction {
    // value 存储待转换的值
    public Zext(Value value, BasicBlock basicBlock) {
        super(null, new Integer32Type(), basicBlock);
        this.addOperand(value);
    }

    @Override
    public String toString() {
        return name + " = zext " + operands.get(0).getType() + " " +
                this.getFirstOperand().getName() + " to i32";
    }
}
