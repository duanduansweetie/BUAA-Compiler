package llvmir.value.instructions;
import llvmir.value.Value;
import llvmir.value.structure.BasicBlock;
import llvmir.value.structure.Function;
import llvmir.value.structure.Instruction;
import java.util.List;
public class Call extends Instruction{
    // 无参数的函数调用
    public Call(Value function, BasicBlock basicBlock) {
        super(null, function.getType(), basicBlock);
        this.operands.add(function);
    }

    // 单个参数的函数调用
    public Call(Value function, Value arg, BasicBlock basicBlock) {
        super(null, function.getType(), basicBlock);
        this.operands.add(function);
        this.operands.add(arg);
    }
    public Call(Value function,List<Value> args,BasicBlock basicblock){
        super(null, function.getType(), basicblock);
        this.operands.add(function);
        this.operands.addAll(args);
    }
    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder();
        Function func=(Function)operands.get(0);
        if(!(func.getType().isVoidType())){
            sb.append(name+"=");
        }
        sb.append("call "+this.getType()+" ");
        sb.append(func.getName()).append("(");
        for(int i=1;i<operands.size();i++){
            sb.append(operands.get(i).getType()).append(" ").append(operands.get(i).getName());
            if(i!=operands.size()-1){
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

}
