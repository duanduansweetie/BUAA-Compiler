package parser;
import error.Error;
import error.ErrorManager;
import error.ErrorType;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import lexer.LexerType;
import lexer.Token;
import node.*;
import symbol.*;

public class Parser {
    public static BufferedWriter parseWriter;
    private List<Token> tokenList=new ArrayList<>();
    private Token nowToken;
    private Token nextToken;
    private int index=0;//tokenlist索引
    private final ErrorManager errorManager;
    private Error error;
    private final ScopeStack scopeStack;
    private final SymbolBuilder symbolBuilder;
    public Parser(ErrorManager errorManager,ScopeStack scopeStack,SymbolBuilder symbolBuilder) {
        this.errorManager = errorManager;
        this.scopeStack=scopeStack;
        this.symbolBuilder=symbolBuilder;
    }
    public List<Token> getTokenList() {
        return tokenList;
    }
    public void setTokenList(List<Token> tokenList) {
        this.tokenList = tokenList;
    }
    public void init(String outputFilePath) throws IOException {
        //reader = new BufferedReader(new FileReader(inputFilePath));
        parseWriter = new BufferedWriter(new FileWriter(outputFilePath));
    }
    public void close() throws IOException {
        parseWriter.close();
    }
    public void getToken(){
        if(index<tokenList.size()){
            nowToken= tokenList.get(index);
            if(index+1<tokenList.size()){
                nextToken = tokenList.get(index+1);
                index++;
            }else{
                nextToken = null;
                index++;
            }
        }
        else{
            nowToken = null;
            nextToken = null;
        }
        System.out.println("[DEBUG] Current token: " + (nowToken != null ? nowToken.getValue() : "null") + ", Type: " + (nowToken != null ? nowToken.getType() : "null"));
    }
    public void retract(){
        if(index-2<0)
            index=0;
        else
            index-=2;
        getToken();
    }
    public void showParser() throws IOException{
        CompUnitNode compUnitNode = parseCompUnit();
        if(errorManager.hasErrors()){
            return;
        }
        if(compUnitNode!=null){
            compUnitNode.show();
        }
    }
    public CompUnitNode parseCompUnit() {
        // CompUnit → {Decl} {FuncDef} MainFuncDef
        List<DeclNode> declNodes = new ArrayList<>();
        List<FuncDefNode> funcDefNodes = new ArrayList<>();
        MainFuncDefNode mainFuncDefNode = null;
        scopeStack.enterScope();

        getToken();
        while (nowToken.getType() == LexerType.CONSTTK
                || (nowToken.getType() == LexerType.INTTK && nextToken.getType() != LexerType.MAINTK
                && tokenList.get(index + 1).getType() != LexerType.LPARENT)
        ) {
            DeclNode declNode = parseDecl();

            if (declNode != null) {
                declNodes.add(declNode);
            }
        }

        while ((nowToken.getType() == LexerType.INTTK && nextToken.getType() != LexerType.MAINTK)
                || nowToken.getType() == LexerType.VOIDTK) {
            FuncDefNode funcDefNode = parseFuncDef();
            if (funcDefNode != null) {
                funcDefNodes.add(funcDefNode);
            }
        }
        if (nowToken.getType() == LexerType.INTTK && nextToken.getType() == LexerType.MAINTK) {
            mainFuncDefNode = parseMainFuncDef();
        }
        CompUnitNode compUnitNode = new CompUnitNode(declNodes, funcDefNodes, mainFuncDefNode);

        scopeStack.exitScope();

        return compUnitNode;
    }
    public DeclNode parseDecl(){
        System.out.println("[DEBUG] Entering parseDecl, Current Token: " + nowToken.getValue());
        ConstDeclNode constDeclNode;
        VarDeclNode varDeclNode;
        if(nowToken.getType()==LexerType.CONSTTK){
            constDeclNode = parseConstDecl();
            return new DeclNode(constDeclNode);
        }
        else if(nowToken.getType()==LexerType.INTTK){
            varDeclNode = parseVarDecl();
            return new DeclNode(varDeclNode);
        }
        else if(nowToken.getType()==LexerType.STATICTK){
            varDeclNode = parseVarDecl();
            return new DeclNode(varDeclNode);
        }
        else{
            System.err.println("Decl error");
            return null;
        }
    }
    public ConstDeclNode parseConstDecl(){
        System.out.println("[DEBUG] Entering parseConstDecl, Current Token: " + nowToken.getValue());
        BTypeNode bTypeNode;
        List<ConstDefNode> constDefNodeList = new ArrayList<>();
        getToken();
        bTypeNode = parseBType();
        getToken();
        ConstDefNode constDefNode = parseConstDef();
        if(constDefNode!=null){
            constDefNodeList.add(constDefNode);
            symbolBuilder.buildConst(bTypeNode, constDefNode);
        }
        while(nowToken.getType()==LexerType.COMMA){
            getToken();
            constDefNode= parseConstDef();
            if(constDefNode!=null){
                constDefNodeList.add(constDefNode);
                symbolBuilder.buildConst(bTypeNode, constDefNode);
            }

        }
        if(nowToken.getType()==LexerType.SEMICN){
            getToken();
        }
        else{
            errorManager.addError(tokenList.get(index-2).getLinenum(), ErrorType.I);
        }
        System.out.println("[DEBUG] Exiting parseConstDecl");
        return new ConstDeclNode(bTypeNode, constDefNodeList);
    }
    public ConstDefNode parseConstDef(){
        // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
        String ident;
        ConstExpNode constExpNode = null;
        ConstInitValNode constInitValNode;
        int linenum;
        if(nowToken.getType()==LexerType.IDENFR){
            ident = nowToken.getValue();
            linenum=nowToken.getLinenum();
            getToken();
        }
        else{
            System.err.println("ConstDef error: missing identifier");
            return null;
        }
        if(nowToken.getType()==LexerType.LBRACK){
            getToken();
            constExpNode = parseConstExp();
            if(nowToken.getType()==LexerType.RBRACK){
                getToken();
            }
            else{
                errorManager.addError(tokenList.get(index-2).getLinenum(), ErrorType.K);
            }
        }
        if(nowToken.getType()==LexerType.ASSIGN){
            getToken();
        }
        else{
            System.err.println("ConstDef error: missing '='");
        }
        constInitValNode = parseConstInitVal();
        ConstDefNode constDefNode= new ConstDefNode(ident, constExpNode, constInitValNode);
        constDefNode.setLineno(linenum);
        return constDefNode;
    }
    public ConstInitValNode parseConstInitVal(){
        // ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}'
        ConstExpNode constExpNode=null;
        List<ConstExpNode> constExpNodes = new ArrayList<>();
        if(nowToken.getType()==LexerType.LBRACE){
            getToken();
            if(nowToken.getType()!=LexerType.RBRACE){
                constExpNode = parseConstExp();//已经调用了一次gettoken，读到逗号了。
                if(constExpNode!=null)
                    constExpNodes.add(constExpNode);
                while(nowToken.getType()==LexerType.COMMA){
                    getToken();
                    constExpNode = parseConstExp();
                    if(constExpNode!=null)
                        constExpNodes.add(constExpNode);
                }
                if(nowToken.getType()==LexerType.RBRACE){
                    getToken();
                }
                else{
                    System.err.println("ConstInitVal error: missing '}'");
                    return null;
                }
            }else if(nowToken.getType()==LexerType.RBRACE){
                //空初始化列表
                getToken();
                return new ConstInitValNode(constExpNodes);
            }
            return new ConstInitValNode(constExpNodes);
        }else{
            constExpNode = parseConstExp();
            if(constExpNode==null){
                System.err.println("ConstInitVal error: invalid ConstExp");
                return null;
            }
            else
                return new ConstInitValNode(constExpNode);
        }
    }

