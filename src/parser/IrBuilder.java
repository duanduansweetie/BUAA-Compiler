package parser;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import symbol.ScopeStack;
import node.*;
import llvmir.type.*;
import llvmir.value.instructions.*;
import llvmir.value.structure.BasicBlock;
import llvmir.value.structure.Function;
import llvmir.value.structure.GlobalVariable;
import llvmir.value.structure.Instruction;
import llvmir.value.structure.Module;
import llvmir.value.structure.Param;
import llvmir.value.structure.GlobalVariable.VType;
import llvmir.value.*;
import llvmir.value.constants.*;
public class IrBuilder {
    private Module module;
    private ScopeStack scopeStack;
    BasicBlock currentBlock;
    Function currentFunction;
    public boolean inGlobal = true;
    public boolean voidFuncHasReturn = true;
    Stack<BasicBlock> nextBlockStack = new Stack<>();
    Stack<BasicBlock> forStmtBlockStack = new Stack<>();
    int stringIndex = 0;
    public IrBuilder(Module module, ScopeStack scopeStack) {
        this.module = module;
        this.scopeStack = scopeStack;
        initLibraryFunctions();
    }
    private void initLibraryFunctions() {
        // declare i32 @getint()
        Function getint = new Function("getint", new Integer32Type());
        module.addFunction(getint);

        // declare void @putint(i32)
        List<Param> putintParams = new ArrayList<>();
        putintParams.add(new Param("", new Integer32Type()));
        Function putint = new Function("putint", new VoidType(), putintParams);
        module.addFunction(putint);

        // declare void @putch(i32)
        List<Param> putchParams = new ArrayList<>();
        putchParams.add(new Param("", new Integer32Type()));
        Function putch = new Function("putch", new VoidType(), putchParams);
        module.addFunction(putch);

        // declare void @putstr(i8*)
        List<Param> putstrParams = new ArrayList<>();
        putstrParams.add(new Param("", new PointType(new Integer8Type())));
        Function putstr = new Function("putstr", new VoidType(), putstrParams);
        module.addFunction(putstr);
    }

    // ... existing code ...
    public void setCurrentFunction(Function currentFunction) {
        this.currentFunction = currentFunction;
    }

    public void setCurrentBlock(BasicBlock currentBlock) {
        this.currentBlock = currentBlock;
    }
    // 构建llvm ir
public void buildGlobalVariable(ConstDeclNode constDeclNode){
    VType vType = VType.CONSTANT;
    Type irType;
    Value value;
    BTypeNode bTypeNode=constDeclNode.getBTypeNode();
    List<ConstDefNode> constDefList=constDeclNode.getConstDefNodeList();
    for(ConstDefNode constDefNode:constDefList){
        String name=constDefNode.getIdent();
        if(bTypeNode.getType().equals("int")&&constDefNode.isArray()){
            irType = new IntArrayType(constDefNode.getConstExpNode().calculate());
            value=new ConstantArray(constDefNode.getConstInitValNode().calculateArray(),
                    constDefNode.getConstExpNode().calculate());
        }
        else {
            irType=new Integer32Type();
            value=new ConstantInt(constDefNode.getConstInitValNode().calculate());
        }
        GlobalVariable globalVariable=new GlobalVariable(name,irType,vType,value);
        module.addGlobalVariable(globalVariable);
        scopeStack.lookup(name).setValue(globalVariable);
    }
}
    public void buildGlobalVariable(VarDeclNode varDeclNode) {
        VType vType = VType.VARIABLE;
        Type irType;
        Value value;
        BTypeNode bTypeNode = varDeclNode.getbTypeNode();
        List<VarDefNode> varDefList = varDeclNode.getVarDefNodeList();
        for (VarDefNode varDefNode : varDefList) {
            // 确定变量名
            String name = varDefNode.getIdentNode();
            // 确定变量类型和值
            if (bTypeNode.getType().equals("int") && varDefNode.isArray()) {
                irType = new IntArrayType(varDefNode.getConstExpNode().calculate());
                value = new ConstantArray(varDefNode.calculateArray(), varDefNode.getConstExpNode().calculate());
            } else {
                irType = new Integer32Type();
                value = new ConstantInt(varDefNode.calculate());
            }
            GlobalVariable gv = new GlobalVariable(name, irType, vType, value);
            module.addGlobalVariable(gv);
            scopeStack.lookup(name).setValue(gv);
        }
    }
    public Function buildMainFunction(){
       Function fuc=new Function("main",new Integer32Type());
       module.setMainFunction(fuc);
         return fuc;
    }
    public Function buildFunction(FuncDefNode funcDefNode){
        String name=funcDefNode.getIdent();
        String type=funcDefNode.getFuncType().getType();
        Type irType=type.equals("int")?new Integer32Type():new VoidType();
        if(funcDefNode.getFuncFParams()!=null){
            List<Param> params=buildParams(funcDefNode.getFuncFParams());
            Function function=new Function(name,irType,params);
            module.addFunction(function);
            return function;
        }else{
            Function function=new Function(name,irType);
            module.addFunction(function);
            return function;
        }
    }
    public List<Param> buildParams(FuncFParamsNode funcFParamsNode){
        List<FuncFParamNode> funcFParamNodes=funcFParamsNode.getFuncFParamsNodes();
        List<Param> params=new ArrayList<>();
        Type irType;
        for(int i=0;i<funcFParamNodes.size();i++){
            FuncFParamNode funcFParamNode=funcFParamNodes.get(i);
            String type=funcFParamNode.getbTypeNode().getType();
            if(type.equals("int") && funcFParamNode.isArray()) {
                irType = new PointType(new Integer32Type());
            }
            else{
                irType=new Integer32Type();
            }
            Param param=new Param(funcFParamNode.getIdentNode(),irType);
            params.add(param);
        }
        return  params;
    }
    public void buildFunctionBody(BlockNode blockNode, Function function){
        currentBlock=new BasicBlock(function);
        currentFunction=function;
        inGlobal=false;
        scopeStack.addSymbolTable(blockNode.symbolTableIndex);
        List<Param> params=function.getParams();
        for(int i=0;i<params.size();i++){
            Param param=params.get(i);
            Alloca alloca=new Alloca(param.getType(),currentBlock,true);
            currentBlock.addInstruction(alloca);
            scopeStack.lookup(param.getName()).setValue(alloca);
            Store store=new Store(param,alloca,currentBlock);
            currentBlock.addInstruction(store);
        }
        scopeStack.popSymbolTable();
        buildBlock(blockNode);
        if(!voidFuncHasReturn && function.getType() instanceof VoidType){
            Ret ret=new Ret(null,currentBlock);
            currentBlock.addInstruction(ret);
        }
    }

