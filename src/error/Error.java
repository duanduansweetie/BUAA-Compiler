package error;
import java.util.List;
import node.*;
import symbol.*;
public class Error {
    private final int lineNumber;
    private final ErrorType errorType;
    private ScopeStack scopeStack;
    private ErrorManager errorManager;
   
    // 是否在void函数中
    public Error(int lineNumber, ErrorType errorType){
        this.lineNumber = lineNumber;
        this.errorType = errorType;
    }
    public int getLineNumber() {
        return lineNumber;
    }
    public ErrorType getErrorType() {
        return errorType;
    }
    @Override
    public String toString(){
        return lineNumber + " " + errorType.toString();
    }
    

    public void setScopeStack(ScopeStack scopeStack) {
        this.scopeStack = scopeStack;
    }

    public void setErrorManager(ErrorManager errorManager) {
        this.errorManager = errorManager;
    }
}
