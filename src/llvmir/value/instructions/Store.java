package llvmir.value.instructions;

import llvmir.value.Value;
import llvmir.value.structure.BasicBlock;
import llvmir.value.structure.Instruction;

public class Store extends Instruction {
    public Store(Value value, Value pointer, BasicBlock basicBlock) {
        // 不需要输出Store的类型，因为Store没有返回值
        super("Store", null, basicBlock);
        operands.add(value);
        operands.add(pointer);
    }

    @Override
    public String toString() {
        return "store " + operands.get(0).getType() + " " + operands.get(0).getPrintName() + ", " +
                operands.get(0).getType() + "* " + operands.get(1).getPrintName();
    }
}
