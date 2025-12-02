package mips.value;

public class MipsImm implements MipsOperand {
    private int value;

    public MipsImm(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