    public BTypeNode parseBType(){
        //System.out.println("Parsing BType, currentToken: " + nowToken.getValue()+" "+nowToken.getType());
        if(nowToken.getType()==LexerType.INTTK){
            String bType = nowToken.getValue();
            return new BTypeNode(bType);
        }
        else{
            System.err.println("BType error");
            return null;
        }
    }
    public VarDeclNode parseVarDecl() {
        // VarDecl → [ 'static' ] BType VarDef { ',' VarDef } ';' // i
//    System.out.println("Parsing vardecl, currentToken: " + nowToken.getValue()+" "+nowToken.getType());
        BTypeNode bTypeNode;
        boolean isStatic = false;
        List<VarDefNode> varDefNodeList = new ArrayList<>();
        if(nowToken.getType()==LexerType.STATICTK){
            getToken();
            isStatic = true;
        }
        bTypeNode = parseBType();
        getToken();
        if (bTypeNode == null) {
            System.err.println("VarDecl error: missing BType");
            return null;
        }

        VarDefNode varDefNode = parseVarDef();
        System.out.println("222");
        if (varDefNode != null){

            varDefNodeList.add(varDefNode);

            if(!isStatic){ symbolBuilder.buildVar(bTypeNode, varDefNode);
            }

            else{

                symbolBuilder.buildStatic(bTypeNode, varDefNode);
            }
        }


        while (nowToken.getType() == LexerType.COMMA) {
            getToken();
            varDefNode = parseVarDef();
            if (varDefNode != null){
                varDefNodeList.add(varDefNode);
                if(!isStatic){ symbolBuilder.buildVar(bTypeNode, varDefNode);
                }

                else{

                    symbolBuilder.buildStatic(bTypeNode, varDefNode);
                }
            }

        }
        if (nowToken.getType() == LexerType.SEMICN) {
            //System.out.println("VarDecl complete");
            getToken();
        } else {
            //错误处理
            errorManager.addError(tokenList.get(index-2).getLinenum(), ErrorType.I);
        }

        return new VarDeclNode(isStatic,bTypeNode, varDefNodeList);
    }
    public VarDefNode parseVarDef(){
        //VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal // k
        //System.out.println("Parsing vardef, currentToken: " + nowToken.getValue()+" "+nowToken.getType());
        ConstExpNode constExpNode=null;
        InitValNode initValNode;
        String identNode;
        int linenum;

        if(nowToken.getType()==LexerType.IDENFR){
            identNode = nowToken.getValue();
            linenum=nowToken.getLinenum();
            getToken();
            //System.out.println("newtoken " + nowToken.getValue()+" "+nowToken.getType());
        }
        else{
            System.err.println("VarDef error: missing identifier");
            return null;
        }
        if(nowToken.getType()==LexerType.LBRACK) {
            getToken();
            constExpNode = parseConstExp();
            if (nowToken.getType() == LexerType.RBRACK) {
                getToken();
            } else {
                errorManager.addError(tokenList.get(index-2).getLinenum(), ErrorType.K);
            }
            if(nowToken.getType()==LexerType.ASSIGN){
                getToken();
                initValNode = parseInitVal();
                return new VarDefNode(identNode, constExpNode, initValNode);
            }
            else{
                //System.out.println("VarDef without initialization");
                VarDefNode varDefNode= new VarDefNode(identNode, constExpNode);
                return varDefNode;
            }
        }
        else if(nowToken.getType()==LexerType.ASSIGN){
            getToken();


            initValNode = parseInitVal();
            VarDefNode varDefNode=new VarDefNode(identNode, constExpNode, initValNode);
            varDefNode.setLineno(linenum);
            return varDefNode;

        }

        else{
            VarDefNode varDefNode = new VarDefNode(identNode, constExpNode);
            varDefNode.setLineno(linenum);
            return varDefNode;
        }
    }
    public InitValNode parseInitVal(){
        //InitVal → Exp | '{' [ Exp { ',' Exp } ] '}'
        ExpNode expNode = null;
        List<ExpNode> expNodes = new ArrayList<>();
        if(nowToken.getType()==LexerType.LBRACE) {
            getToken();
            if (nowToken.getType() != LexerType.RBRACE) {
                expNode = parseExp();//已经调用了一次gettoken，读到逗号了。
                if (expNode != null)
                    expNodes.add(expNode);
                while (nowToken.getType() == LexerType.COMMA) {
                    getToken();
                    expNode = parseExp();
                    if (expNode != null)
                        expNodes.add(expNode);
                }
                if (nowToken.getType() == LexerType.RBRACE) {
                    getToken();
                } else {
                    System.err.println("InitVal error: missing '}'");
                    return null;
                }
            } else if (nowToken.getType() == LexerType.RBRACE) {
                //空初始化列表
                getToken();
                return new InitValNode(expNodes);
            }
            return new InitValNode(expNodes);
        }
        else{
            expNode = parseExp();
            if(expNode==null){
                System.err.println("InitVal error: invalid Exp");
                return null;
            }
            else
                return new InitValNode(expNode);
        }
    }
    public FuncDefNode parseFuncDef(){
        //FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // j
        FuncTypeNode funcTypeNode;
        String identNode;
        FuncFParamsNode funcFParamsNode=null;
        BlockNode blockNode;
        funcTypeNode = parseFuncType();
        getToken();
        int lineno;
        if(nowToken.getType()==LexerType.IDENFR){
            identNode = nowToken.getValue();
            lineno=nowToken.getLinenum();
            getToken();
        }
        else{
            System.err.println("FuncDef error: missing identifier");
            return null;
        }
        if(nowToken.getType()==LexerType.LPARENT){
            getToken();
        }
        else{
            System.err.println("FuncDef error: missing '('");
            return null;
        }
        if(nowToken.getType()==LexerType.INTTK){
            funcFParamsNode = parseFuncFParams();
        }
        if(nowToken.getType()==LexerType.RPARENT){
            getToken();
        }
        else{
            errorManager.addError(tokenList.get(index-2).getLinenum(), ErrorType.J);
        }
        FuncDefNode funcDefNode = new FuncDefNode(funcTypeNode, identNode, funcFParamsNode);
        symbolBuilder.buildFunc(funcTypeNode,identNode,funcFParamsNode,lineno);
        scopeStack.enterScope();
        if(funcFParamsNode!=null){
            symbolBuilder.buildPara(funcFParamsNode);
        }
        blockNode = parseBlockForFunc();
        boolean isReturn=false;
        if(!blockNode.getBlockItemNodes().isEmpty()){
            BlockItemNode blockItemNode = blockNode.getBlockItemNodes().get(blockNode.getBlockItemNodes().size() - 1);
            if(blockItemNode.getStmtNode()!=null)
            {
                StmtNode stmtNode = blockItemNode.getStmtNode();
                if (stmtNode.isReturn()) {
                    isReturn = true;
                }
            }
        }
        if(!isReturn){
            if(errorManager.inIntFunc){
                errorManager.addError(tokenList.get(index-2).getLinenum(), ErrorType.G);
            }
        }
        errorManager.inIntFunc = false;
        errorManager.inVoidFunc = false;
        funcDefNode=new FuncDefNode(funcTypeNode,identNode,funcFParamsNode,blockNode);
        scopeStack.exitScope();
        return funcDefNode;
    }
    public MainFuncDefNode parseMainFuncDef(){
        //MainFuncDef → 'int' 'main' '(' ')' Block // j
        BlockNode blockNode;
        if(nowToken.getType()!=LexerType.INTTK){
            System.err.println("MainFuncDef error: missing 'int'");
            return null;
        }else{
            getToken();
        }
        if(nowToken.getType()!=LexerType.MAINTK){
            System.err.println("MainFuncDef error: missing 'main'");
            return null;}
        else{
            getToken();
        }
        if(nowToken.getType()!=LexerType.LPARENT){
            System.err.println("MainFuncDef error: missing '('");
            return null;}
        else{
            getToken();
        }
        if(nowToken.getType()!=LexerType.RPARENT){
            //j型错误处理
            errorManager.addError(tokenList.get(index-2).getLinenum(), ErrorType.J);
        }
        else{
            getToken();
        }
        errorManager.inIntFunc=true;
        scopeStack.enterScope();
        blockNode = parseBlockForFunc();
        boolean isReturn = false;
        // 判断函数Block中的最后一条语句是否为return语句
        if (!blockNode.getBlockItemNodes().isEmpty()) {
            BlockItemNode blockItemNode = blockNode.getBlockItemNodes().get(blockNode.getBlockItemNodes().size() - 1);
            if (blockItemNode.getStmtNode() != null) {
                StmtNode stmtNode = blockItemNode.getStmtNode();
                if (stmtNode.isReturn()) {
                    isReturn = true;
                }
            }
        }
        if (!isReturn) {
            // index-1是main函数的左括号前的token
            errorManager.addError(tokenList.get(index - 1).getLinenum(), ErrorType.G);
        }
        errorManager.inIntFunc=false;
        scopeStack.exitScope();
        MainFuncDefNode mainFuncDefNode=new MainFuncDefNode(blockNode);
        return mainFuncDefNode;
    }
    public FuncTypeNode parseFuncType() {
        //FuncType → 'void' | 'int'
        if (nowToken.getType() == LexerType.VOIDTK || nowToken.getType() == LexerType.INTTK) {
            String funcType = nowToken.getValue();
            if(nowToken.getType() == LexerType.VOIDTK){
                errorManager.inVoidFunc=true;
            }
            else if(nowToken.getType() == LexerType.INTTK){
                errorManager.inIntFunc=true;
            }
            return new FuncTypeNode(funcType);
        } else {
            System.err.println("FuncType error");
            return null;
        }
    }
    public FuncFParamsNode parseFuncFParams(){
        // FuncFParams → FuncFParam { ',' FuncFParam }
        FuncFParamNode funcFParamNode;
        List<FuncFParamNode> funcFParamsNode = new ArrayList<>();
        funcFParamNode = parseFuncFParam();
        if(funcFParamNode!=null)
            funcFParamsNode.add(funcFParamNode);
        while(nowToken.getType()==LexerType.COMMA){
            getToken();
            funcFParamNode = parseFuncFParam();
            if(funcFParamNode!=null)
                funcFParamsNode.add(funcFParamNode);
        }
        return new FuncFParamsNode(funcFParamsNode);
    }
    public FuncFParamNode parseFuncFParam(){
        // FuncFParam → BType Ident ['[' ']']  //k
        BTypeNode bTypeNode;
        String identNode;
        int linenum;

        bTypeNode = parseBType();
        getToken();
        if(nowToken.getType()==LexerType.IDENFR){
            identNode = nowToken.getValue();
            linenum=nowToken.getLinenum();
            getToken();
        }
        else{
            System.err.println("FuncFParam error: missing identifier");
            return null;
        }
        if(nowToken.getType()==LexerType.LBRACK){
            getToken();
            if(nowToken.getType()==LexerType.RBRACK){
                getToken();
                //return new FuncFParamNode(bTypeNode, identNode, true);
            }
            else{
                errorManager.addError(tokenList.get(index-2).getLinenum(), ErrorType.K);
            }
            FuncFParamNode funcFParamNode= new FuncFParamNode(bTypeNode, identNode, true);
            funcFParamNode.setLineno(linenum);
            return funcFParamNode;
        }
        else{
            FuncFParamNode funcFParamNode= new FuncFParamNode(bTypeNode, identNode, false);
            funcFParamNode.setLineno(linenum);
            return funcFParamNode;
        }
    }
    public BlockNode parseBlock() {
        List<BlockItemNode> blockItemNodes = new ArrayList<>();
        scopeStack.enterScope();
        if (nowToken.getType() == LexerType.LBRACE) {
            getToken();
        } else {
            System.err.println("Block error");
            return null;
        }
        int safetyCounter = 0;
        while (nowToken.getType() != LexerType.RBRACE) {
            System.out.println("Current token in parseBlock: " + nowToken.getValue() + ", type: " + nowToken.getType());
            if (safetyCounter++ > 1000) { // 假设最大解析次数为 10000
                throw new RuntimeException("Infinite loop detected in parseBlock()");
            }
            BlockItemNode blockItemNode = parseBlockItem();
            if (blockItemNode != null) {
                blockItemNodes.add(blockItemNode);
            }

        }
        getToken();
        scopeStack.exitScope();
        return new BlockNode(blockItemNodes, scopeStack.getScopeNumberCurrent()); // 作用域编号可以直接设置为 0 或忽略
    }
    public BlockNode parseBlockForFunc(){
        List<BlockItemNode> blockItemNodes = new ArrayList<>();
        if(nowToken.getType()==LexerType.LBRACE) {
            getToken();
        }else{
            System.err.println("Block error");
            return null;
        }
        while(nowToken.getType()!=LexerType.RBRACE){
            BlockItemNode blockItemNode = parseBlockItem();
            if(blockItemNode!=null){
                blockItemNodes.add(blockItemNode);
            }
        }
        getToken();
        return new BlockNode(blockItemNodes, scopeStack.getScopeNumberBefore());
    }
    public BlockItemNode parseBlockItem() {
        // BlockItem → Decl | Stmt
        DeclNode declNode;
        StmtNode stmtNode;
        //System.out.println("Parsing BlockItem, currentToken: " + nowToken.getValue()+" "+nowToken.getType());
        if (nowToken.getType() == LexerType.CONSTTK || nowToken.getType() == LexerType.INTTK|| nowToken.getType() == LexerType.STATICTK) {
            declNode = parseDecl();
            return new BlockItemNode(declNode);
        } else {
            stmtNode = parseStmt();
            return new BlockItemNode(stmtNode);
        }
    }
    public StmtNode parseStmt() {
        /*
         * Stmt → LVal '=' Exp ';'
         * | [Exp] ';'
         * | Block
         * | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
         * | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
         * | 'break' ';' | 'continue' ';'
         * | 'return' [Exp] ';'
         * | 'printf''('StringConst {','Exp}')'';'
         * |  Cond '?' Exp ':' Exp ';'
         */
        switch (nowToken.getType()) {
            case INTCON, LPARENT, PLUS, MINU, NOT,IDENFR:
                return parseCondStmt();
            case SEMICN:
                return parseExpressionStmt();
            case LBRACE:
                return new StmtNode(parseBlock());
            case IFTK:
                return parseIfStmt();
            case FORTK:
                return parseForToStmt();
            case BREAKTK:
                return parseBreakStmt();
            case CONTINUETK:
                return parseContinueStmt();
            case RETURNTK:
                return parseReturnStmt();
            case PRINTFTK:
                return parsePrintfStmt();
            default:
                System.err.println("Stmt errorrrr");
                return null;
        }
    }
    private StmtNode parseCondStmt(){
        int tempIndex = index;
        CondNode condNode=parseCond();
        if(nowToken.getType()==LexerType.QUES){
            getToken();
            ExpNode trueExp=parseExp();
            if(nowToken.getType()==LexerType.COLON){
                getToken();
                ExpNode falseExp=parseExp();
                if(nowToken.getType()==LexerType.SEMICN){
                    getToken();
                    return new StmtNode(condNode,trueExp,falseExp);
                } else {
                    //i类错误
                    errorManager.addError(tokenList.get(index-2).getLinenum(), ErrorType.I);
                    return null;
                }
            }
            else{
                System.err.println("Ternary Stmt error: Expected ':'");
                return null;
            }
        }
        else{
            index=tempIndex-1;
            getToken();
            if(nowToken.getType()==LexerType.IDENFR){
                return parseAssignmentOrExpStmt();
            }
            else{
                return parseExpressionStmt();
            }
        }
    }
    private StmtNode parseExpressionStmt(){
        ExpNode expNode=null;
        if(nowToken.getType()!=LexerType.SEMICN){
            expNode=parseExp();
        }
        if(nowToken.getType()==LexerType.SEMICN){
            getToken();
        } else {
            //i类错误
            errorManager.addError(tokenList.get(index-2).getLinenum(), ErrorType.I);
        }
        return new StmtNode(expNode);
    }
    private StmtNode parseAssignmentOrExpStmt() {
        int tempIndex = index;
        LValNode lValNode = parseLVal();

        if (nowToken.getType() == LexerType.ASSIGN||nowToken.getType()== LexerType.ADDASSI||nowToken.getType()== LexerType.MINUASSI
                ||nowToken.getType()== LexerType.MULTASSI||nowToken.getType()== LexerType.DIVASSI) {
            String operator = nowToken.getValue();
            getToken();
            return parseAssignmentStmt(lValNode,operator);
        } else {
            // 回退，解析为表达式
            index = tempIndex - 1;
            getToken();
            return parseExpressionStmt();
        }
    }

