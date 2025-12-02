package mips.instructions;

import mips.structure.MipsInstr;
import mips.value.GlobalLabel;
import mips.value.MipsImm;
import mips.value.MipsOperand;
import mips.value.StackSlot;

public class MipsReturn extends MipsInstr {
    private MipsOperand op;
    private boolean isMain;
    private int stackOffset;

    public MipsReturn(int stackOffset) {
        this.stackOffset = stackOffset;
        this.isMain = false;
    }

    public MipsReturn(MipsOperand op, boolean isMain, int stackOffset) {
        this.op = op;
        this.isMain = isMain;
        this.stackOffset = stackOffset;
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
        return "lw $ra, " + (stackOffset - 4) + "($sp)\n" + "addi $sp, $sp, " + stackOffset + "\n";
    }
}
