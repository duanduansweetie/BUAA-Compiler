import error.ErrorManager;
import java.io.IOException;
import lexer.LexerAnalyzer;
import parser.IrBuilder;
import parser.Parser;
import parser.SymbolBuilder;
import symbol.ScopeStack;
import llvmir.value.structure.Module;
import llvmir.Printer;
public class Compiler{
    public static void main(String[] args) throws IOException{
        ErrorManager errorManager = new ErrorManager();
        Module module=new Module();
        LexerAnalyzer lexer = new LexerAnalyzer(errorManager);
        ScopeStack scopeStack=new ScopeStack(errorManager);
        IrBuilder irBuilder=new IrBuilder(module,scopeStack);
        SymbolBuilder symbolBuilder=new SymbolBuilder(scopeStack,errorManager,irBuilder);
        Parser parser = new Parser(errorManager,scopeStack,symbolBuilder,irBuilder);
         errorManager.setScopeStack(scopeStack);
         Printer printer=new Printer(module);
        //词法分析
        try {
            lexer.init("testfile.txt", "lexer.txt");
            while (lexer.getsym() != null) {
            }
          lexer.showLexer();
//            errorManager.writeErrorsToFile("error.txt");
            lexer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //语法分析
    parser.setTokenList(lexer.getTokenList());
    try{
      parser.init("parser.txt");
      parser.showParser();
     parser.close();

        scopeStack.initBuffer("symbol.txt");
        scopeStack.showSymbolList();
        scopeStack.closeBuffer();

        errorManager.writeErrorsToFile("error.txt");
        //errorManager.showErrors();
    }catch(IOException e){
        e.printStackTrace();
    }
        //中间代码生成
        try {
            Printer.initBuffer("llvm_ir.txt");
            Printer.showIr();
            Printer.closeBuffer();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}