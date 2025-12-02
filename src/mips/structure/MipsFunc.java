package mips.structure;
import java.util.ArrayList;
import java.util.List;
import mips.value.PhyReg;

public class MipsFunc {
    private int stackSize = 0; // 栈大小
    public String name; // 函数名
    public List<MipsBlock> blocks = new ArrayList<>();
    private List<PhyReg> usedRegisters = new ArrayList<>();// 使用的寄存器

    public MipsFunc(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getStackSize() {
        return stackSize;
    }

    public void setStackSize(int stackSize){
        this.stackSize=stackSize;
    }

    public void addRegister(PhyReg register) {
        if (!usedRegisters.contains(register)) {
            usedRegisters.add(register);
        }
    }

    public void removeRegister(PhyReg register) {
        usedRegisters.remove(register);
    }

    public List<PhyReg> getUsedRegisters() {
        return usedRegisters;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name + ":\n");
        // 栈大小
        sb.append("addi $sp, $sp, -").append(stackSize).append("\n");
        sb.append("sw $ra, ").append(stackSize - 4).append("($sp)\n");
        for (MipsBlock block : blocks) {
            sb.append(block);
        }
        return sb.toString();
    }
}
