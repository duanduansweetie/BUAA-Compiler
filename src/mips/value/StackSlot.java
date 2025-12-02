package mips.value;

public class StackSlot implements MipsOperand {
    private int offset;

    public StackSlot(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return offset + "($sp)";
    }
}