    public void buildStmt(StmtNode stmtNode){
        if (stmtNode.isLValAndExp())
            buildLValAndExp(stmtNode.getLValNode(), stmtNode.getExpNode());
        else if (stmtNode.isIf())
            buildIf(stmtNode.getCondNode(), stmtNode.getStmtNode1(), stmtNode.getStmtNode2());
        else if (stmtNode.isFor())
            buildFor(stmtNode.getForStmtNode1(), stmtNode.getCondNode(), stmtNode.getForStmtNode2(),
                    stmtNode.getStmtNode1());
        else if (stmtNode.isReturn())
            buildReturn(stmtNode.getExpNode());
        else if (stmtNode.isBreak())
            buildBreak();
        else if (stmtNode.isContinue())
            buildContinue();
        else if (stmtNode.isBlock())
            buildBlock(stmtNode.getBlockNode());
        else if (stmtNode.isExp())
            buildExp(stmtNode.getExpNode());
        else if (stmtNode.isPrintf())
            buildPrintf(stmtNode.getStringConst(), stmtNode.getExpNodeList());
    }

    public void buildPrintf(String stringConst, List<ExpNode> expNodeList){
        // 打印字符串
        // 打印表达式
        // 当输出字符串stringConst中有%c和%d时，需要对其进行切割
        // 例如：printf("a=%c,b=%d.",a,b) ->
        // printf("a="),putch(a),printf(",b="),putint(b),printf(".")
        List<String> segments = cutString(stringConst);
        List<Value> values = new ArrayList<>();
        for (ExpNode expNode : expNodeList) {
            values.add(buildExp(expNode));
        }
        for(String string:segments){
            if(string.equals("%d")){
                Function function=new Function("putint",new VoidType());
                if(values.get(0).getType() instanceof Integer8Type)
                {
                    Zext zext = new Zext(values.get(0), currentBlock);
                    currentBlock.addInstruction(zext);
                    values.set(0, zext);
                }
                Instruction call = new Call(function, values.get(0), currentBlock);
                currentBlock.addInstruction(call);
                values.remove(0);
            }
            else{
                GlobalVariable gv=findGlobalVariable(string);
                if(gv!=null){
                    GetElementPtr getElementPtr = new GetElementPtr(new ConstantInt(0), gv, currentBlock);
                    currentBlock.addInstruction(getElementPtr);
                    Function function = new Function("putstr", new VoidType());
                    Instruction call = new Call(function, getElementPtr, currentBlock);
                    currentBlock.addInstruction(call);
                    continue;
                }
                Value value = new ConstantString(string, string.length());
                String name = ".str." + stringIndex++;
                // 这里改过
                GlobalVariable newGv = new GlobalVariable(name, new CharArrayType(string.length()), VType.CONSTANT,
                        value);
                module.addGlobalVariable(newGv);
                Function function = new Function("putstr", new VoidType());

                GetElementPtr getElementPtr = new GetElementPtr(new ConstantInt(0), newGv, currentBlock);
                currentBlock.addInstruction(getElementPtr);
                Instruction call = new Call(function, getElementPtr, currentBlock);
                currentBlock.addInstruction(call);
            }
        }
    }
    public GlobalVariable findGlobalVariable(String string) {
        for (Value globalVariable : module.getGlobalVariables()) {
            GlobalVariable gv = (GlobalVariable) globalVariable;
            if (gv.getFirstOperand() instanceof ConstantString) {
                ConstantString constantString = (ConstantString) gv.getFirstOperand();
                String str = constantString.getValueStr();
                if (str != null && str.length() != 0 && str.equals(string)) {
                    return gv;
                }
            }
        }
        return null;
    }

