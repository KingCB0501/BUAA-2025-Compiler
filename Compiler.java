import Utils.ErrorLog;
import Utils.Printer;
import frontend.Checker.Checker;
import frontend.Checker.SymbolTable;
import frontend.Lexer.Lexer;
import frontend.Lexer.TokenStream;
import frontend.Parser.AST.CompUnit;
import frontend.Parser.Parser;
import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        String testFileName = "testfile.txt";
        Lexer lexer = new Lexer(testFileName);
        TokenStream tokenStream = lexer.lex();

        CompUnit compUnit = Parser.getInstance(tokenStream).parseCompUnit();
        Checker checker = new Checker();
        SymbolTable rootSymbolTable = checker.getRootTable(compUnit);


//        Printer.printLexer(tokenStream.toString());
//        Printer.printParser(compUnit.toString());
        Printer.printChecker(rootSymbolTable.toString());
        // 有error则输出
        if (ErrorLog.getInstance().getErrorCount() != 0) {
            Printer.printError(ErrorLog.getInstance().toString());
        }
    }
}
