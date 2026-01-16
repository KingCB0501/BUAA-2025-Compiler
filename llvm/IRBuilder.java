package llvm;

import frontend.Lexer.Token;
import llvm.Const.Constant;
import llvm.Const.ConstantData;
import llvm.UserClass.BasicBlock;
import llvm.UserClass.Function;
import llvm.UserClass.GlobalVar;
import llvm.UserClass.StringLiteral;
import llvm.instruction.Alloca;
import llvm.instruction.Branch;
import llvm.instruction.Call;
import llvm.instruction.Compute;
import llvm.instruction.Getelemntptr;
import llvm.instruction.Icmp;
import llvm.instruction.Jump;
import llvm.instruction.Load;
import llvm.instruction.Ret;
import llvm.instruction.Store;
import llvm.instruction.Zext;
import llvm.type.BaseIntegerType;
import llvm.type.LLVMType;

import java.util.ArrayList;
import java.util.HashMap;

public class IRBuilder {
    protected LlvmModule llvmModule;

    private int namecnt;
    private int strcnt;

    private int globvar_cnt;

    protected Function curFunc;
    protected BasicBlock curBBlock;

    private HashMap<String, StringLiteral> stringLiterals = new HashMap<>();

    public IRBuilder() {
        this.llvmModule = new LlvmModule();
        this.namecnt = 0;
        this.strcnt = 0;
        this.curFunc = null;
        this.curBBlock = null;

        this.globvar_cnt = 0;
    }

    public LlvmModule getModule() {
        return this.llvmModule;
    }

    /**
     * 创建原本全局变量
     *
     * @param varName
     * @param type
     * @param init
     * @param isConst
     * @return
     */
    public GlobalVar makeGlobalVar(String varName, LLVMType type, Constant init, boolean isConst) {
//        GlobalVar globalvar = new GlobalVar(varName, type, init, isConst);
        // 因为会将某些静态局部变量提升为全局变量声明处，所以直接用全局变量名称赋名可能会重名
        String name = (globvar_cnt++) + "_" + varName;
        GlobalVar globalvar = new GlobalVar(name, type, init, isConst);
        llvmModule.addGlobalVar(globalvar);
        return globalvar;
    }

    /**
     * 创建由局部静态变量升级的全局变量，通过globvar_cnt命名
     *
     * @param type
     * @param init
     * @param isConst
     * @return
     */
    // static局部变量升级
    public GlobalVar makeGlobalVar(LLVMType type, Constant init, boolean isConst) {
//        GlobalVar globalvar = new GlobalVar(varName, type, init, isConst);
        // 因为会将某些静态局部变量提升为全局变量声明处，所以直接用全局变量名称赋名可能会重名
        String name = (globvar_cnt++) + "_" + "static";
        GlobalVar globalvar = new GlobalVar(name, type, init, isConst);
        llvmModule.addGlobalVar(globalvar);
        return globalvar;
    }

    // TODO这个init到底有什么用
    public Alloca makeAlloca(LLVMType type, Constant init) {   // const变量的区域存入init
        Alloca alloca = new Alloca(namecnt++, type, init);
        curBBlock.addInstruction(alloca);
        return alloca;
    }

    public Function makeFunc(String funcName, BaseIntegerType retType, ArrayList<BaseIntegerType> fParamTypes) {
        Function func = new Function(funcName, retType, fParamTypes);
        llvmModule.addFunction(func);
        return func;
    }

    public Alloca makeAlloca(LLVMType type) {
        Alloca alloca = new Alloca(namecnt++, type);
        curBBlock.addInstruction(alloca);
        return alloca;
    }


    // 因为跳转常常涉及到基本块，所以我们可以先创建，在判断留的一系列基本块结束之后再加入curFunc
    public BasicBlock makeBasicBlock() {
        BasicBlock basicBlock = new BasicBlock(namecnt++);
//        basicBlock.addInstruction(new Compute(namecnt++, new Token(TokenType.PLUS, " ", -1), new ConstantData(LLVMType.INT32, 0), new ConstantData(LLVMType.INT32, 0)));
        curFunc.addBasicBlock(basicBlock);
        return basicBlock;
    }