    public List<String> cutString(String stringConst){
        List<String> segments=new ArrayList<>();
        StringBuilder sb=new StringBuilder();
        if(stringConst==null||stringConst.length()<=2){
            return segments;
        }
        stringConst=stringConst.substring(1,stringConst.length()-1);
        for(int i=0;i<stringConst.length();i++){
            if(stringConst.charAt(i)=='%'&&i<stringConst.length()-1){
                if(stringConst.charAt(i + 1) == 'd'){
                    if(sb.length()!=0){
                        segments.add(sb.toString()+"\00");
                        sb=new StringBuilder();
                    }
                    i++;
                    segments.add("%" + stringConst.charAt(i));
                }
            }
            else if (stringConst.charAt(i) == '\\' && i < stringConst.length() - 1) {
                if (stringConst.charAt(i + 1) == 'n') {
                    sb.append('\n');
                    i++;
                } else {
                    sb.append(stringConst.charAt(i));
                }
            } else {
                sb.append(stringConst.charAt(i));
            }
        }
        if (sb.length() != 0) {
            segments.add(sb.toString() + "\00");
        }
        return segments;
    }
public void buildLValAndExp(LValNode lValNode, ExpNode expNode){
    Value value = buildLVal(lValNode);
    Value expValue = buildExp(expNode);
    if(((PointType)value.getType()).getPoint() instanceof Integer8Type && expValue.getType() instanceof Integer32Type){
        Trunc trunc = new Trunc(expValue, currentBlock);
        currentBlock.addInstruction(trunc);
        expValue = trunc;
    }else if(((PointType)value.getType()).getPoint() instanceof Integer32Type && expValue.getType() instanceof Integer8Type){
        Zext zext = new Zext(expValue, currentBlock);
        currentBlock.addInstruction(zext);
        expValue = zext;
    }
    Store store = new Store(expValue, value, currentBlock);
    currentBlock.addInstruction(store);
}
public void buildIf(CondNode condNode,StmtNode stmtNode1,StmtNode stmtNode2){
    BasicBlock thenBlock=new BasicBlock(currentFunction);
    BasicBlock mergeBlock=new BasicBlock(currentFunction);
    if(stmtNode2==null){
        buildCond(condNode,thenBlock,mergeBlock);
        currentBlock=thenBlock;
        buildStmt(stmtNode1);
        Br br = new Br(mergeBlock, currentBlock);
        currentBlock.addInstruction(br);
        currentBlock = mergeBlock;
    }
    else{
        BasicBlock elseBlock=new BasicBlock(currentFunction);
        buildCond(condNode,thenBlock,elseBlock);
        currentBlock=thenBlock;
        buildStmt(stmtNode1);
        Br brThen = new Br(mergeBlock, currentBlock);
        currentBlock.addInstruction(brThen);
        currentBlock=elseBlock;
        buildStmt(stmtNode2);
        Br brElse = new Br(mergeBlock, currentBlock);
        currentBlock.addInstruction(brElse);
        currentBlock=mergeBlock;
    }
}
public void buildCond(CondNode condNode,BasicBlock thenBlock,BasicBlock elseBlock) {
        buildLOrExp(condNode.getlOrExpNode(), thenBlock, elseBlock);
}
public void buildLOrExp(LOrExpNode lOrExpNode, BasicBlock trueBlock, BasicBlock falseBlock){
        BasicBlock nextBlock;
        int i;
        for(i=0;i<lOrExpNode.getlAndExpNodeList().size()-1;i++){
            LAndExpNode lAndExpNode=lOrExpNode.getlAndExpNodeList().get(i);
            nextBlock=new BasicBlock(currentFunction);
            buildLAndExp(lAndExpNode,trueBlock,nextBlock);
            currentBlock=nextBlock;
        }
        LAndExpNode lAndExpNode=lOrExpNode.getlAndExpNodeList().get(i);
        buildLAndExp(lAndExpNode,trueBlock,falseBlock);
}
public void buildLAndExp(LAndExpNode lAndExpNode, BasicBlock trueBlock, BasicBlock falseBlock){
        BasicBlock nextBlock;
        int i;
        for(i=0;i<lAndExpNode.getEqExpNodeList().size()-1;i++){
            EqExpNode eqExpNode=lAndExpNode.getEqExpNodeList().get(i);
            nextBlock=new BasicBlock(currentFunction);
          Value value=buildEqExp(eqExpNode);
            Br br=new Br(trueBlock,nextBlock,value,currentBlock);
            currentBlock.addInstruction(br);
            currentBlock=nextBlock;
        }
        EqExpNode eqExpNode=lAndExpNode.getEqExpNodeList().get(i);
        Value value=buildEqExp(eqExpNode);
        Br br=new Br(trueBlock,falseBlock,value,currentBlock);
        currentBlock.addInstruction(br);
}
    public Value buildEqExp(EqExpNode eqExpNode) {
        // 先计算内部的instruction，再计算当前的instruction
        List<Value> instructions = new ArrayList<>();

        // 遍历 EqExp 的下一级节点，即 RelExp 节点
        for (int i = 0; i < eqExpNode.getRelExpNodeList().size(); i++) {
            RelExpNode relExpNode = eqExpNode.getRelExpNodeList().get(i);
            Value value = buildRelExp(relExpNode);
            // 添加到 instructions 列表中
            instructions.add(value);
        }
        // 如果只有一个值
        if (eqExpNode.getRelExpNodeList().size() == 1) {
            Icmp icmp = new Icmp(instructions.get(0), new ConstantInt(0), "!=", currentBlock);
            currentBlock.addInstruction(icmp);
            return icmp;
        }

        // 逐一处理操作符 `==` 和 `!=`
        for (int i = 0; i < eqExpNode.getRelExpNodeList().size() - 1; i++) {
            String op = eqExpNode.getEqOpList().get(i); // 获取操作符 `==` 或 `!=`
            Value instruction1 = instructions.get(i);
            Value instruction2 = instructions.get(i + 1);

            if (instruction1.getType() instanceof Integer1Type) {
                Zext zext = new Zext(instruction1, currentBlock);
                currentBlock.addInstruction(zext);
                instruction1 = zext;
            }

            // `==` 和 `!=` 的结果是 i1 类型
            Icmp icmp = new Icmp(instruction1, instruction2, op, currentBlock);
            currentBlock.addInstruction(icmp);

            // 将结果存储到 instructions 中的第 i+1 个位置
            instructions.set(i + 1, icmp);
        }
        // 返回的是i1类型
        return instructions.get(instructions.size() - 1);
    }

