package symbol;
import java.util.List;
public class ArraySymbol extends Symbol{
    private final int size;
    private final SymbolKind kind;
    boolean canCal=false;
    List<Integer> valueList;
    public ArraySymbol(String name, int size, SymbolType type,SymbolKind kind,int scopelevel,int scopenum) {
        super(name,type,scopelevel,scopenum);
        this.size = size;
        this.kind = kind;
    }
    public ArraySymbol(String name, int size, SymbolType type,SymbolKind kind,int scopelevel,int scopenum,List<Integer> valueList) {
        super(name,type,scopelevel,scopenum);
        this.size = size;
        this.kind = kind;
        this.valueList=valueList;
        if(kind== SymbolKind.CONSTANT){
            this.canCal=true;
        }
    }
    public SymbolKind getKind() {
        return kind;
    }
    public int getSize() {
        return size;
    }
    public boolean isCanCal() {
        return canCal;
    }
    public String getTypeName(){
        if(kind==SymbolKind.CONSTANT&&getType()==SymbolType.INT) {
            return "ConstIntArray";
        }
        else if(kind==SymbolKind.VARIABLE&&getType()==SymbolType.INT){
            return "IntArray";
        }
        else if(kind==SymbolKind.STATIC&&getType()==SymbolType.INT){
            return "StaticIntArray";
        }
        else{
            return "UnknownArrayType";
        }
    }
    public int getValue(int index){
        return valueList.get(index);
    }
}
