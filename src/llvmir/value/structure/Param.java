package llvmir.value.structure;
import llvmir.type.Type;
import llvmir.value.Value;
public class Param extends Value{
    int index;
    String name;
    public Param(String name,Type type){
        super(name,type);
        this.name=name;
    }
    public void setIndex(int index){
        this.index=index;
        name = "%zty" + index;
    }
    public String getName(){
        return name;
    }
    @Override
    public String toString(){
        return this.getType() + " %zty" + this.index;
    }
}