    private StmtNode parseAssignmentStmt(LValNode lValNode,String operator) {
        Symbol symbol = scopeStack.lookup(lValNode.getIdentNode());
        errorManager.checkConstantAssignment(symbol, lValNode.getLineno());
        ExpNode expNode = parseExp();
        if (nowToken.getType() == LexerType.SEMICN) {
            getToken();
        } else {
            errorManager.addError(tokenList.get(index - 2).getLinenum(), ErrorType.I);
        }
        return new StmtNode(lValNode, operator, expNode);

    }

    private StmtNode parseForToStmt() {
        ForStmtNode forStmtNode1 = null;
        CondNode condNode = null;
        ForStmtNode forStmtNode2 = null;
        StmtNode stmtNode1 = null;

        getToken();
        if (nowToken.getType() == LexerType.LPARENT) {
            getToken();
        } else {
            System.err.println("Stmt error");
            return null;
        }
        if (nowToken.getType() != LexerType.SEMICN) {
            forStmtNode1 = parseForStmt();
        }
        if (nowToken.getType() == LexerType.SEMICN) {
            getToken();
        } else {
            System.err.println("Stmt error");
            return null;
        }
        if (nowToken.getType() != LexerType.SEMICN) {
            // [Cond]
            condNode = parseCond();
        }
        if (nowToken.getType() == LexerType.SEMICN) {
            getToken();
        } else {
            System.err.println("Stmt error");
            return null;
        }
        // 这里之前对文法解析有问题
        if (nowToken.getType() != LexerType.RPARENT) {
            forStmtNode2 = parseForStmt();
        }
        if (nowToken.getType() == LexerType.RPARENT) {
            getToken();
        } else {
            System.err.println("Stmt error");
            return null;
        }
        errorManager.inFor++;
        stmtNode1 = parseStmt();
        errorManager.inFor--;
        return new StmtNode(forStmtNode1, condNode, forStmtNode2, stmtNode1);
    }

