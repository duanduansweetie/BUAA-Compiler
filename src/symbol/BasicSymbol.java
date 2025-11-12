package symbol;

public class BasicSymbol extends Symbol{
    private final SymbolKind kind;
    private int valueNum;
    private boolean canCalculate=false;
    public BasicSymbol(String name, SymbolType type, SymbolKind kind, int scopelevel, int scopenum) {
        super(name,type,scopelevel,scopenum);
        this.kind = kind;
    }
    public BasicSymbol(String name, SymbolType type, SymbolKind kind, int scopelevel, int scopenum, int valueNum) {
        super(name,type,scopelevel,scopenum);
        this.kind = kind;
        this.valueNum = valueNum;
        if(kind== SymbolKind.CONSTANT){
            this.canCalculate=true;
        }
    }
    public SymbolKind getKind(){
        return kind;
    }
    public int getValueNum(){
        return valueNum;
    }
    public boolean isCanCalculate(){
        return canCalculate;
    }
    public void setValue(int valueNum){
        this.valueNum=valueNum;
        canCalculate = true;
    }
    public String getTypeName(){
        if(kind==SymbolKind.CONSTANT&&getType()==SymbolType.INT) {
            return "ConstInt";
        }
        else if(kind==SymbolKind.VARIABLE&&getType()==SymbolType.INT){
            return "Int";
        }
        else if(kind==SymbolKind.STATIC&&getType()==SymbolType.INT){
            return "StaticInt";
        }
        else{
            return "UnknownType";
        }
    }
}
