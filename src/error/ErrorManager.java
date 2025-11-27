package error;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import node.ExpNode;
import node.FuncRParamsNode;
import symbol.*;
public class ErrorManager {
    private final List<Error> errorList = new ArrayList<>();
    public boolean hasError = false; // 是否有错误
    public int inFor=0;
    public boolean inVoidFunc = false;
    public boolean inIntFunc = false;
    public boolean inFor(){
        return inFor!=0;
    }
     private ScopeStack scopeStack; // 添加 ScopeStack 的引用

    public void setScopeStack(ScopeStack scopeStack) {
        this.scopeStack = scopeStack;
    }
    public void addError(int lineNumber, ErrorType errorType) {
        for(Error error : errorList) {
            if (error.getLineNumber() == lineNumber) {
                return; // Error already exists, do not add again
            }
        }
        hasError = true;
        errorList.add(new Error(lineNumber, errorType));
    }
    public void showErrors() {
        errorList.sort(Comparator.comparingInt(Error::getLineNumber));
        for (Error error : errorList) {
            System.out.println(error.getLineNumber() + " " + error.getErrorType());
        }
    }

    public void writeErrorsToFile(String filePath) throws IOException {
        errorList.sort(Comparator.comparingInt(Error::getLineNumber));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (Error error : errorList) {
                writer.write(error.getLineNumber() + " " + error.getErrorType() + "\n");
            }
        }
    }

    //D:函数参数个数不匹配
    public void checkFuncParams(String identNode, int linenum, FuncRParamsNode funcRParamsNode){
        int paramNum=funcRParamsNode.getExpNodes().size();
        FuncSymbol symbol=(FuncSymbol) scopeStack.lookup(identNode);
        if(symbol!=null){
            if(paramNum!=symbol.getParamNum()){
               addError(linenum,ErrorType.D);
            }
            else{
                checkParamTypes(funcRParamsNode, symbol.getParamList(), linenum);
            }
        }
    }
    //E:函数参数类型不匹配
    private void checkParamTypes(FuncRParamsNode funcRParamsNode, List<Symbol> paramList, int linenum){
        for(int i=0;i<paramList.size();i++){
            Symbol param = paramList.get(i);
            ExpNode exp = funcRParamsNode.getExpNodes().get(i);
            Symbol realParam=exp.getIdent()!=null?scopeStack.lookup(exp.getIdent()) : null;
            boolean isArray=realParam instanceof ArraySymbol;
            if(param instanceof ArraySymbol){
                if(isArray){
                    ArraySymbol realArray = (ArraySymbol) param;
                    ArraySymbol expArray = (ArraySymbol) realParam;
                    if (realArray.getType() != expArray.getType()) {
                        addError(linenum, ErrorType.E);
                    }
                }
                else{
                    addError(linenum,ErrorType.E);
                }
            }
            else if(isArray){
                addError(linenum,ErrorType.E);
            }
        }
    }
    //H：不能对常量赋值
    public void checkConstantAssignment(Symbol symbol, int linenum){
        if(symbol instanceof ArraySymbol arraySymbol && arraySymbol.getKind() == SymbolKind.CONSTANT ||
                symbol instanceof BasicSymbol basicSymbol && basicSymbol.getKind() == SymbolKind.CONSTANT)
        {
           addError(linenum,ErrorType.H);
        }
    }
    // L:printf函数参数类型不匹配
    public void checkPrintfType(String stringConst, List<ExpNode> expNodeList,int linenum){
        int count=0;
        for(int i=0;i<stringConst.length();i++){
            if(stringConst.charAt(i)=='%'&&i+1<stringConst.length()){
                if (stringConst.charAt(i + 1) == 'c') {
                    count++;
                } else if (stringConst.charAt(i + 1) == 'd') {
                    count++;
                }
            }
        }
        int count_exp=expNodeList.size();
        if(count!=count_exp){
            addError(linenum,ErrorType.L);
        }
    }


    public boolean hasErrors() {
        return !errorList.isEmpty();
    }

    public List<Error> getErrors() {
        return errorList;
    }
}
