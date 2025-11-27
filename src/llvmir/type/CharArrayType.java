package llvmir.type;

public class CharArrayType extends Type{
    private int length;
    public CharArrayType() {
        this.length = 0;
    }
    public CharArrayType(int length){
        this.length=length;
    }
    @Override
    public int getSize(){
        return 4*length;
    }
    @Override
    public String toString(){
        return "["+length+" x i8]";
    }
}
