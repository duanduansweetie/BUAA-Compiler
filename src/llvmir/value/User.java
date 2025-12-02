package llvmir.value;
import java.util.ArrayList;
import java.util.List;
import llvmir.type.Type;

public class User extends Value{
    public List<Value> operands;
    public User(String name, Type type) {
        super(name, type);
        this.operands = new ArrayList<>();
    }
    public void addOperand(Value value){
        this.operands.add(value);
        if (value != null) {
            value.addUser(this);
        }
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
        if (operands != null) {
            for (Value value : operands) {
                if (value != null) {
                    value.addUser(this);
                }
            }
        }
    }
}
