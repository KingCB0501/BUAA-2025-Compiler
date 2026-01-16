package Utils;
import java.io.FileWriter;
import java.io.IOException;

public class Printer {
    private static final String lexerOutput = "lexer.txt";
    private static final String errorOutput = "error.txt";

    public Printer() {

    }

    public static void printLexer(String output) {
        try (FileWriter fileWriter = new FileWriter(lexerOutput)) {
            fileWriter.write(output);
        } catch (IOException e) {
            System.err.println("Error writing lexer to file " + lexerOutput);
        }
    }

    public static void printError(String output) {
        try(FileWriter fileWriter = new FileWriter(errorOutput)) {
            fileWriter.write(output);
        }catch (IOException e) {
            System.err.println("Error writing error to file " + errorOutput);
        }
    }

}
