package mips.instructions;

import mips.structure.MipsInstr;
import mips.value.GlobalLabel;
import mips.value.MipsImm;
import mips.value.MipsOperand;
import mips.value.PhyReg;
import mips.value.StackSlot;

import java.util.HashMap;
import java.util.Map;

public class MipsALU extends MipsInstr {
    private MipsOperand dst;
    private MipsOperand op1;
    private MipsOperand op2;
    private ALUOp op;

    public enum ALUOp {
        ADDU, SUBU, MUL, DIV, REM, SLL, SRL, SLE, SGT, SNE, SEQ, SLT, SGE, AND, OR, XOR
    }

    private static final Map<String, ALUOp> OP_MAP = new HashMap<>();

    static {
        OP_MAP.put("+", ALUOp.ADDU);
        OP_MAP.put("-", ALUOp.SUBU);
        OP_MAP.put("*", ALUOp.MUL);
        OP_MAP.put("/", ALUOp.DIV);
        OP_MAP.put("%", ALUOp.REM);
        OP_MAP.put("<<", ALUOp.SLL);
        OP_MAP.put(">>", ALUOp.SRL);
        OP_MAP.put("<=", ALUOp.SLE);
        OP_MAP.put(">", ALUOp.SGT);
        OP_MAP.put("!=", ALUOp.SNE);
        OP_MAP.put("==", ALUOp.SEQ);
        OP_MAP.put("<", ALUOp.SLT);
        OP_MAP.put(">=", ALUOp.SGE);
        OP_MAP.put("&", ALUOp.AND);
        OP_MAP.put("|", ALUOp.OR);
        OP_MAP.put("^", ALUOp.XOR);
    }

    public MipsALU(MipsOperand dst, MipsOperand op1, MipsOperand op2, String opStr) {
        this.dst = dst;
        this.op1 = op1;
        this.op2 = op2;
        this.op = OP_MAP.get(opStr);
    }

    public MipsALU(MipsOperand dst, MipsOperand op1, MipsOperand op2, ALUOp op) {
        this.dst = dst;
        this.op1 = op1;
        this.op2 = op2;
        this.op = op;
    }

    @Override
    public String toString() {
        // Pre-process operands to handle StackSlot/GlobalLabel
        StringBuilder prefix = new StringBuilder();
        String o1 = op1.toString();
        String o2 = op2.toString();
        boolean op1IsReg = op1 instanceof PhyReg;
        boolean op2IsReg = op2 instanceof PhyReg;
        boolean op1IsImm = op1 instanceof MipsImm;
        boolean op2IsImm = op2 instanceof MipsImm;

        if (op1 instanceof StackSlot || op1 instanceof GlobalLabel) {
            prefix.append(loadOp(op1, "$v1"));
            o1 = "$v1";
            op1IsReg = true;
            op1IsImm = false;
        }
        
        if (op2 instanceof StackSlot || op2 instanceof GlobalLabel) {
            String reg = o1.equals("$v1") ? "$at" : "$v1";
            prefix.append(loadOp(op2, reg));
            o2 = reg;
            op2IsReg = true;
            op2IsImm = false;
        }

        String targetDst = dst.toString();
        boolean dstIsMem = dst instanceof StackSlot || dst instanceof GlobalLabel;
        if (dstIsMem) {
            targetDst = "$v1";
        }

        String instr = "";
        switch (op) {
            case ADDU: instr = toAddString(targetDst, o1, o2, op1IsImm, op2IsImm); break;
            case SUBU: instr = toSubString(targetDst, o1, o2, op1IsImm, op2IsImm); break;
            case MUL: instr = toMulString(targetDst, o1, o2, op1IsImm, op2IsImm); break;
            case DIV: instr = toDivString(targetDst, o1, o2, op1IsImm, op2IsImm, true); break;
            case REM: instr = toDivString(targetDst, o1, o2, op1IsImm, op2IsImm, false); break;
            case SLL: instr = toSllString(targetDst, o1, o2, op1IsImm, op2IsImm); break;
            case SRL: instr = "srl " + targetDst + ", " + o1 + ", " + (op2IsImm ? ((MipsImm)op2).getValue() : o2); break;
            case SLE: instr = toCmpString("sle", targetDst, o1, o2, op1IsImm, op2IsImm); break;
            case SGT: instr = toCmpString("sgt", targetDst, o1, o2, op1IsImm, op2IsImm); break;
            case SNE: instr = toCmpString("sne", targetDst, o1, o2, op1IsImm, op2IsImm); break;
            case SEQ: instr = toCmpString("seq", targetDst, o1, o2, op1IsImm, op2IsImm); break;
            case SLT: instr = toSltString(targetDst, o1, o2, op1IsImm, op2IsImm); break;
            case SGE: instr = toCmpString("sge", targetDst, o1, o2, op1IsImm, op2IsImm); break;
            case AND: instr = toLogicString("and", "andi", targetDst, o1, o2, op1IsImm, op2IsImm); break;
            case OR: instr = toLogicString("or", "ori", targetDst, o1, o2, op1IsImm, op2IsImm); break;
            case XOR: instr = toLogicString("xor", "xori", targetDst, o1, o2, op1IsImm, op2IsImm); break;
        }
        
        String result = prefix.toString() + instr;
        if (dstIsMem) {
            result += "\n" + storeOp(dst, targetDst);
        }
        return result;
    }

