package mips.value;

public class GlobalLabel implements MipsOperand {
    private String name;

    public GlobalLabel(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