    public Value buildRelExp(RelExpNode relExpNode) {
        // 先计算内部的instruction，再计算当前的instruction
        List<Value> instructions = new ArrayList<>();

        // 遍历 RelExp 的下一级节点，即 AddExp 节点
        for (int i = 0; i < relExpNode.getAddExpNodeList().size(); i++) {
            AddExpNode addExpNode = relExpNode.getAddExpNodeList().get(i);
            Value value = buildAddExp(addExpNode);
            // 添加到 instructions 列表中
            instructions.add(value);
        }

        for (int i = 0; i < relExpNode.getAddExpNodeList().size() - 1; i++) {
            String op = relExpNode.getRelOpList().get(i); // 获取操作符 `<`、`<=`、`>`、`>=`
            Value instruction1 = instructions.get(i);
            Value instruction2 = instructions.get(i + 1);

            // `<`、`<=`、`>`、`>=` 的结果是 i1 类型
            Icmp icmp = new Icmp(instruction1, instruction2, op, currentBlock);
            currentBlock.addInstruction(icmp);
            // 转换为i32，方便后续的计算
            Zext zext = new Zext(icmp, currentBlock);
            currentBlock.addInstruction(zext);

            // 将结果存储到 instructions 中的第 i+1 个位置
            instructions.set(i + 1, zext);
        }
        // 返回值应该是一个 i32
        return instructions.get(instructions.size() - 1);
    }
    public void buildFor(ForStmtNode forStmtNode1, CondNode condNode, ForStmtNode forStmtNode2, StmtNode stmtNode) {
        // for语句
        BasicBlock nextBlock = new BasicBlock(currentFunction); // for结束后的基本块
        BasicBlock stmtBlock = new BasicBlock(currentFunction); // for循环体的基本块
        BasicBlock forStmtBlock = new BasicBlock(currentFunction); // forStmt的基本块
        BasicBlock condBlock = null;

        nextBlockStack.push(nextBlock);
        forStmtBlockStack.push(forStmtBlock);

        // 处理初始化部分
        if (forStmtNode1 != null) {
            buildForStmt(forStmtNode1);
        }

        // 处理条件部分
        if (condNode != null) {
            condBlock = new BasicBlock(currentFunction);
            Br br = new Br(condBlock, currentBlock);
            currentBlock.addInstruction(br);
            currentBlock = condBlock;
            buildCond(condNode, stmtBlock, nextBlock);
        } else {
            Br br = new Br(stmtBlock, currentBlock);
            currentBlock.addInstruction(br);
        }

        // 处理循环体部分
        currentBlock = stmtBlock;
        buildStmt(stmtNode);
        Br br = new Br(forStmtBlock, currentBlock);
        currentBlock.addInstruction(br);

        // 处理循环更新部分
        currentBlock = forStmtBlock;
        if (forStmtNode2 != null) {
            buildForStmt(forStmtNode2);
        }
        if (condBlock != null) {
            Br br1 = new Br(condBlock, currentBlock);
            currentBlock.addInstruction(br1);
        } else {
            Br br1 = new Br(stmtBlock, currentBlock);
            currentBlock.addInstruction(br1);
        }

        // 跳转到结束块
        currentBlock = nextBlock;

        nextBlockStack.pop();
        forStmtBlockStack.pop();
    }
    public void buildForStmt(ForStmtNode forStmtNode) {
        List<LValNode> lValNodes = forStmtNode.getlValNode(); // 获取多个 LVal 节点
        List<ExpNode> expNodes = forStmtNode.getExpNode();   // 获取多个 Exp 节点

        for (int i = 0; i < lValNodes.size(); i++) {
            LValNode lValNode = lValNodes.get(i);
            ExpNode expNode = expNodes.get(i);
            buildLValAndExp(lValNode, expNode); // 对每个 LVal 和 Exp 生成赋值代码
        }
    }

