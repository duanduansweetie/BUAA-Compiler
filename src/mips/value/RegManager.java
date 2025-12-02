package mips.value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import mips.structure.MipsFunc;

public class RegManager {
    private static final Map<Integer, PhyReg> REG_MAP = new HashMap<>();
    private static final List<PhyReg> ARG_REGS = new ArrayList<>();
    private static final List<PhyReg> TEMP_REGS = new ArrayList<>();
    private static final List<PhyReg> SAVED_REGS = new ArrayList<>();
    public static final Map<PhyReg, Integer> TEMP_REG_OFFSET = new HashMap<>();

    static {
        register(PhyReg.ZERO); register(PhyReg.AT); register(PhyReg.V0); register(PhyReg.V1);
        register(PhyReg.A0); register(PhyReg.A1); register(PhyReg.A2); register(PhyReg.A3);
        register(PhyReg.T0); register(PhyReg.T1); register(PhyReg.T2); register(PhyReg.T3);
        register(PhyReg.T4); register(PhyReg.T5); register(PhyReg.T6); register(PhyReg.T7);
        register(PhyReg.S0); register(PhyReg.S1); register(PhyReg.S2); register(PhyReg.S3);
        register(PhyReg.S4); register(PhyReg.S5); register(PhyReg.S6); register(PhyReg.S7);
        register(PhyReg.T8); register(PhyReg.T9);
        register(PhyReg.K0); register(PhyReg.K1); register(PhyReg.GP); register(PhyReg.SP);
        register(PhyReg.FP); register(PhyReg.RA);

        TEMP_REG_OFFSET.put(PhyReg.T0, -8);
        TEMP_REG_OFFSET.put(PhyReg.T1, -12);
        TEMP_REG_OFFSET.put(PhyReg.T2, -16);
        TEMP_REG_OFFSET.put(PhyReg.T3, -20);
        TEMP_REG_OFFSET.put(PhyReg.T4, -24);
        TEMP_REG_OFFSET.put(PhyReg.T5, -28);
        TEMP_REG_OFFSET.put(PhyReg.T6, -32);
        TEMP_REG_OFFSET.put(PhyReg.T7, -36);
        TEMP_REG_OFFSET.put(PhyReg.T8, -40);
        TEMP_REG_OFFSET.put(PhyReg.T9, -44);
        TEMP_REG_OFFSET.put(PhyReg.S0, -48);
        TEMP_REG_OFFSET.put(PhyReg.S1, -52);
        TEMP_REG_OFFSET.put(PhyReg.S2, -56);
        TEMP_REG_OFFSET.put(PhyReg.S3, -60);
        TEMP_REG_OFFSET.put(PhyReg.S4, -64);
        TEMP_REG_OFFSET.put(PhyReg.S5, -68);
        TEMP_REG_OFFSET.put(PhyReg.S6, -72);
        TEMP_REG_OFFSET.put(PhyReg.S7, -76);
    }

    private static void register(PhyReg reg) {
        REG_MAP.put(reg.getIndex(), reg);
        if (reg.getType() == PhyReg.RegType.TEMP) TEMP_REGS.add(reg);
        else if (reg.getType() == PhyReg.RegType.SAVED) SAVED_REGS.add(reg);
        else if (reg.getType() == PhyReg.RegType.ARG) ARG_REGS.add(reg);
    }

    public static PhyReg getTempRegister(MipsFunc func) {
        if (TEMP_REGS.isEmpty()) throw new RuntimeException("Out of temporary registers");
        PhyReg reg = TEMP_REGS.remove(0);
        func.addRegister(reg);
        return reg;
    }

    public static void releaseTempRegisters(MipsFunc func) {
        TEMP_REGS.clear();
        func.getUsedRegisters().clear();
        for (PhyReg reg : REG_MAP.values()) {
            if (reg.getType() == PhyReg.RegType.TEMP) TEMP_REGS.add(reg);
        }
    }
    
    public static void releaseTempRegister(PhyReg reg, MipsFunc func) {
        TEMP_REGS.add(reg);
        func.removeRegister(reg);
    }

    public static PhyReg getArgRegister() {
        if (ARG_REGS.isEmpty()) return null;
        return ARG_REGS.remove(0);
    }

    public static void releaseArgRegisters() {
        ARG_REGS.clear();
        for (PhyReg reg : REG_MAP.values()) {
            if (reg.getType() == PhyReg.RegType.ARG) ARG_REGS.add(reg);
        }
    }

    public static int getArgRegCount() { return ARG_REGS.size(); }
    public static List<PhyReg> getArgRegisters() { return ARG_REGS; }
}