    private StmtNode parseBreakStmt() {
        if(!errorManager.inFor()){
            errorManager.addError(nowToken.getLinenum(),ErrorType.M);
        }
        getToken();
        if (nowToken.getType() == LexerType.SEMICN) {
            getToken();
        } else {
            errorManager.addError(tokenList.get(index-2).getLinenum(), ErrorType.I);
        }
        return new StmtNode("break");
    }

    private StmtNode parseIfStmt() {
        CondNode condNode = null;
        StmtNode stmtNode1 = null;
        StmtNode stmtNode2 = null;
        boolean isNewIf=false;
        BTypeNode btypeNode=null;
        InitValNode initValNode=null;
        String ident=null;
        getToken();
        if (nowToken.getType() == LexerType.LPARENT) {
            getToken();
        } else {
            System.err.println("Stmt error");
            return null;
        }
        if(nowToken.getType()==LexerType.INTTK){
            isNewIf=true;
            System.out.println(nowToken.getType());
            btypeNode=parseBType();
            getToken();
            System.out.println(nowToken.getType());
            ident=nowToken.getValue();
            getToken();
            System.out.println(nowToken.getType());
            getToken();
            System.out.println(nowToken.getType());
            initValNode= parseInitVal();

        }
        else{
            condNode= parseCond();}
        //condNode = parseCond();//这个地方出错了，应该把整个表达式解析出来，这里nowtoken应该是括号了
        //System.out.println(nowToken.getValue()+"if");
        if (nowToken.getType() == LexerType.RPARENT) {
            getToken();
        } else {
            //System.out.println(nowToken.getValue()+"if1");
            errorManager.addError(tokenList.get(index-2).getLinenum(), ErrorType.J);
        }
        stmtNode1 = parseStmt();
        //System.out.println(nowToken.getValue()+" if2");
        if (nowToken.getType() == LexerType.ELSETK) {
            getToken();
            stmtNode2 = parseStmt();
        }
        if(isNewIf==false){return new StmtNode(condNode, stmtNode1, stmtNode2);}
        else{
            System.out.println("111");
            return new StmtNode(btypeNode,ident,initValNode,stmtNode1,stmtNode2);
        }
    }

