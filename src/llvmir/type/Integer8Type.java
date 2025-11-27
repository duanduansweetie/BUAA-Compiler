package llvmir.type;

public class Integer8Type extends Type{
    public Integer8Type(){

    }
    @Override
    public  int getSize(){
        return 4;
    }
    @Override
    public String toString(){
        return "i8";
    }
}
