package llvm;

import llvm.UserClass.Function;
import llvm.UserClass.GlobalVar;
import llvm.UserClass.StringLiteral;
import llvm.type.BaseIntegerType;
import llvm.type.LLVMType;
import llvm.type.PointerType;

import java.util.ArrayList;

public class Module {
    private ArrayList<StringLiteral> stringLiterals;
    private ArrayList<GlobalVar> globalVars;
    private ArrayList<Function> functions;


    public Module() {
        this.stringLiterals = new ArrayList<>();
        this.globalVars = new ArrayList<>();
        this.functions = new ArrayList<>();
    }

    public void addStringLiteral(StringLiteral stringLiteral) {
        stringLiterals.add(stringLiteral);
    }

    public void addGlobalVar(GlobalVar globalVar) {
        globalVars.add(globalVar);
    }

    public void addFunction(Function function) {
        functions.add(function);
    }

    public static Function getInt = new Function("getint", LLVMType.INT32, new ArrayList<>());
    public static Function putInt = new Function("putint", LLVMType.VOID, new ArrayList<BaseIntegerType>() {{
        add(LLVMType.INT32);
    }});
    public static Function putChar = new Function("putch", LLVMType.VOID, new ArrayList<BaseIntegerType>() {{
        add(LLVMType.INT32);
    }});
    public static Function putStr = new Function("putstr", LLVMType.VOID, new ArrayList<BaseIntegerType>() {{
        add(new PointerType(LLVMType.INT8));
    }});

    public String declareLibFunc() {
        return "declare i32 @getint()\n" +
                "declare void @putint(i32)\n" +
                "declare void @putch(i32)\n" +
                "declare void @putstr(i8*)\n";
    }

    //TODO 等待完成
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(declareLibFunc());
        for (GlobalVar gv : globalVars) {
            sb.append(gv.toString());
        }
        sb.append("\n");
        for (StringLiteral sl : stringLiterals) {
            sb.append(sl.toString());
        }
        sb.append("\n");
        for (Function f : functions) {
            sb.append(f.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