    private String storeOp(MipsOperand op, String reg) {
        if (op instanceof StackSlot) return "sw " + reg + ", " + op;
        if (op instanceof GlobalLabel) return "sw " + reg + ", " + op;
        return "";
    }

    private String loadOp(MipsOperand op, String reg) {
        if (op instanceof StackSlot) return "lw " + reg + ", " + op + "\n";
        if (op instanceof GlobalLabel) return "la " + reg + ", " + op + "\n";
        return "";
    }

    private String toAddString(String dst, String o1, String o2, boolean op1IsImm, boolean op2IsImm) {
        if (op1IsImm && op2IsImm) {
            int value = ((MipsImm) op1).getValue() + ((MipsImm) op2).getValue();
            return "li " + dst + ", " + value;
        } else if (op1IsImm) {
            return "addiu " + dst + ", " + o2 + ", " + o1;
        } else if (op2IsImm) {
            return "addiu " + dst + ", " + o1 + ", " + o2;
        } else {
            return "addu " + dst + ", " + o1 + ", " + o2;
        }
    }

    private String toSubString(String dst, String o1, String o2, boolean op1IsImm, boolean op2IsImm) {
        if (op1IsImm && op2IsImm) {
            int value = ((MipsImm) op1).getValue() - ((MipsImm) op2).getValue();
            return "li " + dst + ", " + value;
        } else if (op1IsImm) {
            if (((MipsImm) op1).getValue() == 0) {
                return "subu " + dst + ", $zero, " + o2;
            }
            return "subu " + o2 + ", $zero, " + o2 + "\naddiu " + dst + ", " + o2 + ", " + ((MipsImm) op1).getValue();
        } else if (op2IsImm) {
            return "addiu " + dst + ", " + o1 + ", " + (-((MipsImm) op2).getValue());
        } else {
            return "subu " + dst + ", " + o1 + ", " + o2;
        }
    }

    private String toMulString(String dst, String o1, String o2, boolean op1IsImm, boolean op2IsImm) {
        if (op1IsImm && op2IsImm) {
            int value = ((MipsImm) op1).getValue() * ((MipsImm) op2).getValue();
            return "li " + dst + ", " + value;
        }
        if (op2IsImm) {
            int val = ((MipsImm)op2).getValue();
            if (val > 0 && (val & (val - 1)) == 0) {
                int shift = Integer.numberOfTrailingZeros(val);
                return "sll " + dst + ", " + o1 + ", " + shift;
            }
        }
        if (op1IsImm) {
            int val = ((MipsImm)op1).getValue();
            if (val > 0 && (val & (val - 1)) == 0) {
                int shift = Integer.numberOfTrailingZeros(val);
                return "sll " + dst + ", " + o2 + ", " + shift;
            }
        }

        StringBuilder sb = new StringBuilder();
        String finalO1 = o1;
        String finalO2 = o2;
        
        if (op1IsImm) {
            sb.append("li $v1, ").append(o1).append("\n");
            finalO1 = "$v1";
        }
        if (op2IsImm) {
            String reg = finalO1.equals("$v1") ? "$at" : "$v1";
            sb.append("li ").append(reg).append(", ").append(o2).append("\n");
            finalO2 = reg;
        }
        sb.append("mul ").append(dst).append(", ").append(finalO1).append(", ").append(finalO2);
        return sb.toString();
    }

    private String toSllString(String dst, String o1, String o2, boolean op1IsImm, boolean op2IsImm) {
        if (op1IsImm && op2IsImm) {
            int value = ((MipsImm) op1).getValue() << ((MipsImm) op2).getValue();
            return "li " + dst + ", " + value;
        } else if (op2IsImm) {
            return "sll " + dst + ", " + o1 + ", " + ((MipsImm) op2).getValue();
        } else {
            return "move $v1, " + o2 + "\nsllv " + dst + ", " + o1 + ", $v1";
        }
    }