    public void buildReturn(ExpNode expNode) {
        if (expNode == null) {
            // 返回空
            Ret ret = new Ret(null, currentBlock);
            currentBlock.addInstruction(ret);
            return;
        }
        // 返回语句
        Value value = buildExp(expNode);
        // 类型转换
        if (currentFunction.getType() instanceof Integer8Type) {
            if (value.getType() instanceof Integer32Type) {
                Trunc trunc = new Trunc(value, currentBlock);
                currentBlock.addInstruction(trunc);
                value = trunc;
            }
        } else if (currentFunction.getType() instanceof Integer32Type) {
            if (value.getType() instanceof Integer8Type) {
                Zext zext = new Zext(value, currentBlock);
                currentBlock.addInstruction(zext);
                value = zext;
            }
        }
        Ret ret = new Ret(value, currentBlock);
        currentBlock.addInstruction(ret);
    }
    public void buildBreak() {
        // break语句
        Br br = new Br(nextBlockStack.peek(), currentBlock);
        currentBlock.addInstruction(br);

        currentBlock = new BasicBlock(currentFunction);
    }

    public void buildContinue() {
        // continue语句
        Br br = new Br(forStmtBlockStack.peek(), currentBlock);
        currentBlock.addInstruction(br);

        currentBlock = new BasicBlock(currentFunction);
    }
    public void buildBlock(BlockNode blockNode) {
        // 处理block
        // 处理block之前，要将符号表的List中将已经弹出的该块的符号表重新调回栈内
        scopeStack.addSymbolTable(blockNode.symbolTableIndex);

        for (BlockItemNode blockItemNode : blockNode.getBlockItemNodes()) {
            if (blockItemNode.getDeclNode() != null) {
                // 处理变量和常量声明
                DeclNode declNode = blockItemNode.getDeclNode();
                if (declNode.getConstDeclNode() != null) {
                    // ConstExp -> AddExp 中使用的 ident 必须是普通常量，不包括数组元素、函数返回值；
                    // 当然ConstExp 中还可以使用数值常量，字符常量。
                    buildConstant(declNode.getConstDeclNode());
                } else {
                    // 处理变量声明
                    buildVariable(declNode.getVarDeclNode());
                }
            } else {
                // 处理语句
                buildStmt(blockItemNode.getStmtNode());
            }
        }
        // 处理完block之后，将符号表弹出
        scopeStack.popSymbolTable();
    }
    public void buildConstant(ConstDeclNode constDeclNode) {
        // 构建局部常量，不需要加入module，只需要作为语句加入当前函数
        // 对于常量，先用alloca分配空间，再store赋值
        Type irType;
        BTypeNode bTypeNode = constDeclNode.getBTypeNode();
        List<ConstDefNode> constDefList = constDeclNode.getConstDefNodeList();
        for (ConstDefNode constDefNode : constDefList) {
            // 确定常量名
            String name = constDefNode.getIdent();
            // 确定常量类型和值
            if (bTypeNode.getType().equals("int") && constDefNode.isArray()) {
                irType = new IntArrayType(constDefNode.getConstExpNode().calculate());
            } else if (bTypeNode.getType().equals("int")) {
                irType = new Integer32Type();
            } else if (bTypeNode.getType().equals("char") && constDefNode.isArray()) {
                irType = new CharArrayType(constDefNode.getConstExpNode().calculate());
            } else {
                irType = new Integer8Type();
            }
            Alloca alloca = new Alloca(irType, currentBlock);
            currentBlock.addInstruction(alloca);
            scopeStack.lookup(name).setValue(alloca);

            if (!constDefNode.isArray() && bTypeNode.getType().equals("int")) {
                Store store = new Store(new ConstantInt(constDefNode.getConstInitValNode().calculate()), alloca,
                        currentBlock);
                currentBlock.addInstruction(store);
                // ((BasicSymbol)
                // scopeStack.lookup(name)).setValue(constDefNode.getConstInitValNode().calculate());
            } else{
                // int数组,需要逐个赋值，使用getelementptr
                for (int i = 0; i < constDefNode.getConstInitValNode().calculateArray().size(); i++) {
                    GetElementPtr getElementPtr = new GetElementPtr(new ConstantInt(i), alloca, currentBlock);
                    currentBlock.addInstruction(getElementPtr);
                    Store store = new Store(new ConstantInt(constDefNode.getConstInitValNode().calculateArray().get(i)),
                            getElementPtr, currentBlock);
                    currentBlock.addInstruction(store);
                }
                // ((ArraySymbol) scopeStack.lookup(name))
                // .setValueList(constDefNode.getConstInitValNode().calculateArray());
            }
        }
    }
    public void buildVariable(VarDeclNode varDeclNode) {
        // 对于变量，也要先alloca分配空间，但是中间需要判断是否有初始化
        // 未初始化的变量，也进行空间的分配
        Type irType;
        BTypeNode bTypeNode = varDeclNode.getbTypeNode();
        List<VarDefNode> varDefList = varDeclNode.getVarDefNodeList();
        for (VarDefNode varDefNode : varDefList) {
            // 确定变量名
            String name = varDefNode.getIdentNode();
            // 确定变量类型和值
            if (bTypeNode.getType().equals("int") && varDefNode.isArray()) {
                irType = new IntArrayType(varDefNode.getConstExpNode().calculate());
            } else if (bTypeNode.getType().equals("int")) {
                irType = new Integer32Type();
            } else if (bTypeNode.getType().equals("char") && varDefNode.isArray()) {
                irType = new CharArrayType(varDefNode.getConstExpNode().calculate());
            } else {
                irType = new Integer8Type();
            }
            Alloca alloca = new Alloca(irType, currentBlock);
            currentBlock.addInstruction(alloca);
            scopeStack.lookup(name).setValue(alloca);

            InitValNode initValNode = varDefNode.getInitValNode();
            if (initValNode == null) {
                // 从符号表找到这个变量,并把他的Value设置为null
                continue;
            }
           if (initValNode.getExpNode() != null) {
                ExpNode expNode = initValNode.getExpNode();

                // 有初始化表达式，用BinaryOp语句计算值
                Value value = buildExp(expNode);
                // 类型转换
                if (irType instanceof Integer8Type) {
                    if (value.getType() instanceof Integer32Type) {
                        Trunc trunc = new Trunc(value, currentBlock);
                        currentBlock.addInstruction(trunc);
                        value = trunc;
                    }
                } else if (irType instanceof Integer32Type) {
                    if (value.getType() instanceof Integer8Type) {
                        Zext zext = new Zext(value, currentBlock);
                        currentBlock.addInstruction(zext);
                        value = zext;
                    }
                }
                Store store = new Store(value, alloca, currentBlock);
                currentBlock.addInstruction(store);
            } else {
                // 数组初始化
                for (int i = 0; i < initValNode.getExpNodeList().size(); i++) {
                    ExpNode expNode = initValNode.getExpNodeList().get(i);

                    Value value = buildExp(expNode);
                    GetElementPtr getElementPtr = new GetElementPtr(new ConstantInt(i), alloca, currentBlock);
                    currentBlock.addInstruction(getElementPtr);
                    Store store = new Store(value, getElementPtr, currentBlock);
                    currentBlock.addInstruction(store);

                }
            }
        }

    }
    public Value buildExp(ExpNode expNode) {
        return buildAddExp(expNode.getAddExpNode());
    }