    /**
     * 得到数组首元素基地址
     * %2 = getelementptr inbounds [3 x i32], [3 x i32]* %1, i32 0, i32 0
     *
     * @param base_value 传入的是数组指针，取出数组首元素指针int*
     */
    public Getelemntptr makeGetelemntptr(Value base_value) {
        Getelemntptr getelemntptr = new Getelemntptr(namecnt++, base_value,
                new ConstantData(LLVMType.INT32, 0), new ConstantData(LLVMType.INT32, 0));
        curBBlock.addInstruction(getelemntptr);
        return getelemntptr;
    }

    /**
     * 得到数组a[delta]位置的元素的指针
     *
     * @param base_value 数组起始位置元素地址
     * @param delta      偏移量
     * @return 从base_value往后数delta个元素的位置指针int*
     */
    // %3 = getelementptr inbounds i32, i32* %2, i32 1
    public Getelemntptr makeGetelemntptr(Value base_value, int delta) {
        Getelemntptr getelemntptr = new Getelemntptr(namecnt++, base_value, new ConstantData(LLVMType.INT32, delta));
        curBBlock.addInstruction(getelemntptr);
        return getelemntptr;
    }


    /**
     * 传入数组元素指针，用于获取第delta个元素的位置
     *
     * @param base_value
     * @param delta
     * @param isFromElem
     * @return
     */
    public Getelemntptr makeGetelemntptr(Value base_value, Value delta, boolean isFromElem) {
        Getelemntptr getelemntptr = new Getelemntptr(namecnt++, base_value, delta);
        curBBlock.addInstruction(getelemntptr);
        return getelemntptr;
    }


    /**
     * 传入数组指针，用于获取数组第delta个元素的指针 int*
     *
     * @param base_value 数组指针
     * @param delta      要查找的元素的偏移量
     * @return 数组元素指针
     */
    public Getelemntptr makeGetelemntptr(Value base_value, Value delta) {
        Getelemntptr getelemntptr = new Getelemntptr(namecnt++, base_value,
                new ConstantData(LLVMType.INT32, 0), delta);
        curBBlock.addInstruction(getelemntptr);
        return getelemntptr;
    }


    public void makeStore(Value value, Value pointer) {
        Store store = new Store(value, pointer);
        curBBlock.addInstruction(store);
    }

    public Value makeLoad(Value pointer) {
        Load load = new Load(namecnt++, pointer);
        curBBlock.addInstruction(load);
        return load;
    }

    public void makeRet(Value value) {
        Ret ret;
        if (value == null) {  // 返回void
            ret = new Ret();
        } else {
            ret = new Ret(value);
        }
        curBBlock.addInstruction(ret);
    }

    public void makeJump(BasicBlock targetBlock) {
        Jump jump = new Jump(targetBlock);
        curBBlock.addInstruction(jump);
    }

    public void makeBranch(Value ans, BasicBlock trueBlock, BasicBlock falseBlock) {
        Branch branch = new Branch(ans, trueBlock, falseBlock);
        curBBlock.addInstruction(branch);
    }

    public Call makeCall(Function func, ArrayList<Value> rParams) {
        Call call = new Call(namecnt++, func, rParams);
        curBBlock.addInstruction(call);
        return call;
    }

    public StringLiteral makeStringLiteral(String string) {
        if (stringLiterals.containsKey(string)) {
            return stringLiterals.get(string);
        }
        StringLiteral stringLiteral = new StringLiteral(strcnt++, string);
        stringLiterals.put(string, stringLiteral);
        llvmModule.addStringLiteral(stringLiteral);
        return stringLiteral;
    }

    public void makeCallPutStr(String string) {
        StringLiteral stringLiteral = makeStringLiteral(string);
        Getelemntptr getelemntptr = makeGetelemntptr(stringLiteral);
        makeCall(LlvmModule.putStr, new ArrayList<Value>() {{
            add(getelemntptr);
        }});
    }

    public Compute makeCompute(Token op_token, Value leftOperand, Value rightOperand) {
        Compute compute = new Compute(namecnt++, op_token, leftOperand, rightOperand);
        curBBlock.addInstruction(compute);
        return compute;
    }

    public Icmp makeIcmp(Token op_token, Value leftOperand, Value rightOperand) {
        Icmp icmp = new Icmp(namecnt++, op_token, leftOperand, rightOperand);
        curBBlock.addInstruction(icmp);
        return icmp;
    }

    public Zext makeZext(Value oldValue, LLVMType targetType) {
        Zext zext = new Zext(namecnt++, oldValue, (BaseIntegerType) targetType);
        curBBlock.addInstruction(zext);
        return zext;
    }


}