    private StmtNode parseContinueStmt() {
        if(!errorManager.inFor()){
            errorManager.addError(nowToken.getLinenum(),ErrorType.M);
        }
        getToken();
        if (nowToken.getType() == LexerType.SEMICN) {
            getToken();
        } else {
            errorManager.addError(tokenList.get(index-2).getLinenum(), ErrorType.I);
        }
        return new StmtNode("continue");
    }

    private StmtNode parseReturnStmt() {
        ExpNode expNode = null;
        getToken();
        if (nowToken.getType() != LexerType.SEMICN && ExpNode.contains(nowToken.getType())) {
            expNode = parseExp();
        }
        if (nowToken.getType() == LexerType.SEMICN) {
            getToken();
        } else {
            errorManager.addError(tokenList.get(index-2).getLinenum(), ErrorType.I);
        }
        if(errorManager.inVoidFunc&&expNode!=null){
            errorManager.addError(tokenList.get(index - 2).getLinenum(),ErrorType.F);
        }
        return new StmtNode(expNode, true);
    }

    private StmtNode parsePrintfStmt() {
        List<ExpNode> expNodeList = new ArrayList<>();
        ExpNode expNode;
        String stringConst;

        getToken();
        if (nowToken.getType() == LexerType.LPARENT) {
            getToken();
        } else {
            System.err.println("Stmt error");
            return null;
        }
        if (nowToken.getType() == LexerType.STRCON) {
            stringConst = nowToken.getValue();
            getToken();
        } else {
            System.err.println("Stmt error");
            return null;
        }
        while (nowToken.getType() == LexerType.COMMA) {
            getToken();
            expNode = parseExp();
            if (expNode != null) {
                expNodeList.add(expNode);
            }
        }
        int linenum=nowToken.getLinenum();
        errorManager.checkPrintfType(stringConst, expNodeList, linenum);
        // J类错误
        if (nowToken.getType() == LexerType.RPARENT) {
            getToken();
        } else {
            errorManager.addError(tokenList.get(index-2).getLinenum(), ErrorType.J);
        }
        // I类错误
        if (nowToken.getType() == LexerType.SEMICN) {
            getToken();
        } else {
            errorManager.addError(tokenList.get(index-2).getLinenum(), ErrorType.I);
        }
        return new StmtNode(stringConst, expNodeList);
    }


