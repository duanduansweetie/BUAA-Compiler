package llvmir.type;

public class Integer32Type extends Type{
    public Integer32Type(){

    }
    @Override
    public  int getSize(){
        return 4;
    }
    @Override
    public String toString(){
        return "i32";
    }
}