    public Value buildAddExp(AddExpNode addExpNode) {
        // 先计算内部的instrution,再计算当前的instruction
        // 先存储所有的instruction,再根据op进行计算
        List<Value> instructions = new ArrayList<>();

        for (int i = 0; i < addExpNode.getMulExpNodeList().size(); i++) {
            MulExpNode mulExpNode = addExpNode.getMulExpNodeList().get(i);
            Value value = buildMulExp(mulExpNode);
            // 这里要避免对函数的void类型进行zext
            if (value.getType() instanceof Integer8Type || value.getType() instanceof Integer1Type) {
                Zext zext = new Zext(value, currentBlock);
                currentBlock.addInstruction(zext);
                value = zext;
            }
            instructions.add(value);
        }
        for (int i = 0; i < addExpNode.getAddOpList().size(); i++) {
            String op = addExpNode.getAddOpList().get(i);
            Value instruction1 = instructions.get(i);
            Value instruction2 = instructions.get(i + 1);
            // 确定type
            BinaryOp binaryOp = new BinaryOp(instruction1, instruction2, op, currentBlock);
            currentBlock.addInstruction(binaryOp);
            // 将结果存储到instructions中的第i+1个位置
            instructions.set(i + 1, binaryOp);
        }
        return instructions.get(instructions.size() - 1);
    }
    public Value buildMulExp(MulExpNode mulExpNode) {
        // 先计算内部的instrution,再计算当前的instruction
        // 先存储所有的instruction,再根据op进行计算
        List<Value> instructions = new ArrayList<>();

        for (int i = 0; i < mulExpNode.getUnaryExpNodeList().size(); i++) {
            UnaryExpNode unaryExpNode = mulExpNode.getUnaryExpNodeList().get(i);
            Value value = buildUnaryExp(unaryExpNode);
            // 这里要避免对函数的void类型进行zext
            if (value.getType() instanceof Integer8Type || value.getType() instanceof Integer1Type) {
                Zext zext = new Zext(value, currentBlock);
                currentBlock.addInstruction(zext);
                value = zext;
            }
            instructions.add(value);
        }
        for (int i = 0; i < mulExpNode.getMulOpList().size(); i++) {
            String op = mulExpNode.getMulOpList().get(i);
            Value instruction1 = instructions.get(i);
            Value instruction2 = instructions.get(i + 1);
            // 确定type
            BinaryOp binaryOp = new BinaryOp(instruction1, instruction2, op, currentBlock);
            currentBlock.addInstruction(binaryOp);
            // 将结果存储到instructions中的第i+1个位置
            instructions.set(i + 1, binaryOp);
        }
        return instructions.get(instructions.size() - 1);
    }
    public Value buildUnaryExp(UnaryExpNode unaryExpNode) {
        // 先计算内部的instrution,再计算当前的instruction
        // 先存储所有的instruction,再根据op进行计算
        if (unaryExpNode.getPrimaryExpNode() != null) {
            return buildPrimaryExp(unaryExpNode.getPrimaryExpNode());
        } else if (unaryExpNode.getIdentNode() != null) {
            return callFunction(unaryExpNode.getIdentNode(), unaryExpNode.getFuncRParamsNode());
        } else {
            // UnaryOp UnaryExp
            if (Objects.equals(unaryExpNode.getUnaryOpNode().getOp(), "!")) {
                Value instruction = buildUnaryExp(unaryExpNode.getUnaryExpNode());
                if (!(instruction.getType() instanceof Integer32Type)) {
                    Zext zext = new Zext(instruction, currentBlock);
                    currentBlock.addInstruction(zext);
                    instruction = zext;
                }
                Icmp icmp = new Icmp(instruction, new ConstantInt(0), "==", currentBlock);
                currentBlock.addInstruction(icmp);
                return icmp;
            } else if (unaryExpNode.getUnaryOpNode().getOp().equals("-")) {
                // 看作0-unaryExp
                Value instruction = buildUnaryExp(unaryExpNode.getUnaryExpNode());
                if (instruction instanceof ConstantInt) {
                    return new ConstantInt(-((ConstantInt) instruction).getValue());
                }
                if (instruction.getType() instanceof Integer8Type) {
                    Zext zext = new Zext(instruction, currentBlock);
                    currentBlock.addInstruction(zext);
                    instruction = zext;
                }
                BinaryOp binaryOp = new BinaryOp(new ConstantInt(0), instruction, "-", currentBlock);
                currentBlock.addInstruction(binaryOp);
                return binaryOp;
            } else {
                // 出现+操作，直接返回
                return buildUnaryExp(unaryExpNode.getUnaryExpNode());
            }
        }
    }
    public Value buildPrimaryExp(PrimaryExpNode primaryExpNode) {
        if (primaryExpNode.getlValNode() != null) {
            // 右值是一个指针类型
            // 如果是数组的指针，就要用GetElementPtr
            // 如果是普通的指针，就用Load
            Value value = buildLVal(primaryExpNode.getlValNode());
            Type type = ((PointType) value.getType()).getPoint();
            if (type instanceof IntArrayType || type instanceof CharArrayType) {
                GetElementPtr getElementPtr = new GetElementPtr(new ConstantInt(0), value, currentBlock);
                currentBlock.addInstruction(getElementPtr);
                return getElementPtr;
            } else {
                Load load = new Load(value, currentBlock);
                currentBlock.addInstruction(load);
                return load;
            }
        } else if (primaryExpNode.getExpNode() != null) {
            return buildExp(primaryExpNode.getExpNode());
        } else{
            return new ConstantInt(primaryExpNode.getNumber());
        }
    }