    public ForStmtNode parseForStmt() {
        // ForStmt → LVal '=' Exp { ',' LVal '=' Exp }
        List<LValNode> lValNodes = new ArrayList<>();
        List<ExpNode> expNodes = new ArrayList<>();

        // 解析第一个 LVal '=' Exp
        LValNode lValNode = parseLVal();
        if (nowToken.getType() == LexerType.ASSIGN) {
            getToken();
        } else {
            System.err.println("ForStmt error: Expected '='");
            return null;
        }
        String ident=lValNode.getIdentNode();
        Symbol symbol=scopeStack.lookup(ident);
        errorManager.checkConstantAssignment(symbol, lValNode.getLineno());
        ExpNode expNode = parseExp();
        lValNodes.add(lValNode);
        expNodes.add(expNode);

        // 解析后续的 { ',' LVal '=' Exp }
        while (nowToken.getType() == LexerType.COMMA) {
            getToken(); // 跳过 ','
            lValNode = parseLVal();
            if (nowToken.getType() == LexerType.ASSIGN) {
                getToken();
            } else {
                System.err.println("ForStmt error: Expected '=' after ','");
                return null;
            }
            ident=lValNode.getIdentNode();
        symbol=scopeStack.lookup(ident);
        errorManager.checkConstantAssignment(symbol, lValNode.getLineno());
            expNode = parseExp();
            lValNodes.add(lValNode);
            expNodes.add(expNode);
        }

        // 返回 ForStmtNode，包含所有的 LVal 和 Exp
        return new ForStmtNode(lValNodes, expNodes);
    }
    public ExpNode parseExp(){
        // Exp → AddExp
        return new ExpNode(parseAddExp());
    }
    public CondNode parseCond(){
        // Cond → LOrExp
        //System.out.println(nowToken.getValue()+"cond");
        return new CondNode(parseLOrExp());
    }
    public LValNode parseLVal(){
        // LVal → Ident { '[' Exp ']' }
        String ident;
        ExpNode expNode;
        int linenum;
        if(nowToken.getType()==LexerType.IDENFR){

            ident = nowToken.getValue();
            linenum=nowToken.getLinenum();
            if(!ident.equals("getint")&&scopeStack.lookup(ident)==null){
                errorManager.addError(nowToken.getLinenum(), ErrorType.C);
            }
            getToken();
        }
        else{
            System.err.println("LVal error: missing identifier");
            return null;
        }
        if(nowToken.getType()==LexerType.LBRACK){
            getToken();
            expNode = parseExp();
            if(nowToken.getType()==LexerType.RBRACK){
                getToken();
            }
            else{
                errorManager.addError(tokenList.get(index-2).getLinenum(), ErrorType.K);
                //return new LValNode(ident, expNode);
            }
            LValNode lValNode= new LValNode(ident, expNode);
            lValNode.setLineno(linenum);
            return lValNode;
        }
        else{

            LValNode lValNode= new LValNode(ident);
            lValNode.setLineno(linenum);
            return lValNode;
        }
    }
    public PrimaryExpNode parsePrimaryExp(){
        //PrimaryExp → '(' Exp ')' | LVal | Number // j
        ExpNode expNode;
        LValNode lValNode;
        NumberNode numberNode;
        if(nowToken.getType()==LexerType.LPARENT){
            getToken();
            expNode = parseExp();
            if(nowToken.getType()==LexerType.RPARENT){
                getToken();
            }
            else{
                errorManager.addError(tokenList.get(index-2).getLinenum(), ErrorType.J);
            }
            return new PrimaryExpNode(expNode);
        }
        else if(nowToken.getType()==LexerType.INTCON){
            //System.out.println(nowToken.getValue()+" primary");
            numberNode= parseNumber();
            return new PrimaryExpNode(numberNode);
        }
        else if(nowToken.getType()==LexerType.IDENFR){
            lValNode = parseLVal();
            return new PrimaryExpNode(lValNode);
        }
        else{
            System.err.println("PrimaryExp error");
            return null;
        }
    }
    public NumberNode parseNumber(){
        //Number → IntConst
        if(nowToken.getType()==LexerType.INTCON){
            String numberValue = nowToken.getValue();
            NumberNode numberNode = new NumberNode(numberValue);
            //System.out.println(nowToken.getValue()+" number1");
            getToken();
            //System.out.println(nowToken.getValue()+" number2");
            return numberNode;
        }
        else{
            System.err.println("Number error");
            return null;
        }
    }
    public UnaryExpNode parseUnaryExp() {
        // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
        if (nowToken.getType() == LexerType.IDENFR && nextToken.getType() == LexerType.LPARENT) {
            // Ident '(' [FuncRParams] ')'
            String identNode = nowToken.getValue();
            int lineno = nowToken.getLinenum();
            FuncRParamsNode funcRParamsNode = null;
            if(!identNode.equals("getint") &&scopeStack.lookup(identNode)==null){

                errorManager.addError(nowToken.getLinenum(), ErrorType.C);
            }
            getToken(); // 获取 '('
            getToken(); // 跳过 Ident

            // 如果不是右括号，解析函数参数
            if (nowToken.getType() != LexerType.RPARENT && ExpNode.contains(nowToken.getType())) {
                funcRParamsNode = parseFuncRParams();
                errorManager.checkFuncParams(identNode, lineno, funcRParamsNode); // 检查参数匹配
            }

            if (nowToken.getType() == LexerType.RPARENT) {
                getToken();
            } else {
                errorManager.addError(tokenList.get(index - 2).getLinenum(), ErrorType.J);
            }
            return new UnaryExpNode(identNode, funcRParamsNode);
        } else if (!isUnaryOperator(nowToken.getType())) {
            // PrimaryExp
            return new UnaryExpNode(parsePrimaryExp());
        } else {
            // UnaryOp UnaryExp
            UnaryOpNode unaryOpNode = parseUnaryOp();
            getToken();
            UnaryExpNode unaryExpNode = parseUnaryExp();
            return new UnaryExpNode(unaryOpNode, unaryExpNode);
        }
    }
    public boolean isUnaryOperator(LexerType type) {
        return type == LexerType.PLUS || type == LexerType.MINU || type == LexerType.NOT||type==LexerType.MULPLUS;
    }
    public UnaryOpNode parseUnaryOp() {
        // UnaryOp → '+' | '-' | '!'
        switch(nowToken.getType()){
            case PLUS:
                return new UnaryOpNode("+");
            case MINU:
                return new UnaryOpNode("-");
            case NOT:
                return new UnaryOpNode("!");
            case MULPLUS:
                return new UnaryOpNode("++");
            default:
                System.err.println("UnaryOp error");
                return null;
        }
    }
    public FuncRParamsNode parseFuncRParams() {
        // FuncRParams → Exp { ',' Exp }
        ExpNode expNode;
        List<ExpNode> expNodeList = new ArrayList<>();
        expNode = parseExp();
        if (expNode != null)
            expNodeList.add(expNode);
        while (nowToken.getType() == LexerType.COMMA) {
            getToken();
            expNode = parseExp();
            if (expNode != null)
                expNodeList.add(expNode);
        }
        return new FuncRParamsNode(expNodeList);
    }
    public RelExpNode parseRelExp(){
        // RelExp → AddExp {('<' | '<=' | '>' | '>='') AddExp}
        List<AddExpNode> addExpNodes = new ArrayList<>();
        List<String> relOps = new ArrayList<>();
        AddExpNode addExpNode = parseAddExp();
        if (addExpNode != null)
            addExpNodes.add(addExpNode);
        while (nowToken.getType() == LexerType.LSS || nowToken.getType() == LexerType.LEQ ||
                nowToken.getType() == LexerType.GRE || nowToken.getType() == LexerType.GEQ) {
            relOps.add(nowToken.getValue());
            getToken();
            addExpNode = parseAddExp();
            if(addExpNode!=null)
                addExpNodes.add(addExpNode);
        }
        //System.out.println(nowToken.getValue()+" relexp");
        return new RelExpNode(addExpNodes, relOps);
    }
    public MulExpNode parseMulExp(){
        // MulExp → UnaryExp {('\*' | '/' | '%') UnaryExp}
        List<UnaryExpNode> unaryExpNodes=new ArrayList<>();
        List<String> mulOps=new ArrayList<>();
        UnaryExpNode unaryExpNode = parseUnaryExp();
        if(unaryExpNode!=null)
            unaryExpNodes.add(unaryExpNode);
        while(nowToken.getType()==LexerType.MULT||nowToken.getType()==LexerType.DIV||nowToken.getType()==LexerType.MOD||nowToken.getType()==LexerType.BITAND) {
            mulOps.add(nowToken.getValue());
            getToken();
            unaryExpNode = parseUnaryExp();
            if(unaryExpNode!=null)
                unaryExpNodes.add(unaryExpNode);
        }
        //System.out.println(nowToken.getValue()+" mulexp");
        return new MulExpNode(unaryExpNodes, mulOps);
    }
    public AddExpNode parseAddExp(){
        // AddExp → MulExp {('+' | '-') MulExp}
        List<MulExpNode> mulExpNodes=new ArrayList<>();
        List<String> addOps=new ArrayList<>();
        MulExpNode mulExpNode = parseMulExp();
        if(mulExpNode!=null)
            mulExpNodes.add(mulExpNode);
        while(nowToken.getType()==LexerType.PLUS||nowToken.getType()==LexerType.MINU) {
            addOps.add(nowToken.getValue());
            getToken();
            mulExpNode = parseMulExp();
            if(mulExpNode!=null)
                mulExpNodes.add(mulExpNode);
        }
        //System.out.println(nowToken.getValue()+" addexp");
        return new AddExpNode(mulExpNodes, addOps);
    }
    public EqExpNode parseEqExp(){
        //EqExp → RelExp {('==' | '!=') RelExp}
        List<RelExpNode> relExpNodes=new ArrayList<>();
        List<String> eqOps=new ArrayList<>();
        RelExpNode relExpNode = parseRelExp();
        if(relExpNode!=null)
            relExpNodes.add(relExpNode);
        while(nowToken.getType()==LexerType.EQL||nowToken.getType()==LexerType.NEQ) {
            eqOps.add(nowToken.getValue());
            getToken();
            relExpNode = parseRelExp();
            if(relExpNode!=null)
                relExpNodes.add(relExpNode);
        }
        //System.out.println(nowToken.getValue()+" eqexp");
        return new EqExpNode(relExpNodes, eqOps);
    }
    public LAndExpNode parseLAndExp() {
        //LAndExp → EqExp { '&&' EqExp }
        List<EqExpNode> eqExpNodes = new ArrayList<>();
        EqExpNode eqExpNode = parseEqExp();
        if (eqExpNode != null)
            eqExpNodes.add(eqExpNode);
        while (nowToken.getType() == LexerType.AND) {
            getToken();
            eqExpNode = parseEqExp();
            if (eqExpNode != null)
                eqExpNodes.add(eqExpNode);
        }
        //System.out.println(nowToken.getValue()+" landexp");
        return new LAndExpNode(eqExpNodes);
    }
    public LOrExpNode parseLOrExp(){
        // LOrExp → LAndExp { '||' LAndExp }
        List<LAndExpNode> lAndExpNodes = new ArrayList<>();
        LAndExpNode lAndExpNode = parseLAndExp();
        if (lAndExpNode != null)
            lAndExpNodes.add(lAndExpNode);
        while (nowToken.getType() == LexerType.OR) {
            getToken();
            lAndExpNode = parseLAndExp();
            if (lAndExpNode != null)
                lAndExpNodes.add(lAndExpNode);
        }
        //System.out.println(nowToken.getValue()+" lorexp");
        return new LOrExpNode(lAndExpNodes);
    }
    public ConstExpNode parseConstExp() {
        // ConstExp → AddExp
        return new ConstExpNode(parseAddExp());
    }

    public void setError(Error error) {
        this.error = error;
    }
}








