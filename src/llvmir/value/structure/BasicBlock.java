package llvmir.value.structure;
import llvmir.value.Value;
import java.util.ArrayList;
import java.util.List;
public class BasicBlock extends Value{
    private List<Instruction> instructions;
    private Function parent;
    public BasicBlock(Function parent){
        super(null,null);
        this.parent=parent;
        parent.addBasicBlock(this);
    }
    public List<Instruction> getInstructions(){
        return instructions;
    }
    public Function getParent(){
        return parent;
    }
    public void setParent(Function parent){
        this.parent=parent;
    }
    public void addInstruction(Instruction instruction){
        if(instructions==null){
            instructions=new ArrayList<>();
        }
        instructions.add(instruction);
    }
    public void replaceInstruction(Instruction oldInst, List<Instruction> newInst){
        int index=instructions.indexOf(oldInst);
       instructions.remove(index);
       instructions.addAll(index,newInst);
       updateUsers(oldInst,newInst.get(newInst.size()-1));
    }
    public void updateUsers(Instruction oldInst, Value newInsts) {
        Function function = oldInst.getParent();
        for (BasicBlock bb : function.getBasicBlocks())
            for (Instruction inst : bb.getInstructions())
                for (int i = 0; i < inst.getOperands().size(); i++)
                    if (inst.getOperands().get(i) == oldInst)
                        inst.getOperands().set(i, newInsts);
    }
    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder();
        sb.append(this.getName()+":\n");
        if(instructions==null){
            return sb.toString();
        }
        for(Instruction inst:instructions){
            sb.append("  "+inst.toString()+"\n");
        }
        return sb.toString();
    }

}
