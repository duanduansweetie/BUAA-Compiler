package llvmir.type;

public abstract class Type {
    public abstract int getSize();
    public boolean isCharArrayType() {
        return this instanceof CharArrayType;
    }
    public boolean isIntArrayType() {
        return this instanceof IntArrayType;
    }
    public boolean isInteger1Type() {
        return this instanceof Integer1Type;
    }

    public boolean isInteger8Type() {
        return this instanceof Integer8Type;
    }

    public boolean isInteger32Type() {
        return this instanceof Integer32Type;
    }
    public boolean isVoidType(){
        return this instanceof VoidType;
    }
    public boolean isPointerType() {
        return this instanceof PointType;
    }
}
