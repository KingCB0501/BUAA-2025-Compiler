import Utils.ErrorLog;
import Utils.Printer;
import frontend.Lexer.Lexer;
import frontend.Lexer.TokenStream;
import java.io.IOException;

public class Compiler {
    public static void main(String[] args) throws IOException {
        String testFileName = "testfile.txt";
        Lexer lexer = new Lexer(testFileName);
        TokenStream tokens = lexer.lex();

        // 输出lexer文件
        Printer.printLexer(tokens.toString());
        // 有error则输出
        if (ErrorLog.getInstance().getErrorCount() != 0) {
            Printer.printError(ErrorLog.getInstance().toString());
        }
    }
}
