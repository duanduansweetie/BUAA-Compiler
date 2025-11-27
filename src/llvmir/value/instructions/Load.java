package llvmir.value.instructions;

import llvmir.type.PointType;
import llvmir.value.Value;
import llvmir.value.structure.BasicBlock;
import llvmir.value.structure.Instruction;

public class Load extends Instruction {

    public Load(Value pointer, BasicBlock basicBlock) {
        super(null, ((PointType) pointer.getType()).getPoint(), basicBlock);
        this.operands.add(pointer);
    }

    @Override
    public String toString() {
        return getPrintName() + " = load " + this.getType() + ", " + operands.get(0).getType() + " " + operands.get(0).getPrintName();
    }
}
