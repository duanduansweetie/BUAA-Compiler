package mips;

import llvmir.value.Value;
import llvmir.value.constants.ConstantInt;
import llvmir.value.structure.BasicBlock;
import llvmir.value.structure.Instruction;
import mips.value.MipsImm;
import mips.value.MipsOperand;
import mips.value.PhyReg;
import mips.value.RegManager;

public class RegisterAllocator {
    private final GenerationContext context;

    public RegisterAllocator(GenerationContext context) {
        this.context = context;
    }

    public MipsOperand allocateReg(Value value) {
        if (value.getRegister() != null) {
            return value.getRegister();
        }
        PhyReg reg = RegManager.getTempRegister(context.getCurrFunc());
        value.setRegister(reg);
        return reg;
    }

    public MipsOperand getOperand(Value value) {
        if (value.getRegister() != null) {
            return value.getRegister();
        }
        if (value instanceof ConstantInt) {
            MipsImm imm = new MipsImm(((ConstantInt) value).getValue());
            value.setRegister(imm);
            return imm;
        }
        PhyReg reg = RegManager.getTempRegister(context.getCurrFunc());
        value.setRegister(reg);
        return reg;
    }

    public void tryToReleaseRegister(Value instruction, BasicBlock bb, int i) {
        Instruction nowInstruction = (Instruction) instruction;
        int flag = 0;
        for (int j = i + 1; j < bb.getInstructions().size(); j++) {
            Instruction nextInstruction = (Instruction) bb.getInstructions().get(j);
            if (nextInstruction.getOperands() == null || (nextInstruction.getOperands().size() == 0)) {
                continue;
            }
            for (Value operand : nextInstruction.getOperands()) {
                if (operand == nowInstruction) {
                    flag++;
                }
            }
        }

        if (flag == 0 && instruction.getRegister() != null) {
            instruction.clearRegister(context.getCurrFunc());
        }
        if (((Instruction) instruction).getOperands() == null
                || ((Instruction) instruction).getOperands().size() == 0) {
            return;
        }
        for (Value operand : nowInstruction.getOperands()) {
            int flags = 0;
            for (int j = i + 1; j < bb.getInstructions().size(); j++) {
                Instruction nextInstruction = (Instruction) bb.getInstructions().get(j);
                if (nextInstruction.getOperands() == null || (nextInstruction.getOperands().size() == 0)) {
                    continue;
                }
                for (Value operandNext : nextInstruction.getOperands()) {
                    if (operand == operandNext) {
                        flags++;
                    }
                }
            }
            if (flags == 0 && operand != null && operand.getRegister() != null) {
                operand.clearRegister(context.getCurrFunc());
            }
        }
    }
}
