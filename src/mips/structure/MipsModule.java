package mips.structure;
import java.util.ArrayList;
import java.util.List;
public class MipsModule {
    public MipsModule() {
    }
    public List<MipsGV> globalVariables = new ArrayList<>();
    public List<MipsFunc> functions = new ArrayList<>();
    public MipsFunc main;
    public MipsFunc getFunction(String name){
        for(MipsFunc func:functions){
            if(func.name.equals(name)){
                return func;
            }
        }
        return null;
    }
    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder();
        sb.append(".data\n");
        for(MipsGV gv:globalVariables){
            sb.append(gv.toString()).append("\n");
        }
        sb.append(".text\n");
        sb.append(main).append("\n\n");
        for(MipsFunc func:functions){
            sb.append(func.toString()).append("\n");
        }
        return sb.toString();
    }
}