    private String toDivString(String dst, String o1, String o2, boolean op1IsImm, boolean op2IsImm, boolean isDiv) {
        if (op1IsImm && op2IsImm) {
            int v1 = ((MipsImm) op1).getValue();
            int v2 = ((MipsImm) op2).getValue();
            if (v2 != 0) {
                int value = isDiv ? (v1 / v2) : (v1 % v2);
                return "li " + dst + ", " + value;
            }
        }
        if (isDiv && op2IsImm) {
            int val = ((MipsImm)op2).getValue();
            if (val > 0 && (val & (val - 1)) == 0) {
                int k = Integer.numberOfTrailingZeros(val);
                if (k == 0) {
                    if (op1IsImm) return "li " + dst + ", " + o1;
                    return "move " + dst + ", " + o1;
                }
                
                StringBuilder sb = new StringBuilder();
                String temp = "$at";
                if (o1.equals("$at")) temp = "$v1";
                
                if (op1IsImm) {
                    sb.append("li ").append(temp).append(", ").append(o1).append("\n");
                    sb.append("sra ").append(temp).append(", ").append(temp).append(", 31\n");
                    sb.append("srl ").append(temp).append(", ").append(temp).append(", ").append(32 - k).append("\n");
                    sb.append("li $v1, ").append(o1).append("\n");
                    sb.append("addu ").append(temp).append(", $v1, ").append(temp).append("\n");
                } else {
                    sb.append("sra ").append(temp).append(", ").append(o1).append(", 31\n");
                    sb.append("srl ").append(temp).append(", ").append(temp).append(", ").append(32 - k).append("\n");
                    sb.append("addu ").append(temp).append(", ").append(o1).append(", ").append(temp).append("\n");
                }
                sb.append("sra ").append(dst).append(", ").append(temp).append(", ").append(k);
                return sb.toString();
            }
        }

        StringBuilder sb = new StringBuilder();
        String finalO1 = o1;
        String finalO2 = o2;
        
        if (op1IsImm) {
            sb.append("li $v1, ").append(o1).append("\n");
            finalO1 = "$v1";
        }
        if (op2IsImm) {
            String reg = finalO1.equals("$v1") ? "$at" : "$v1";
            sb.append("li ").append(reg).append(", ").append(o2).append("\n");
            finalO2 = reg;
        }
        
        sb.append("div ").append(finalO1).append(", ").append(finalO2).append("\n");
        sb.append(isDiv ? "mflo " : "mfhi ").append(dst);
        return sb.toString();
    }

    private String toSltString(String dst, String o1, String o2, boolean op1IsImm, boolean op2IsImm) {
        if (op1IsImm && op2IsImm) {
            int val = (((MipsImm)op1).getValue() < ((MipsImm)op2).getValue()) ? 1 : 0;
            return "li " + dst + ", " + val;
        } else if (op2IsImm) {
            return "slti " + dst + ", " + o1 + ", " + o2;
        } else if (op1IsImm) {
            return "li $v1, " + o1 + "\nslt " + dst + ", $v1, " + o2;
        } else {
            return "slt " + dst + ", " + o1 + ", " + o2;
        }
    }

    private String toCmpString(String op, String dst, String o1, String o2, boolean op1IsImm, boolean op2IsImm) {
        StringBuilder sb = new StringBuilder();
        String finalO1 = o1;
        String finalO2 = o2;
        
        if (op1IsImm) {
            sb.append("li $v1, ").append(o1).append("\n");
            finalO1 = "$v1";
        }
        if (op2IsImm) {
            String reg = finalO1.equals("$v1") ? "$at" : "$v1";
            sb.append("li ").append(reg).append(", ").append(o2).append("\n");
            finalO2 = reg;
        }
        sb.append(op).append(" ").append(dst).append(", ").append(finalO1).append(", ").append(finalO2);
        return sb.toString();
    }

    private String toLogicString(String op, String opi, String dst, String o1, String o2, boolean op1IsImm, boolean op2IsImm) {
        if (op1IsImm && op2IsImm) {
            int v1 = ((MipsImm)op1).getValue();
            int v2 = ((MipsImm)op2).getValue();
            int res = 0;
            if (op.equals("and")) res = v1 & v2;
            else if (op.equals("or")) res = v1 | v2;
            else if (op.equals("xor")) res = v1 ^ v2;
            return "li " + dst + ", " + res;
        } else if (op2IsImm) {
            return opi + " " + dst + ", " + o1 + ", " + o2;
        } else if (op1IsImm) {
            return opi + " " + dst + ", " + o2 + ", " + o1;
        } else {
            return op + " " + dst + ", " + o1 + ", " + o2;
        }
    }
}
