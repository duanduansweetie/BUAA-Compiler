package llvmir.value.structure;
import llvmir.type.Type;
import llvmir.type.VoidType;
import llvmir.value.Value;
import llvmir.value.instructions.Call;
import java.util.ArrayList;
import java.util.List;
public class Function extends Value{
    public int index = 0;
    List<Param> params = new ArrayList<>();
    List<BasicBlock> basicBlocks =new ArrayList<>();
    public Function(String name, Type type) {
        super("@" + name, type);
    }
    public Function(String name, Type type, List<Param> params) {
        super("@" + name, type);
        this.params = params;
        for (Param param : params) {
            param.setIndex(index++);
        }
    }
    public List<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }
    public List<Param> getParams() {
        return params;
    }

    public void addBasicBlock(BasicBlock basicBlock) {
        basicBlock.setParent(this);
        basicBlocks.add(basicBlock);
    }

    @Override
    public String toString(){
        setIndex();
        StringBuilder sb = new StringBuilder();
        sb.append(("define dso_local ") + this.getType() + " " + this.getName() + "("
                + this.showParams() + ") {\n");
        for (BasicBlock bb : basicBlocks) {
            sb.append(bb);
        }
        sb.append("}\n");
        return sb.toString();
    }
    // 为每个基本块的每条指令设置index
    public void setIndex(){
        for(int i=0;i<basicBlocks.size();i++){
            basicBlocks.get(i).setName("zty" + index++);
            for(int j=0;j<basicBlocks.get(i).getInstructions().size();j++){
                Value value = basicBlocks.get(i).getInstructions().get(j);
                String name=value.getName();
                // 如果是函数调用指令，且返回值为void，则不设置name
                if (value instanceof Call) {
                    Call call = (Call) value;
                    Function function = (Function) call.operands.get(0);
                    if (function.getType() instanceof VoidType) {
                        continue;
                    }
                }
                if(name==null);{
                    value.setName("zty" + index++);

                }            }
        }
    }
    public String showParams() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i));
            if (i != params.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

}
