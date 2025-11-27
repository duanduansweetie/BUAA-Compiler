package llvmir.value.structure;
import llvmir.type.Type;
import llvmir.value.User;
import java.util.ArrayList;
import java.util.List;
public class Instruction extends User{
    protected Function parent;
    BasicBlock basicBlock;
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
