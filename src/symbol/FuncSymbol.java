package symbol;
import java.util.List;
public class FuncSymbol extends Symbol{
    private final int paramNum;
    private final List<Symbol> paramList;
    public FuncSymbol(String name, SymbolType type, int paramNum, List<Symbol> paramList,int scopelevel,int scopenum) {
        super(name, type, scopelevel,scopenum);
        this.paramNum = paramNum;
        this.paramList = paramList;
    }
    public int getParamNum() {
        return paramNum;
    }
    public List<Symbol> getParamList() {
        return paramList;
    }
    public  String getTypeName(){
        if(getType()== SymbolType.INT) {
            return "IntFunc";
        }
        else{
            return "VoidFunc";
        }
    }
}