    public Value buildLVal(LValNode lValNode) {
        // 先找到这个变量，再根据这个变量的Value进行操作
        String name = lValNode.getIdentNode();
        Value value = scopeStack.lookup(name).getValue();
        if (value == null) {
            value = scopeStack.lookupBefore(name).getValue();
        }
        if (lValNode.getExpNode() != null) {
            // 数组元素
            if (value.getType() instanceof PointType
                    && ((PointType) value.getType()).getPoint() instanceof PointType) {
                Load load = new Load(value, currentBlock);
                currentBlock.addInstruction(load);
                GetElementPtr getElementPtr = new GetElementPtr(buildExp(lValNode.getExpNode()),
                        load, currentBlock, true);
                currentBlock.addInstruction(getElementPtr);
                return getElementPtr;
            }
            Value index = buildExp(lValNode.getExpNode());
            GetElementPtr getElementPtr = new GetElementPtr(index, value, currentBlock);
            currentBlock.addInstruction(getElementPtr);
            return getElementPtr;
        }
        return value;
    }
    public Value callFunction(String name, FuncRParamsNode funcRParamsNode) {
        // 先找到这个函数，再根据这个函数的Value进行操作
        // 函数的调用关键是找到参数
        Function function = module.searchFunction("@" + name);
        if (funcRParamsNode == null) {
            // 无参数
            Call call = new Call(function, currentBlock);
            currentBlock.addInstruction(call);
            return call;
        }
        // 有参数
        // 形参列表
        List<Param> args = function.getParams();
        // 实参列表
        List<ExpNode> expNodes = funcRParamsNode.getExpNodes();
        // 实参的Value列表
        List<Value> values = new ArrayList<>();
        for (int i = 0; i < expNodes.size(); i++) {
            Value value = buildExp(expNodes.get(i));
            // 根据args的类型，进行类型转换
            if (args.get(i).getType() instanceof Integer32Type && value.getType() instanceof Integer8Type) {
                Zext zext = new Zext(value, currentBlock);
                currentBlock.addInstruction(zext);
                values.add(zext);
            } else if (args.get(i).getType() instanceof Integer8Type && value.getType() instanceof Integer32Type) {
                Trunc trunc = new Trunc(value, currentBlock);
                currentBlock.addInstruction(trunc);
                values.add(trunc);
            } else {
                values.add(value);
            }
        }
        // Call(Value function, List<Value> args, BasicBlock basicBlock)
        Call call = new Call(function, values, currentBlock);
        currentBlock.addInstruction(call);
        return call;
    }


}
