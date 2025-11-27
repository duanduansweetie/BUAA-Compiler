package parser;
import node.*;
import error.*;
import symbol.*;
import java.util.ArrayList;
import java.util.List;
public class SymbolBuilder {
    private final ScopeStack scopeStack;
    private final ErrorManager errorManager;
    private final IrBuilder irBuilder;
    public SymbolBuilder(ScopeStack scopeStack, ErrorManager errorManager,IrBuilder irBuilder){
        this.scopeStack=scopeStack;
        this.errorManager=errorManager;
        this.irBuilder=irBuilder;
    }
    public void buildConst(BTypeNode bTypeNode,ConstDefNode constDefNode){
        if(constDefNode.getConstExpNode() ==null){
            //不是数组
            String constName=constDefNode.getIdent();
            SymbolType symbolType=SymbolType.INT;
            if(scopeStack.getSymbol(constName)!=null){
                errorManager.addError(constDefNode.getLineno(), ErrorType.B);
            }
            else{
                scopeStack.addSymbol(constName,symbolType,SymbolKind.CONSTANT,constDefNode.getConstInitValNode().calculate());
            }
        }
        else{
            String constName=constDefNode.getIdent();
            ConstExpNode constExpNode=constDefNode.getConstExpNode();
            SymbolType symbolType=SymbolType.INT;
            if(scopeStack.getSymbol(constName)!=null){
                errorManager.addError(constDefNode.getLineno(), ErrorType.B);
            }
           else{
               scopeStack.addSymbol(constName,symbolType,SymbolKind.CONSTANT,constExpNode.calculate(), constDefNode.getConstInitValNode().calculateArray());
            }
        }
    }
    public void buildVar(BTypeNode bTypeNode, VarDefNode varDefNode){
        if(!varDefNode.isArray()){
            //不是数组
            String varName=varDefNode.getIdentNode();
            SymbolType symbolType=SymbolType.INT;
            if(scopeStack.getSymbol(varName)!=null){
                errorManager.addError(varDefNode.getLineno(), ErrorType.B);
            }
            else{
                if (irBuilder.inGlobal == true && varDefNode.getInitValNode() != null) {
                    scopeStack.addSymbol(varName, symbolType, SymbolKind.VARIABLE,
                            varDefNode.getInitValNode().calculate());
                } else if (irBuilder.inGlobal == true) {
                    scopeStack.addSymbol(varName, symbolType, SymbolKind.VARIABLE, 0);
                }
                else{scopeStack.addSymbol(varName,symbolType,SymbolKind.VARIABLE);}
            }
        }
        else{
            String varName=varDefNode.getIdentNode();
           ConstExpNode constExpNode=varDefNode.getConstExpNode();
            SymbolType symbolType=SymbolType.INT;
            if(scopeStack.getSymbol(varName)!=null){
                errorManager.addError(varDefNode.getLineno(), ErrorType.B);
            }
           else{
                if (irBuilder.inGlobal == true && varDefNode.getInitValNode() != null) {
                    scopeStack.addSymbol(varName, symbolType, SymbolKind.VARIABLE, constExpNode.calculate(),
                            varDefNode.getInitValNode().calculateArray());
                } else if (irBuilder.inGlobal == true) {
                    // List<Integer> valueList 初始化为全0
                    scopeStack.addSymbol(varName, symbolType, SymbolKind.VARIABLE, constExpNode.calculate(), null);
                } else
                    scopeStack.addSymbol(varName,symbolType,SymbolKind.VARIABLE,constExpNode.calculate(), null);
            }
        }
    }
    public void buildFunc(FuncTypeNode funcTypeNode, String identNode, FuncFParamsNode funcFParamsNode, int lineno) {
        // 确定函数的返回值类型 int or void
        SymbolType symbolType = funcTypeNode.getType().equals("int") ? SymbolType.INT : SymbolType.VOID;

        // 检查符号表中是否已存在该函数
        Symbol existingSymbol = scopeStack.getSymbol(identNode);
        if(funcFParamsNode==null){
            if (existingSymbol != null) {
                // B类错误：重复定义
                errorManager.addError(lineno, ErrorType.B);
            }
            else{
                scopeStack.addSymbol(identNode,symbolType,0,null);
            }
            return;
        }

        // 构建函数参数列表
        int paramNum = funcFParamsNode.getFuncFParamsNodes().size();
        List<Symbol> paramList = new ArrayList<>();

        for (FuncFParamNode funcFParamNode : funcFParamsNode.getFuncFParamsNodes()) {
            String paramIdent = funcFParamNode.getIdentNode();
            SymbolType paramType = SymbolType.INT; // 参数类型只支持 int
            Symbol paramSymbol;

            if (funcFParamNode.isArray) {
                paramSymbol = new ArraySymbol(paramIdent, -1, paramType, SymbolKind.VARIABLE, -1, -1);
            } else {
                paramSymbol = new BasicSymbol(paramIdent, paramType, SymbolKind.VARIABLE, -1, -1);
            }

            paramList.add(paramSymbol);
        }

        // 添加函数符号到符号表
        if (scopeStack.getSymbol(identNode)!= null) {
            // B类错误：重复定义
            errorManager.addError(lineno, ErrorType.B);
        } else {
            scopeStack.addSymbol(identNode, symbolType, paramNum, paramList);
        }
    }
    public void buildPara(FuncFParamsNode funcFParamsNode){
        for(FuncFParamNode funcFParamNode:funcFParamsNode.getFuncFParamsNodes()){
            String paramName=funcFParamNode.getIdentNode();
            SymbolType symbolType=SymbolType.INT;
            if(scopeStack.getSymbol(paramName)!=null){
               errorManager.addError(funcFParamNode.getLineno(), ErrorType.B);
                return;
            }
            else{
                if(funcFParamNode.isArray){
                    scopeStack.addSymbol(paramName,symbolType,SymbolKind.VARIABLE,-1, new ArrayList<>());
                }
                else{
                    scopeStack.addSymbol(paramName,symbolType,SymbolKind.VARIABLE);
                }
            }
        }
    }

    public void buildStatic(BTypeNode bTypeNode, VarDefNode varDefNode){
        String ident = varDefNode.getIdentNode();
        SymbolType symbolType = SymbolType.INT;
        if(scopeStack.getSymbol(ident)!=null){
            errorManager.addError(varDefNode.getLineno(), ErrorType.B);
            return;
        }
        if(!varDefNode.isArray()){
            if(irBuilder.inGlobal==true&&varDefNode.getInitValNode()!=null){
                scopeStack.addSymbol(ident,symbolType,SymbolKind.STATIC,varDefNode.getInitValNode().calculate());

            }
            else if(irBuilder.inGlobal==true){
                scopeStack.addSymbol(ident,symbolType,SymbolKind.STATIC,0);

            }
            else{scopeStack.addSymbol(ident,symbolType,SymbolKind.STATIC,0);}

        }
        else{
            if(irBuilder.inGlobal==true&&varDefNode.getInitValNode()!=null){
                scopeStack.addSymbol(ident,symbolType,SymbolKind.STATIC,varDefNode.getConstExpNode().calculate(),varDefNode.getInitValNode().calculateArray());

            }
            else if(irBuilder.inGlobal==true){
                scopeStack.addSymbol(ident,symbolType,SymbolKind.STATIC,varDefNode.getConstExpNode().calculate(),null);

            }
            else{ int length=varDefNode.getConstExpNode().calculate();
                scopeStack.addSymbol(ident,symbolType,SymbolKind.STATIC,length, new ArrayList<>());}

        }
    }

}
