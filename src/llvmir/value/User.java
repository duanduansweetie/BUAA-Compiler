package llvmir.value;
import llvmir.type.Type;
import java.util.ArrayList;
import java.util.List;

public class User extends Value{
    public List<Value> operands;
    public User(String name, Type type) {
        super(name, type);
        this.operands = new ArrayList<>();
    }
    public void addOperand(Value value){
        this.operands.add(value);
    }

    public List<Value> getOperands() {
        return this.operands;
    }
    public Value getFirstOperand(){
        if(this.operands.size()>0){
            return this.operands.get(0);
        }
        //这里有无this
        return null;
    }
    public void setOperands(List<Value> operands){
        this.operands=operands;
    }
}
