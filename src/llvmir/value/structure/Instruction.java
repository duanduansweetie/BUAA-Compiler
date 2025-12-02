package llvmir.value.structure;
import java.util.ArrayList;
import java.util.List;
import llvmir.type.Type;
import llvmir.value.User;
import mips.value.PhyReg;

public class Instruction extends User{
    protected Function parent;
    BasicBlock basicBlock;
    public List<PhyReg> readRegisters = new ArrayList<>(); // 读取的寄存器
    public List<PhyReg> writeRegisters = new ArrayList<>(); // 写入的寄存器

    public Instruction(String name,Type type,BasicBlock basicBlock){
        super(name,type);
        this.basicBlock = basicBlock;
        this.parent=basicBlock.getParent();
    }
    public Function getParent() {
        return parent;
    }

    public BasicBlock getBasicBlock() {
        return basicBlock;
    }

}
