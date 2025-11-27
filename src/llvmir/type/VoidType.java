package llvmir.type;

public class VoidType extends Type{
    public VoidType() {

    }
    @Override
    public int getSize() {
        return 0;
    }
    @Override
    public String toString() {
        return "void";
    }
}
