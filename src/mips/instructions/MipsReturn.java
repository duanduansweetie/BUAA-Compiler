package mips.instructions;

import mips.structure.MipsFunc;
import mips.structure.MipsInstr;
import mips.value.GlobalLabel;
import mips.value.MipsImm;
import mips.value.MipsOperand;
import mips.value.PhyReg;
import mips.value.RegManager;
import mips.value.StackSlot;

public class MipsReturn extends MipsInstr {
    private MipsOperand op;
    private boolean isMain;
    private int stackOffset;
    private MipsFunc func;

    public MipsReturn(int stackOffset, MipsFunc func) {
        this.stackOffset = stackOffset;
        this.isMain = false;
        this.func = func;
    }

    public MipsReturn(MipsOperand op, boolean isMain, int stackOffset, MipsFunc func) {
        this.op = op;
        this.isMain = isMain;
        this.stackOffset = stackOffset;
        this.func = func;
    }

    @Override
    public String toString() {
        if (isMain) {
            return "li $v0, 10\nsyscall";
        }
        StringBuilder sb = new StringBuilder();
        if (op == null) {
            sb.append(handleStackOffset()).append("jr $ra");
        } else if (op instanceof MipsImm) {
            sb.append("li $v0, ").append(((MipsImm)op).getValue()).append("\n").append(handleStackOffset()).append("jr $ra");
        } else if (op instanceof StackSlot) {
            sb.append("lw $v0, ").append(op).append("\n").append(handleStackOffset()).append("jr $ra");
        } else if (op instanceof GlobalLabel) {
            sb.append("lw $v0, ").append(op).append("\n").append(handleStackOffset()).append("jr $ra");
        } else {
            sb.append("move $v0, ").append(op).append("\n").append(handleStackOffset()).append("jr $ra");
        }
        return sb.toString();
    }

    private String handleStackOffset() {
        StringBuilder sb = new StringBuilder();
        
        // Restore Callee-Saved Registers
        if (func != null) {
            for (PhyReg reg : func.getUsedRegisters()) {
                if (reg.isCalleeSaved()) {
                    if (RegManager.TEMP_REG_OFFSET.containsKey(reg)) {
                        sb.append("lw ").append(reg).append(", ")
                          .append(func.getStackSize() + RegManager.TEMP_REG_OFFSET.get(reg)).append("($sp)\n");
                    }
                }
            }
        }
        
        sb.append("lw $ra, ").append(stackOffset - 4).append("($sp)\n");
        sb.append("addi $sp, $sp, ").append(stackOffset).append("\n");
        return sb.toString();
    }
}
