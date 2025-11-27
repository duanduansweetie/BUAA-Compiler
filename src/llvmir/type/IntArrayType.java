package llvmir.type;

public class IntArrayType extends Type{
    private int length;
    public IntArrayType(int length){
        this.length=length;
    }
    @Override
    public int getSize(){
        return 4*length;
    }
    @Override
    public String toString(){
        return "["+length+" x i32]";
    }
}
