package llvmir.value.structure;
import java.util.List;
import llvmir.value.Value;
import java.util.ArrayList;
public class Module extends Value{
    private List<Value> functions;
    private List<Value> globalVariables;
    private Function mainFunction;
    public Module() {
        // 单文件结构，但允许多次实例化以便测试/复用
        super("module", null);
        functions = new ArrayList<>();
        globalVariables = new ArrayList<>();
    }
    public void addFunction(Function function){
        // 不考虑重复定义
        functions.add(function);
    }
    public List<Value> getFunctions() {
        return functions;
    }
    public void addGlobalVariable(GlobalVariable globalVariable){
        globalVariables.add(globalVariable);
    }
    public List<Value> getGlobalVariables() {
        return globalVariables;
    }
    public Function getMainFunction() {
        return mainFunction;
    }
    public void setMainFunction(Function mainFunction) {
        this.mainFunction = mainFunction;
    }
    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        for(Value globalVar:globalVariables){
            sb.append(globalVar);
            sb.append("\n");
        }
        sb.append("\ndeclare i32 @getint()\n" +
                "declare i32 @getchar()\n" +
                "declare void @putint(i32)\n" +
                "declare void @putch(i32)\n" +
                "declare void @putstr(i8*)\n\n");
        for (Value value : functions) {
            Function function = (Function) value;
            // 修改点：只打印包含基本块的函数（即用户定义的函数）
            // 库函数没有基本块，所以会被跳过，避免打印空的 define 块
            if (!function.getBasicBlocks().isEmpty()) {
                sb.append(function).append("\n");
            }
        }
        if (mainFunction != null) {
            sb.append(mainFunction);
        }
        return sb.toString();
    }
    public Function searchFunction(String name) {
        for (Value function : functions) {
            if (function.getName().equals(name)) {
                return (Function) function;
            }
        }
        return null;

    }
}
