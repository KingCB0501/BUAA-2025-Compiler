import Utils.ErrorLog;
import Utils.Printer;
import backend.MipsGenerator;
import backend.MipsModule;
import frontend.Checker.Checker;
import frontend.Checker.SymbolTable;
import frontend.Lexer.Lexer;
import frontend.Lexer.TokenStream;
import frontend.Parser.AST.CompUnit;
import frontend.Parser.Parser;
import java.io.IOException;
import llvm.LlvmModule;
import llvm.Visitor;
import optimize.Mem2Reg;
import optimize.RegAlloca;
import optimize.RemovePhi;
import optimize.preOptimize;


public class Compiler {
    public static void main(String[] args) throws IOException {
        String testFileName = "testfile.txt";
        Boolean optimize = true;
        Lexer lexer = new Lexer(testFileName);
        TokenStream tokenStream = lexer.lex();
        Printer.printLexer(tokenStream.toString());

        CompUnit compUnit = Parser.getInstance(tokenStream).parseCompUnit();
        Printer.printParser(compUnit.toString());

        Checker checker = new Checker();
        SymbolTable rootSymbolTable = checker.getRootTable(compUnit);
        Printer.printChecker(rootSymbolTable.toString());

        if (ErrorLog.getInstance().getErrorCount() != 0) {
            Printer.printError(ErrorLog.getInstance().toString());
            return;
        }

        LlvmModule llvmModule = Visitor.getInstance().visitCompunit(compUnit);
        Printer.printLlvmIR(llvmModule.toString());
        if (optimize) {
            preOptimize.deleteDeadCode(llvmModule);   // 删除死的BasicBlock与Func     -----没问题
            preOptimize.analyzeFuncDom(llvmModule);    //  ----应该没问题
            Mem2Reg.work(llvmModule);
            Printer.printLlvmMidOptimize(llvmModule.toString());
            Printer.printLlvmIR(llvmModule.toString());
            RegAlloca.getInstance(llvmModule).alloca();     // 没问题
            RemovePhi.removePhi(llvmModule);
            Printer.printLlvmOptimize(llvmModule.toString());
        }
        MipsModule mipsModule = MipsGenerator.getInstance().tranLlvmModule(llvmModule);
        Printer.printMips(mipsModule.toString());
        // 有error则输出

    }
}
