package mips.instructions;

import java.util.ArrayList;
import java.util.List;
import mips.structure.MipsFunc;
import mips.structure.MipsInstr;
import mips.value.GlobalLabel;
import mips.value.MipsImm;
import mips.value.MipsOperand;
import mips.value.PhyReg;
import mips.value.RegManager;
import mips.value.StackSlot;

public class MipsCall extends MipsInstr {
    private String name;
    private List<MipsOperand> args = new ArrayList<>();
    private int offset;
    private int stackOffset;
    private List<PhyReg> savedRegisters = new ArrayList<>();

    public MipsCall(String name, int offset, MipsFunc func) {
        this.name = name;
        this.offset = -offset + 16;
        this.stackOffset = func.getStackSize();
        this.savedRegisters = new ArrayList<>(func.getUsedRegisters());
    }

    public MipsCall(String name, List<MipsOperand> args, int offset, MipsFunc func) {
        this(name, offset, func);
        this.args = args;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        // Pass arguments
        for (int i = 0; i < args.size() && i < RegManager.getArgRegCount(); i++) {
            MipsOperand arg = args.get(i);
            PhyReg target = RegManager.getArgRegisters().get(i);
            if (arg instanceof PhyReg)
                sb.append("move ").append(target).append(", ").append(arg).append("\n");
            else if (arg instanceof MipsImm)
                sb.append("li ").append(target).append(", ").append(((MipsImm)arg).getValue()).append("\n");
            else if (arg instanceof StackSlot)
                sb.append("lw ").append(target).append(", ").append(arg).append("\n");
            else if (arg instanceof GlobalLabel)
                sb.append("la ").append(target).append(", ").append(arg).append("\n");
        }
        
        int currentOffset = offset;
        for (int i = RegManager.getArgRegCount(); i < args.size(); i++) {
            MipsOperand arg = args.get(i);
            if (arg instanceof PhyReg) {
                sb.append("sw ").append(arg).append(", ").append(currentOffset).append("($sp)\n");
            } else {
                if (arg instanceof MipsImm)
                    sb.append("li $v1, ").append(((MipsImm)arg).getValue()).append("\n");
                else if (arg instanceof StackSlot)
                    sb.append("lw $v1, ").append(arg).append("\n");
                else if (arg instanceof GlobalLabel)
                    sb.append("la $v1, ").append(arg).append("\n");
                
                sb.append("sw $v1, ").append(currentOffset).append("($sp)\n");
            }
            currentOffset += 4;
        }

        // Syscall or Function Call
        if (isSyscall(name)) {
            sb.append(getSyscallString(name));
        } else {
            // Save registers
            for (PhyReg register : savedRegisters) {
                sb.append("sw ").append(register).append(", ")
                        .append(stackOffset + RegManager.TEMP_REG_OFFSET.get(register))
                        .append("($sp)\n");
            }
            sb.append("jal ").append(name).append("\n");
            // Restore registers
            for (PhyReg register : savedRegisters) {
                sb.append("lw ").append(register).append(", ")
                        .append(stackOffset + RegManager.TEMP_REG_OFFSET.get(register))
                        .append("($sp)\n");
            }
        }
        return sb.toString();
    }

    private boolean isSyscall(String name) {
        return name.equals("putint") || name.equals("putch") || name.equals("getint")
                || name.equals("getchar") || name.equals("putstr");
    }

    private String getSyscallString(String name) {
        switch (name) {
            case "putint": return "li $v0, 1\nsyscall\n";
            case "putch": return "li $v0, 11\nsyscall\n";
            case "getint": return "li $v0, 5\nsyscall\n";
            case "getchar": return "li $v0, 12\nsyscall\n";
            case "putstr": return "li $v0, 4\nsyscall\n";
            default: return "";
        }
    }
}
