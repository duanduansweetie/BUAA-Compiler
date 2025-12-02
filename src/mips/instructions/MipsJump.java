package mips.instructions;

import mips.structure.MipsInstr;

public class MipsJump extends MipsInstr {
    private String label;

    public MipsJump(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return "j " + label;
    }
}
