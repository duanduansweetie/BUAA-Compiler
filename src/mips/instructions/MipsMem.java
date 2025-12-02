package mips.instructions;

import mips.structure.MipsInstr;
import mips.value.GlobalLabel;
import mips.value.MipsOperand;
import mips.value.PhyReg;
import mips.value.StackSlot;

public class MipsMem extends MipsInstr {
    private MipsOperand val; // dst for load, src for store
    private MipsOperand addr;
    private MemOp op;

    public enum MemOp {
        LW, SW, LI, LA
    }

    public MipsMem(MipsOperand val, MipsOperand addr, MemOp op) {
        this.val = val;
        this.addr = addr;
        this.op = op;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String targetVal = val.toString();
        boolean valIsMem = val instanceof StackSlot || val instanceof GlobalLabel;
        
        // For SW, val is source. For others, val is destination.
        if (op == MemOp.SW) {
            if (valIsMem) {
                sb.append("lw $v1, ").append(val).append("\n");
                targetVal = "$v1";
            }
        } else {
            // LW, LI, LA: val is destination
            if (valIsMem) {
                targetVal = "$v1";
            }
        }

        String instr = "";
        switch (op) {
            case LW:
                if (addr instanceof PhyReg)
                    instr = "lw " + targetVal + ", 0(" + addr + ")";
                else
                    instr = "lw " + targetVal + ", " + addr;
                break;
            case SW:
                if (val.equals(addr)) return ""; 
                if (targetVal.equals(addr.toString())) return "";
                
                if (addr instanceof PhyReg)
                    instr = "sw " + targetVal + ", 0(" + addr + ")";
                else
                    instr = "sw " + targetVal + ", " + addr;
                break;
            case LI:
                instr = "li " + targetVal + ", " + addr;
                break;
            case LA:
                if (addr instanceof PhyReg)
                    instr = "la " + targetVal + ", 0(" + addr + ")";
                else
                    instr = "la " + targetVal + ", " + addr;
                break;
            default:
                return "";
        }
        
        sb.append(instr);
        
        // If val was destination and is memory, store the result
        if (op != MemOp.SW && valIsMem) {
            sb.append("\nsw ").append(targetVal).append(", ").append(val);
        }
        
        return sb.toString();
    }
}
