package llvmir.value.instructions;
import java.util.List;
import llvmir.type.PointType;
import llvmir.type.Type;
import llvmir.value.structure.BasicBlock;
import llvmir.value.structure.Instruction;
public class Alloca extends Instruction{
    //作用：函数调用指令，调用某个函数并返回值（若有）。
    private Type allocType;
    private boolean isParameter = false;
    // 使用alloca指令时，需要指定分配的类型，即分配的空间的类型
    // alloca的类型是指向分配的空间的指针
    public Alloca(Type allocType, BasicBlock basicBlock) {
        super(null, new PointType(allocType), basicBlock);
        this.allocType = allocType;
    }
    public Alloca(Type allocType, BasicBlock basicBlock, boolean isParameter) {
        super(null, new PointType(allocType), basicBlock);
        this.allocType = allocType;
        this.isParameter = isParameter;
    }
    public boolean isParameter(){
        return isParameter;
    }
    public Type getAllocType(){
        return allocType;
    }
    @Override
    public String toString() {
        return name + " = alloca " + allocType;
    }
}
