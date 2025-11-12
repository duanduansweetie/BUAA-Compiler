package symbol;
import error.ErrorManager;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
public class ScopeStack {
    public static BufferedWriter symbolWriter;
    private final Stack<SymbolMap> scopeStack=new Stack<>();
    private final ErrorManager errorManager;
    //用来存放输出
    private final List<SymbolMap> symbolMapList=new ArrayList<>();
    private int scopeLevel=1;
    private int scopeNum=1;
    public ScopeStack(ErrorManager errorManager){
        this.errorManager=errorManager;
    }
    public void initBuffer(String outputFilePath) throws IOException {
        symbolWriter = new BufferedWriter(new FileWriter(outputFilePath));
    }
    public void closeBuffer(){
        try {
            symbolWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void push(SymbolMap symbolTable) {
        scopeStack.push(symbolTable);
    }

    public SymbolMap pop() {
        return scopeStack.pop();
    }
    public void enterScope(){
        if(scopeStack.isEmpty()){
            SymbolMap map=new SymbolMap(scopeLevel,scopeNum);
            scopeStack.push(map);
        }else{
            scopeLevel++;
            scopeNum++;
            SymbolMap map=new SymbolMap(scopeLevel,scopeNum);
            scopeStack.push(map);
            System.out.println("Entering scope: " + map.getScopenum());
        }
    }
    public void exitScope(){
        if(!scopeStack.isEmpty()){
            scopeLevel--;
            SymbolMap map=scopeStack.pop();
            symbolMapList.add(map);
            System.out.println("Exiting scope: " + map.getScopenum());
        }
    }

    // 将一个符号表添加到栈中
    public void addSymbolTable(int num){
        SymbolMap map=null;
        for(SymbolMap sm:scopeStack){
            if(sm.getScopenum()==num){
                map=sm;
                break;
            }
        }
        if(map!=null){
            scopeStack.push(map);
            symbolMapList.remove(map);
        }
    }
    
    public int getScopeNumberBefore(){
        //返回当前栈顶所在的作用域编号
        return scopeStack.peek().getScopenum();
    }
    public int getScopeNumberCurrent(){
        //返回最近退出的作用域编号
        return symbolMapList.get(symbolMapList.size()-1).getScopenum();
    }


    // 从当前作用域中查找符号
    public Symbol getSymbol(String name){
        if(scopeStack.peek().contains(name)){
            return scopeStack.peek().getSymbol(name);
        }
        return null;
    }
    // 从当前作用域及其上层作用域中查找符号
    public Symbol lookup(String name){
        for(int i=scopeStack.size()-1;i>=0;i--){
            SymbolMap symbolMap=scopeStack.get(i);
            Symbol symbol=symbolMap.getSymbol(name);
            if(symbol!=null){
                return symbol;
            }
        }
        return null;
    }
    // 从上层作用域中查找符号
    public Symbol lookupBefore(String name){
        for(int i=scopeStack.size()-2;i>=0;i--){
            if(scopeStack.get(i).contains(name)){
                return scopeStack.get(i).getSymbol(name);
            }
        }
        return null;
    }
    public boolean addSymbol(Symbol symbol){
        return scopeStack.peek().addSymbol(symbol);
    }

    //基本符号
    public void addSymbol(String name,SymbolType type,SymbolKind kind){
        BasicSymbol symbol=new BasicSymbol(name,type,kind,scopeLevel,scopeNum);
        addSymbol(symbol);
    }

    public void addSymbol(String name,SymbolType type,SymbolKind kind,int value){
        BasicSymbol symbol=new BasicSymbol(name,type,kind,scopeLevel,scopeNum);
        addSymbol(symbol);
    }


    //数组
    public void addSymbol(String name,SymbolType type,SymbolKind kind,int size,List<Integer> valueList){
        ArraySymbol symbol=new ArraySymbol(name,size,type,kind,scopeLevel,scopeNum);
        addSymbol(symbol);
    }
    public void addSymbol(String name,SymbolType type,int paramNum,List<Symbol> paramList){
        FuncSymbol symbol=new FuncSymbol(name,type,paramNum,paramList,scopeLevel,scopeNum);
        addSymbol(symbol);
    }
    public void showSymbolList() throws IOException {
        System.out.println("[DEBUG] Writing symbol list to file...");
        if(errorManager.hasErrors()){
            return;
        }
        symbolMapList.sort((o1, o2) -> Integer.compare(o1.getScopenum(), o2.getScopenum()));
        for(SymbolMap symbolMap:symbolMapList){
            symbolMap.show();
        }
    }
}
