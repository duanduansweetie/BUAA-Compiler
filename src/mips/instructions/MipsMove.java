package mips.instructions;

import mips.structure.MipsInstr;
import mips.value.GlobalLabel;
import mips.value.MipsImm;
import mips.value.MipsOperand;
import mips.value.PhyReg;
import mips.value.StackSlot;

public class MipsMove extends MipsInstr {
    private MipsOperand dst;
    private MipsOperand src;

    public MipsMove(MipsOperand dst, MipsOperand src) {
        this.dst = dst;
        this.src = src;
    }

    @Override
    public String toString() {
        if (dst instanceof PhyReg && src instanceof PhyReg) {
            return "move " + dst + ", " + src;
        } else if (dst instanceof PhyReg && src instanceof MipsImm) {
            return "li " + dst + ", " + src;
        } else if (dst instanceof PhyReg && (src instanceof StackSlot || src instanceof GlobalLabel)) {
            return "lw " + dst + ", " + src;
        } else if ((dst instanceof StackSlot || dst instanceof GlobalLabel) && src instanceof PhyReg) {
            return "sw " + src + ", " + dst;
        } else if ((dst instanceof StackSlot || dst instanceof GlobalLabel) && src instanceof MipsImm) {
            return "li $v1, " + src + "\nsw $v1, " + dst;
        } else if ((dst instanceof StackSlot || dst instanceof GlobalLabel) && (src instanceof StackSlot || src instanceof GlobalLabel)) {
            return "lw $v1, " + src + "\nsw $v1, " + dst;
        } else {
            return "# Unhandled move: " + dst + " <- " + src;
        }
    }
}
