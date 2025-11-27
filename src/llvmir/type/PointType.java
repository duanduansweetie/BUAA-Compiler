package llvmir.type;
// 指针类型，gv,alloca,getElementPtr 指令都是指针类型
public class PointType extends Type{
    private Type point;
    public PointType(Type type) {
        this.point = type;
    }
    public Type getPoint() {
        return point;
    }
    @Override
    public int getSize() {
        return 4;
    }
    @Override
    public String toString() {
        return point.toString() + "*";
    }

}
