package symbol;
import java.util.Map;
import java.io.IOException;
import java.util.LinkedHashMap;
public class SymbolMap {
    private final Map<String,Symbol> symbols=new LinkedHashMap<>();
    private final int scopelevel;
    private final int scopenum;
    public SymbolMap(int scopelevel,int scopenum){
        this.scopelevel=scopelevel;
        this.scopenum=scopenum;
    }
    public boolean addSymbol(Symbol symbol){
        if(symbols.containsKey(symbol.getName())){
            System.err.println("符号重复定义: " + symbol.getName());
            return false;
        }
        symbols.put(symbol.getName(),symbol);
        return true;
    }
    public Symbol getSymbol(String name){
        return symbols.get(name);
    }
    public boolean contains(String name){
        return symbols.containsKey(name);
    }
    public int getScopelevel(){
        return scopelevel;
    }
    public int getScopenum(){
        return scopenum;
    }
    public void show() throws IOException{
        for(Symbol symbol:symbols.values()){
            ScopeStack.symbolWriter.write( scopenum+" "+symbol.getName()+" "+symbol.getTypeName()+ "\n");
        }
    }
}
