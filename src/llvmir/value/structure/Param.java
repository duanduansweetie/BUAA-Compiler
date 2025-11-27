package llvmir.value.structure;
import llvmir.type.Type;
import llvmir.value.Value;
public class Param extends Value{

    public Param(String name,Type type){
        super(name,type);
    }
    public void setIndex(int index){
        this.setName("%zty" + index);
    }

    @Override
    public String toString(){
        return this.getType() + " " + this.getName();
    }
}
