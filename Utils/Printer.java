package Utils;

import frontend.Checker.Checker;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

public class Printer {
    private static final String lexerOutput = "lexer.txt";
    private static final String errorOutput = "error.txt";
    private static final String parserOutput = "parser.txt";
    private static final String checkOutput = "symbol.txt";
    private static final String LLVMOutput = "llvm_ir.txt";
    private static final String mipsOutput = "mips.txt";

    public Printer() {

    }

    public static void printLexer(String output) {
        try (FileWriter fileWriter = new FileWriter(lexerOutput)) {
            fileWriter.write(output);
        } catch (IOException e) {
            System.err.println("Error writing lexer to file " + lexerOutput);
        }
    }

    public static void printParser(String output) {
        try (FileWriter fileWriter = new FileWriter(parserOutput)) {
            fileWriter.write(output);
        } catch (IOException e) {
            System.err.println("Error writing parser to file " + parserOutput);
        }
    }

    public static void printChecker(String output) {
        try (FileWriter fileWriter = new FileWriter(checkOutput)) {
            fileWriter.write(output);
        } catch (IOException e) {
            System.err.println("Error writing checker to file " + parserOutput);
        }
    }

    public static void printError(String output) {
        try (FileWriter fileWriter = new FileWriter(errorOutput)) {
            fileWriter.write(output);
        } catch (IOException e) {
            System.err.println("Error writing error to file " + errorOutput);
        }
    }

    public static void printLlvmIR(String output) {
        try (FileWriter fileWriter = new FileWriter(LLVMOutput)) {
            fileWriter.write(output);
        } catch (IOException e) {
            System.err.println("Error writing LLVM IR to file " + LLVMOutput);
        }

    }

    public static void printMips(String output) {
        try (FileWriter fileWriter = new FileWriter(mipsOutput)) {
            fileWriter.write(output);
        } catch (IOException e) {
            System.err.println("Error writing mips to file " + mipsOutput);
        }
    }

}
