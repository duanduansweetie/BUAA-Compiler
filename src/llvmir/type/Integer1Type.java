package llvmir.type;

public class Integer1Type extends Type{
    public Integer1Type(){

    }
    @Override
    public  int getSize(){
        return 4;
    }
    @Override
    public String toString(){
        return "i1";
    }
}
