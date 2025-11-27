package llvmir.value.instructions;
import llvmir.type.CharArrayType;
import llvmir.type.IntArrayType;
import llvmir.type.Integer32Type;
import llvmir.type.Integer8Type;
import llvmir.type.PointType;
import llvmir.value.Value;
import llvmir.value.structure.BasicBlock;
import llvmir.value.structure.Instruction;
public class GetElementPtr extends Instruction{
    // 只设计两个索引的情况，所以返回值类型一定是指针或者数组类型的儿子，即两种基本类型
    // 换句话说，使用该指令必将解引用
    // 传入的类型必定是一个指针类型
    Boolean isSingle = false;
    public GetElementPtr(Value index, Value array, BasicBlock basicBlock, Boolean isSingle) {
        // 根据数组类型生成GetElementPtr的类型
        super(null, array.getType(), basicBlock);
        operands.add(array);
        operands.add(index);
        if (this.getType() instanceof IntArrayType) {
            this.setType(new PointType(new Integer32Type()));
        } else if (this.getType() instanceof CharArrayType) {
            this.setType(new PointType(new Integer8Type()));
        }
        this.isSingle = isSingle;
    }
    public GetElementPtr(Value index, Value array, BasicBlock basicBlock) {
        // 根据数组类型生成GetElementPtr的类型
        super(null, ((PointType) array.getType()).getPoint(), basicBlock);
        operands.add(array);
        operands.add(index);
        if (this.getType() instanceof IntArrayType) {
            this.setType(new PointType(new Integer32Type()));
        } else if (this.getType() instanceof CharArrayType) {
            this.setType(new PointType(new Integer8Type()));
        }
    }
    @Override
    public String toString() {
        if (isSingle)
            return getPrintName() + " = getelementptr inbounds " + ((PointType) operands.get(0).getType()).getPoint() + ", "
                    + operands.get(0).getType() + " "
                    + operands.get(0).getPrintName() + ", i32 " + operands.get(1).getPrintName();
        else
            return getPrintName() + " = getelementptr inbounds " + ((PointType) operands.get(0).getType()).getPoint() + ", "
                    + operands.get(0).getType() + " "
                    + operands.get(0).getPrintName() + ", i32 0, i32 " + operands.get(1).getPrintName();
    }
}
