package mips.instructions;

import java.util.HashMap;
import java.util.Map;
import mips.structure.MipsInstr;
import mips.value.GlobalLabel;
import mips.value.MipsImm;
import mips.value.MipsOperand;
import mips.value.StackSlot;

public class MipsBranch extends MipsInstr {
    private MipsOperand op1;
    private MipsOperand op2;
    private String label1;
    private String label2;
    private BranchType type;

    public enum BranchType {
        BEQ, BNE, BLE, BGT, BLT, BGE
    }

    private static final Map<String, BranchType> OP_MAP = new HashMap<>();

    static {
        OP_MAP.put("==", BranchType.BEQ);
        OP_MAP.put("!=", BranchType.BNE);
        OP_MAP.put("<=", BranchType.BLE);
        OP_MAP.put(">=", BranchType.BGT);
        OP_MAP.put("<", BranchType.BLT);
        OP_MAP.put(">", BranchType.BGE);
    }

    public MipsBranch(String opStr, MipsOperand op1, MipsOperand op2, String label1, String label2) {
        this.op1 = op1;
        this.op2 = op2;
        this.label1 = label1;
        this.label2 = label2;
        this.type = OP_MAP.get(opStr);
    }

    @Override
    public String toString() {
        StringBuilder prefix = new StringBuilder();
        String o1 = op1.toString();
        String o2 = op2.toString();
        
        // Load op1 if needed
        if (op1 instanceof StackSlot || op1 instanceof GlobalLabel || op1 instanceof MipsImm) {
            prefix.append(loadOp(op1, "$v1"));
            o1 = "$v1";
        }
        
        // Load op2 if needed
        if (op2 instanceof StackSlot || op2 instanceof GlobalLabel || op2 instanceof MipsImm) {
            String reg = o1.equals("$v1") ? "$at" : "$v1";
            prefix.append(loadOp(op2, reg));
            o2 = reg;
        }

        String branchInstr = "";
        switch (type) {
            case BEQ: branchInstr = "beq " + o1 + ", " + o2 + ", " + label1; break;
            case BNE: branchInstr = "bne " + o1 + ", " + o2 + ", " + label1; break;
            case BLE: branchInstr = "ble " + o1 + ", " + o2 + ", " + label1; break;
            case BGT: branchInstr = "bgt " + o1 + ", " + o2 + ", " + label1; break;
            case BLT: branchInstr = "blt " + o1 + ", " + o2 + ", " + label1; break;
            case BGE: branchInstr = "bge " + o1 + ", " + o2 + ", " + label1; break;
        }
        
        return prefix.toString() + branchInstr + "\nj " + label2;
    }

    private String loadOp(MipsOperand op, String reg) {
        if (op instanceof StackSlot) return "lw " + reg + ", " + op + "\n";
        if (op instanceof GlobalLabel) return "la " + reg + ", " + op + "\n";
        if (op instanceof MipsImm) return "li " + reg + ", " + ((MipsImm)op).getValue() + "\n";
        return "";
    }
}
