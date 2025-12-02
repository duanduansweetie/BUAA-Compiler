package mips.value;

public class PhyReg implements MipsOperand {
    private final String name;
    private final int index;
    private final RegType type;

    public enum RegType {
        TEMP, SAVED, ARG, RET, SPECIAL
    }

    public PhyReg(String name, int index, RegType type) {
        this.name = name;
        this.index = index;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public RegType getType() {
        return type;
    }

    @Override
    public String toString() {
        return name;
    }

    public static final PhyReg ZERO = new PhyReg("$zero", 0, RegType.SPECIAL);
    public static final PhyReg AT = new PhyReg("$at", 1, RegType.SPECIAL);
    public static final PhyReg V0 = new PhyReg("$v0", 2, RegType.RET);
    public static final PhyReg V1 = new PhyReg("$v1", 3, RegType.SPECIAL);
    public static final PhyReg A0 = new PhyReg("$a0", 4, RegType.ARG);
    public static final PhyReg A1 = new PhyReg("$a1", 5, RegType.ARG);
    public static final PhyReg A2 = new PhyReg("$a2", 6, RegType.ARG);
    public static final PhyReg A3 = new PhyReg("$a3", 7, RegType.ARG);
    public static final PhyReg T0 = new PhyReg("$t0", 8, RegType.TEMP);
    public static final PhyReg T1 = new PhyReg("$t1", 9, RegType.TEMP);
    public static final PhyReg T2 = new PhyReg("$t2", 10, RegType.TEMP);
    public static final PhyReg T3 = new PhyReg("$t3", 11, RegType.TEMP);
    public static final PhyReg T4 = new PhyReg("$t4", 12, RegType.TEMP);
    public static final PhyReg T5 = new PhyReg("$t5", 13, RegType.TEMP);
    public static final PhyReg T6 = new PhyReg("$t6", 14, RegType.TEMP);
    public static final PhyReg T7 = new PhyReg("$t7", 15, RegType.TEMP);
    public static final PhyReg S0 = new PhyReg("$s0", 16, RegType.TEMP); // Original code treated S registers as TEMP?
    public static final PhyReg S1 = new PhyReg("$s1", 17, RegType.TEMP);
    public static final PhyReg S2 = new PhyReg("$s2", 18, RegType.TEMP);
    public static final PhyReg S3 = new PhyReg("$s3", 19, RegType.TEMP);
    public static final PhyReg S4 = new PhyReg("$s4", 20, RegType.TEMP);
    public static final PhyReg S5 = new PhyReg("$s5", 21, RegType.TEMP);
    public static final PhyReg S6 = new PhyReg("$s6", 22, RegType.TEMP);
    public static final PhyReg S7 = new PhyReg("$s7", 23, RegType.TEMP);
    public static final PhyReg T8 = new PhyReg("$t8", 24, RegType.TEMP);
    public static final PhyReg T9 = new PhyReg("$t9", 25, RegType.TEMP);
    public static final PhyReg K0 = new PhyReg("$k0", 26, RegType.SAVED);
    public static final PhyReg K1 = new PhyReg("$k1", 27, RegType.SAVED);
    public static final PhyReg GP = new PhyReg("$gp", 28, RegType.SAVED);
    public static final PhyReg SP = new PhyReg("$sp", 29, RegType.SPECIAL);
    public static final PhyReg FP = new PhyReg("$fp", 30, RegType.SAVED);
    public static final PhyReg RA = new PhyReg("$ra", 31, RegType.SPECIAL);
}
