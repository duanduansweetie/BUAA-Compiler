package llvmir.value.constants;

import llvmir.type.Integer32Type;
public class ConstantInt extends Constant{
    private int value;

    public ConstantInt(int value){
        super(Integer.toString(value), new Integer32Type());
        this.value = value;
    }

    public int getValue(){
        return value;
    }

    @Override
    public String toString(){
        return " " + value;
    }
}
