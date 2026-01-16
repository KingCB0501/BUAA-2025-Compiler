package llvm;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.Block;
import frontend.Parser.AST.BlockItem;
import frontend.Parser.AST.CompUnit;
import frontend.Parser.AST.ConstDecl;
import frontend.Parser.AST.ConstDef;
import frontend.Parser.AST.ConstInitVal;
import frontend.Parser.AST.Decl;
import frontend.Parser.AST.Exp.AddExp;
import frontend.Parser.AST.Exp.Cond;
import frontend.Parser.AST.Exp.ConstExp;
import frontend.Parser.AST.Exp.EqExp;
import frontend.Parser.AST.Exp.Exp;
import frontend.Parser.AST.Exp.LAndExp;
import frontend.Parser.AST.Exp.LOrExp;
import frontend.Parser.AST.Exp.LVal;
import frontend.Parser.AST.Exp.MulExp;
import frontend.Parser.AST.Exp.PrimaryExp;
import frontend.Parser.AST.Exp.RelExp;
import frontend.Parser.AST.Exp.UnaryExp;
import frontend.Parser.AST.Exp.UnaryOp;
import frontend.Parser.AST.ForStmt;
import frontend.Parser.AST.FuncDef;
import frontend.Parser.AST.FuncFParam;
import frontend.Parser.AST.FuncFParams;
import frontend.Parser.AST.FuncRParams;
import frontend.Parser.AST.FuncType;
import frontend.Parser.AST.InitVal;
import frontend.Parser.AST.MainFuncDef;
import frontend.Parser.AST.Stmt.BlockSubStmt;
import frontend.Parser.AST.Stmt.BreakSubStmt;
import frontend.Parser.AST.Stmt.ContinueSubStmt;
import frontend.Parser.AST.Stmt.ExpSubStmt;
import frontend.Parser.AST.Stmt.ForSubStmt;
import frontend.Parser.AST.Stmt.IfSubStmt;
import frontend.Parser.AST.Stmt.LValSubStmt;
import frontend.Parser.AST.Stmt.PrintfSubStmt;
import frontend.Parser.AST.Stmt.ReturnSubStmt;
import frontend.Parser.AST.Stmt.Stmt;
import frontend.Parser.AST.VarDecl;
import frontend.Parser.AST.VarDef;
import llvm.Const.ConstantArray;
import llvm.Const.ConstantData;
import llvm.UserClass.BasicBlock;
import llvm.UserClass.Function;
import llvm.UserClass.GlobalVar;
import llvm.UserClass.Instruction;
import llvm.instruction.Alloca;
import llvm.instruction.Getelemntptr;
import llvm.instruction.Ret;
import llvm.type.ArrayType;
import llvm.type.BaseIntegerType;
import llvm.type.LLVMType;
import llvm.type.PointerType;

import java.util.ArrayList;
import java.util.Stack;

public class Visitor extends IRBuilder {

    private LVIRSymbolTable curSymTable;
    private boolean cancalValue = false;
    private Stack<Loop> loopStack = new Stack<>();    // 主要用作循环中的continue与break
    private static Visitor visitor;

    private Visitor() {
        this.curSymTable = new LVIRSymbolTable(null);
        curSymTable.addSymbol("getint", LlvmModule.getInt);
        curSymTable.addSymbol("putint", LlvmModule.putInt);
        curSymTable.addSymbol("putch", LlvmModule.putChar);
        curSymTable.addSymbol("putstr", LlvmModule.putStr);
    }

    public static Visitor getInstance() {
        if (visitor == null) {
            visitor = new Visitor();
        }
        return visitor;
    }

    public boolean isGlobal() {
        return this.curSymTable.getParent() == null;
    }

    /**
     * 编译单元 CompUnit → {Decl} {FuncDef} MainFuncDef
     */
    public LlvmModule visitCompunit(CompUnit compUnit) {
        ArrayList<Decl> decls = compUnit.getDecls();
        for (Decl decl : decls) {
            visitDecl(decl);   // 全局变量
        }
        ArrayList<FuncDef> funcDefs = compUnit.getFuncDefs();
        for (FuncDef funcDef : funcDefs) {
            visitFuncDef(funcDef);  // 函数
        }

        MainFuncDef mainFuncDef = compUnit.getMainFuncDef();
        visitMainFuncDef(mainFuncDef);

        return getModule();
    }

    /**
     * 声明 Decl → ConstDecl | VarDecl
     **/
    public void visitDecl(Decl decl) {
        if (decl instanceof ConstDecl) {
            visitConstDecl((ConstDecl) decl);
        } else if (decl instanceof VarDecl) {
            visitVarDecl((VarDecl) decl);
        } else {
            System.err.println("Error: unrecognized decl: " + decl + "in Visitor");
        }
    }

    /**
     * 常量声明 ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
     */
    public void visitConstDecl(ConstDecl decl) {
        ArrayList<ConstDef> constDefs = decl.getConstDefs();
        for (ConstDef constDef : constDefs) {
            visitConstDef(constDef);
        }
    }

    /**
     * ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
     */
    public void visitConstDef(ConstDef constDef) {
        Token ident = constDef.getIdent();
        ConstExp constExp = constDef.getConstExp();
        ConstInitVal constInitVal = constDef.getConstInitVal();
        ArrayList<Integer> inits = visitConstInitval(constInitVal);
        boolean isArray = constExp != null;
        if (isArray) {
            int length = visitConstExp(constExp);       // 数组定义长度
            LLVMType type = new ArrayType(32, length);   // 数组类型
            ConstantArray constantArray = new ConstantArray(type, inits);     // 初值类型, 已经补充足够0
            if (isGlobal()) {    // 全局数组
                GlobalVar globalVar = makeGlobalVar(ident.getValue(), type, constantArray, true);
                // TODO 登记符号表
                curSymTable.addSymbol(ident.getValue(), globalVar);
            } else {
                /**
                 * int c[3] = {1, 2, 3};
                 *
                 * %1 = alloca [3 x i32]
                 * %2 = getelementptr inbounds [3 x i32], [3 x i32]* %1, i32 0, i32 0
                 * store i32 1, i32* %2
                 * %3 = getelementptr inbounds i32, i32* %2, i32 1
                 * store i32 2, i32* %3
                 * %4 = getelementptr inbounds i32, i32* %3, i32 1
                 * store i32 3, i32* %4
                 */
                Alloca alloca = makeAlloca(type, constantArray);
                curSymTable.addSymbol(ident.getValue(), alloca);     // 数组需要加入符号表

                Getelemntptr baseptr = makeGetelemntptr(alloca);
                // TODO数组基地址应该不用存了吧，之后需要用的时候再从符号表中去查找计算
                makeStore(constantArray.getElement(0), baseptr);
                // TODO 目前是逐个初始化， 局部数组变量是否存在直接初始化0，或者只初始化非0元素首字符
                for (int i = 1; i < constantArray.getLength(); i++) {
                    Getelemntptr getelemntptr = makeGetelemntptr(baseptr, i);
                    makeStore(constantArray.getElement(i), getelemntptr);
                }
            }
        } else {
//            int constNum = inits.get(0);
//            ConstantData constantData = new ConstantData(new BaseIntegerType(), constNum);
//            GlobalVar globalVar = makeGlobalVar(ident.getValue(), LLVMType.INT32, constantData, true);
//            curSymTable.addSymbol(ident.getValue(), globalVar);
            int constNum = inits.get(0);
            ConstantData constantData = new ConstantData(new BaseIntegerType(), constNum);
            curSymTable.addSymbol(ident.getValue(), constantData);
        }
    }

    /**
     * ConstExp → AddExp
     *
     * @param constExp
     * @return
     */
    public Integer visitConstExp(ConstExp constExp) {
        cancalValue = true;
        int ans = ((ConstantData) visitAddExp(constExp.getAddExp())).getNum();
        cancalValue = false;
        return ans;
    }

    /**
     * 常量初值 ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}'
     */
    public ArrayList<Integer> visitConstInitval(ConstInitVal constInitVal) {
        ArrayList<Integer> initValues = new ArrayList<>();
        boolean isArray = constInitVal.get_having_brace();  // 是否属数组初值
        if (isArray) {
            ArrayList<ConstExp> constExps = constInitVal.getConstExps();
            for (ConstExp constExp : constExps) {
                initValues.add(visitConstExp(constExp));
            }
        } else {
            ConstExp constExp = constInitVal.getConstExp(0);
            initValues.add(visitConstExp(constExp));
        }
        return initValues;
    }

    /**
     * VarDecl → [ 'static' ] BType VarDef { ',' VarDef } ';'
     *
     * @param decl
     */
    public void visitVarDecl(VarDecl decl) {
        // 静态变量的处理
        // 全局静态变量
        // static int g_val = 10;       @_ZL5g_val = internal global i32 10, align 4
        // 局部变量的静态变量
        //int counter() {
        //    static int cnt = 0;
        //    cnt++;
        //    return cnt;
        //}
        // 处理办法：将该变量当做全局变量在LLVM-IR中声明，但是登记符号表时候依旧登记在当前层符号表
        //@_ZZ7countervE3cnt = internal global i32 0, align 4
        //
        //define i32 @_Z7counterv() {
        //  ; 直接加载这个全局变量
        //  %1 = load i32, i32* @_ZZ7countervE3cnt, align 4
        //  %2 = add nsw i32 %1, 1
        //  store i32 %2, i32* @_ZZ7countervE3cnt, align 4
        //  return %2
        //}
        boolean is_static = decl.getIsStatic();
        ArrayList<VarDef> varDefs = decl.getVarDefs();
        for (VarDef varDef : varDefs) {
            visitVarDef(varDef, is_static);
        }
    }

    /**
     * VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
     *
     * @param varDef
     * @param is_static
     */
    public void visitVarDef(VarDef varDef, boolean is_static) {
        Token ident = varDef.getIdent();
        ConstExp constExp = varDef.getConstExp();
        InitVal initVal = varDef.getInitVal();
        if (constExp == null) {    // 非数组
            if (isGlobal() || is_static) {
                // 所有静态变量当做全局变量去处理，直接添加到当前符号表中，声明语句也不会出现在函数所在的basicblock中，防止重复声明初始化静态变量
                // 全局变量，以及静态局部变量的初始值均为编译器可计算量
                int init_num = 0;
                if (initVal != null) {
                    cancalValue = true;
                    ArrayList<Value> init_datas = visitInitVal(initVal, 1);
                    cancalValue = false;    // TODO  如何确定取出来的一定是constantData
                    init_num = ((ConstantData) init_datas.get(0)).getNum();
                }
                GlobalVar globalVar;
                if (isGlobal()) {
                    globalVar = makeGlobalVar(ident.getValue(), LLVMType.INT32, new ConstantData(LLVMType.INT32, init_num), false);
                } else {
                    globalVar = makeGlobalVar(LLVMType.INT32, new ConstantData(LLVMType.INT32, init_num), false);
                }

                curSymTable.addSymbol(ident.getValue(), globalVar);
            } else {
                // 非静态局部变量，初始话表达式Exp编译期不可知
                // Exp的表达式的值一定是32位的，因为逻辑表达式等不会出现在这里，所以位数不可能为1
                Alloca alloca = makeAlloca(LLVMType.INT32);
                curSymTable.addSymbol(ident.getValue(), alloca);

                // 如果存在初始值
                if (initVal != null) {
                    ArrayList<Value> inits = visitInitVal(initVal, 1);
                    Value init = inits.get(0);
                    // TODO 应该不需要处理位数不匹配问题吧
                    makeStore(init, alloca);
                }
            }
        } else {
            // 数组情况
            int length = visitConstExp(constExp);   // 数组长度
            ArrayType arrayType = new ArrayType(LLVMType.INT32, length);    // 数组类型
            if (isGlobal() || is_static) {  // 静态局部变量数组也当做全局变量储量
                GlobalVar globalVar;
                if (initVal == null) {   // 全初始化为0
                    if (isGlobal()) {   //文法要求静态变量只能是局部变量
                        globalVar = makeGlobalVar(ident.getValue(), arrayType, new ConstantArray(arrayType), false);
                    } else {
                        globalVar = makeGlobalVar(arrayType, new ConstantArray(arrayType), false);
                    }
                    curSymTable.addSymbol(ident.getValue(), globalVar);
                } else {
                    // 存在初始值,全局变量或者静态变量一定可以计算出准确值
                    cancalValue = true;
                    ArrayList<Value> inits = visitInitVal(initVal, length);
                    cancalValue = false;
                    ArrayList<Integer> init_integers = new ArrayList<>();
                    for (Value val : inits) {
                        init_integers.add(((ConstantData) val).getNum());
                    }
                    ConstantArray constantArray = new ConstantArray(arrayType, init_integers);
                    if (isGlobal()) {
                        globalVar = makeGlobalVar(ident.getValue(), arrayType, constantArray, false);
                    } else {
                        globalVar = makeGlobalVar(arrayType, new ConstantArray(arrayType), false);
                    }
                    curSymTable.addSymbol(ident.getValue(), globalVar);
                }
            } else {   // 局部非静态数组
                Alloca alloca = makeAlloca(arrayType);
                curSymTable.addSymbol(ident.getValue(), alloca);
                if (initVal != null) {
                    ArrayList<Value> inits = visitInitVal(initVal, length);
                    if (inits.isEmpty()) {
                        return;
                    }
                    Getelemntptr base_ptr = makeGetelemntptr(alloca);     // 得到数组元素首地址int*
                    makeStore(inits.get(0), base_ptr);
                    for (int i = 1; i < inits.size(); i++) {
                        Getelemntptr temp_ptr = makeGetelemntptr(base_ptr, i);
                        makeStore(inits.get(i), temp_ptr);
                    }
                }
            }

        }
    }

    /**
     * InitVal → Exp | '{' [ Exp { ',' Exp } ] '}'
     *
     * @param length 所最终求得元素个数，数组初始化不足补0
     */
    public ArrayList<Value> visitInitVal(InitVal initVal, int length) {
        // TODO length看起来用处不大
        ArrayList<Exp> exps = initVal.getExps();
        ArrayList<Value> inits = new ArrayList<>();
        for (Exp exp : exps) {
            inits.add(visitExp(exp));
        }
        return inits;
    }

    /**
     * FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
     *
     * @param funcDef
     */
    public void visitFuncDef(FuncDef funcDef) {
        FuncType funcType = funcDef.getFuncType();    //TODO 后期需要修改，两个funcType重名了
        Token ident = funcDef.getIdent_token();
        FuncFParams funcFParams = funcDef.getFuncFparams();
        Block block = funcDef.getBlock();

        BaseIntegerType retType = visitFuncType(funcType);   // 返回值类型
        ArrayList<BaseIntegerType> fParamTypes = getFParamTypes(funcFParams);   // 形参类型列表

        curFunc = makeFunc(ident.getValue(), retType, fParamTypes);
        curSymTable.addSymbol(ident.getValue(), curFunc);

        enter();   // 下入下一个作用域
        BasicBlock enterBlock = makeBasicBlock();    // 所有形参均需要经过alloc,store后才可使用
        curBBlock = enterBlock;   // 新的基础块
        if (funcFParams != null) {
            visitFuncFParams(funcFParams);   // 完成形参的alloca-store
        }

        visitBlock(block);

        // 注意注意，LLVM-IR语法要求所有函数均存在Ret语句；对于原函数为void的函数，我们应该检查是否存在Ret
        addRetInstr2Func();
        leave();

    }

    /**
     * 返回形参类型列表
     *
     * @param funcFParams
     * @return
     */
    public ArrayList<BaseIntegerType> getFParamTypes(FuncFParams funcFParams) {
        ArrayList<BaseIntegerType> fParamTypes = new ArrayList<>();
        if (funcFParams != null) {
            for (FuncFParam funcFParam : funcFParams.getParams()) {
                if (funcFParam.has_brack()) {
                    fParamTypes.add(new PointerType(LLVMType.INT32));     //形参为数组，实际上是指针类型
                } else {
                    fParamTypes.add(LLVMType.INT32);
                }
            }
        }
        return fParamTypes;
    }

    /**
     * FuncIRType → 'void' | 'int'
     */
    public BaseIntegerType visitFuncType(FuncType funcType) {
        Token token = funcType.getToken();
        if (token.isType(TokenType.INTTK)) {
            return LLVMType.INT32;
        } else {
            return LLVMType.VOID;
        }
    }

    /**
     * FuncFParams → FuncFParam { ',' FuncFParam }
     */
    public void visitFuncFParams(FuncFParams funcFParams) {
        ArrayList<FuncFParam> params = funcFParams.getParams();
        for (int i = 0; i < params.size(); i++) {
            visitFuncFParam(i, params.get(i));
        }
    }

    /**
     * FuncFParam → BType Ident ['[' ']']
     */
    public void visitFuncFParam(int index, FuncFParam funcFParam) {
        // 第i个形参
        // 先alloc, 再store， 登记符号表

        Alloca alloca = makeAlloca(curFunc.getFParam(index).getType());   // 从curBlock与index获取形参类型
        makeStore(curFunc.getFParam(index), alloca);

        curSymTable.addSymbol(funcFParam.getIdent_token().getValue(), alloca);

    }

    /**
     * MainFuncDef → 'int' 'main' '(' ')' Block
     *
     * @param mainFuncDef
     */
    public void visitMainFuncDef(MainFuncDef mainFuncDef) {
        Block block = mainFuncDef.getBlock();

        curFunc = makeFunc("main", LLVMType.INT32, new ArrayList<>());
        curSymTable.addSymbol("main", curFunc);

        enter();
        BasicBlock enterBlock = makeBasicBlock();
        curBBlock = enterBlock;
        visitBlock(mainFuncDef.getBlock());
        leave();
    }

    /**
     * Block → '{' { BlockItem } '}'
     */
    public void visitBlock(Block block) {
        ArrayList<BlockItem> blockItems = block.getBlockItems();
        for (BlockItem blockItem : blockItems) {
            visitBlockItem(blockItem);
        }
    }

    /**
     * BlockItem → Decl | Stmt
     */
    public void visitBlockItem(BlockItem blockItem) {
        if (blockItem instanceof Decl) {
            visitDecl((Decl) blockItem);
        } else if (blockItem instanceof Stmt) {
            visitStmt((Stmt) blockItem);
        } else {
            System.err.println("Unknown block item type: " + blockItem.getClass().getName());
        }
    }

    /**
     * 语句 Stmt → LVal '=' Exp ';'
     * | [Exp] ';'
     * | Block
     * | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
     * | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
     * | 'break' ';' | 'continue' ';'
     * | 'return' [Exp] ';'
     * | 'printf''('StringConst {','Exp}')'';'
     */
    public void visitStmt(Stmt stmt) {
        if (stmt instanceof LValSubStmt) {
            visitLValSubStmt((LValSubStmt) stmt);
        } else if (stmt instanceof ExpSubStmt) {
            visitExpSubStmt((ExpSubStmt) stmt);
        } else if (stmt instanceof BlockSubStmt) {
            visitBlockSubStmt((BlockSubStmt) stmt);
        } else if (stmt instanceof IfSubStmt) {
            visitIfSubStmt((IfSubStmt) stmt);
        } else if (stmt instanceof ForSubStmt) {
            visitForSubStmt((ForSubStmt) stmt);
        } else if (stmt instanceof BreakSubStmt) {
            visitBreakSubStmt((BreakSubStmt) stmt);
        } else if (stmt instanceof ContinueSubStmt) {
            visitContinueSubStmt((ContinueSubStmt) stmt);
        } else if (stmt instanceof ReturnSubStmt) {
            visitReturnSubStmt((ReturnSubStmt) stmt);
        } else if (stmt instanceof PrintfSubStmt) {
            visitPrintfSubStmt((PrintfSubStmt) stmt);
        } else {
            System.err.println("Unknown stmt type: " + stmt.getClass().getName());
        }
    }

    /**
     * Stmt → LVal '=' Exp ';'
     */
    public void visitLValSubStmt(LValSubStmt lvalSubStmt) {
        LVal lVal = lvalSubStmt.getLVal();
        Exp exp = lvalSubStmt.getExp();

        // 此时cnacalValue一定为false
        Value vLVal = getLValPointer(lVal);   // 得到vLVal指针的位置
        Value vExp = visitExp(exp);      // Exp返回的是存储Exp值的LLVM-IR变量的名称

        makeStore(vExp, vLVal);

    }

    /**
     * Stmt → [Exp] ';'
     */
    public void visitExpSubStmt(ExpSubStmt expSubStmt) {
        Exp exp = expSubStmt.getExp();
        if (exp != null) {
            visitExp(exp);
        }
    }

    /**
     * Stmt → Block
     */
    public void visitBlockSubStmt(BlockSubStmt blockSubStmt) {
        Block block = blockSubStmt.getBlock();
        enter();
        visitBlock(block);
        leave();
    }

    /**
     * Stmt → 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
     */
    public void visitIfSubStmt(IfSubStmt ifSubStmt) {
        Cond cond = ifSubStmt.getCond();
        Stmt if_stmt = ifSubStmt.getStmt_if();
        Stmt else_stmt = ifSubStmt.getStmt_else();

        if (else_stmt != null) {
            BasicBlock successBlock = makeBasicBlock();
            BasicBlock faliureBlock = makeBasicBlock();
            BasicBlock endlBock = makeBasicBlock();

            visitCond(cond, successBlock, faliureBlock);   // 该函数实现逻辑判断，最后一条语句如果成功则进入success, 如果失败则进入failure

            curBBlock = successBlock;
            curFunc.moveBasicBlock2End(curBBlock);
            visitStmt(if_stmt);
            makeJump(endlBock);

            curBBlock = faliureBlock;
            curFunc.moveBasicBlock2End(curBBlock);
            visitStmt(else_stmt);
            makeJump(endlBock);

            curBBlock = endlBock;
            curFunc.moveBasicBlock2End(curBBlock);

        } else {
            BasicBlock successBlock = makeBasicBlock();
            BasicBlock endBlock = makeBasicBlock();

            visitCond(cond, successBlock, endBlock);

            curBBlock = successBlock;
            curFunc.moveBasicBlock2End(curBBlock);
            visitStmt(if_stmt);
            makeJump(endBlock);

            curBBlock = endBlock;
            curFunc.moveBasicBlock2End(curBBlock);
        }

    }

    /**
     * Stmt → 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
     */
    public void visitForSubStmt(ForSubStmt forSubStmt) {
        ForStmt forStmt_1 = forSubStmt.getForStmt_1();
        Cond cond = forSubStmt.getCond();
        ForStmt forStmt_2 = forSubStmt.getForStmt_2();
        Stmt stmt = forSubStmt.getStmt();

//        BasicBlock initBlock = makeBasicBlock();
        BasicBlock condBlock = makeBasicBlock();
        BasicBlock loopBodyBlock = makeBasicBlock();    // 其实loop_body和follow可以合并成功一个块；后记，不可以，continue可能会直接跳转到follow
        BasicBlock followBlock = makeBasicBlock();
        BasicBlock endBlock = makeBasicBlock();
//        Loop loop = new Loop(initBlock, condBlock, followBlock, loopBodyBlock, endBlock);
        Loop loop = new Loop(curBBlock, condBlock, followBlock, loopBodyBlock, endBlock);
        loopStack.push(loop);
//
//        curBBlock = initBlock;
        if (forStmt_1 != null) {
            visitForStmt(forStmt_1);
        }
        makeJump(condBlock);


        curBBlock = condBlock;
        if (cond != null) {
            visitCond(cond, loopBodyBlock, endBlock);
        } else {
            makeJump(loopBodyBlock);
        }
        curFunc.moveBasicBlock2End(condBlock);


        curBBlock = loopBodyBlock;
        visitStmt(stmt);
        makeJump(followBlock);
        curFunc.moveBasicBlock2End(loopBodyBlock);


        curBBlock = followBlock;
        if (forStmt_2 != null) {
            visitForStmt(forStmt_2);
        }
        makeJump(condBlock);
        curFunc.moveBasicBlock2End(followBlock);
        // 无条件跳转到cond


        loopStack.pop();
        curBBlock = endBlock;
        curFunc.moveBasicBlock2End(endBlock);
    }

    /**
     * Stmt → 'break' ';'
     */
    public void visitBreakSubStmt(BreakSubStmt breakSubStmt) {
        // 无条件跳转到当前loopStack栈顶的loop的endBlock
        Loop loop = loopStack.peek();
        if (loop == null) {
            System.err.println("Loop stack is empty");
            return;
        }
        makeJump(loop.getEndBlock());
//        makeBranch(new ConstantData(LLVMType.INT32, 1), loop.getEndBlock(), loop.getFollowBlock());
        curBBlock = new BasicBlock("breakBasicblock");  //TODO 使该种bb的名称进行修改

    }

    /**
     * Stmt → 'continue' ';'
     */
    // TODO break 与 continue之后的语句算是一个新的基本块吗
    public void visitContinueSubStmt(ContinueSubStmt continueSubStmt) {
        // 无条件跳转到当前loopStack栈顶的loop的followBlock
        Loop loop = loopStack.peek();
        if (loop == null) {
            System.err.println("Loop stack is empty");
            return;
        }
        makeJump(loop.getFollowBlock());
//        makeBranch(new ConstantData(LLVMType.INT32, 1), loop.getEndBlock(), loop.getFollowBlock());
        curBBlock = new BasicBlock("continueBasicblock");
    }

    /**
     * Stmt → 'return' [Exp] ';'
     */
    public void visitReturnSubStmt(ReturnSubStmt returnSubStmt) {
        Exp exp = returnSubStmt.getExp();
        if (exp == null) {
            makeRet(null);
            return;
        }
        Value retValue = visitExp(exp);
        makeRet(retValue);
        curBBlock = new BasicBlock("returnBasicblock");
    }

    /**
     * Stmt → 'printf''('StringConst {','Exp}')'';'
     */
    public void visitPrintfSubStmt(PrintfSubStmt printfSubStmt) {
        ArrayList<Exp> exps = printfSubStmt.getExps();
        ArrayList<Value> vExps = new ArrayList<>();   // 计算出所有exp
        for (Exp exp : exps) {
            Value value = visitExp(exp);
            vExps.add(value);
        }

        int index = 0;
        ArrayList<String> strings = printfSubStmt.spiltString4d();  // 将stringConst按照%d分割开来
        for (String string : strings) {
            if (string.equals("%d")) {
                Value vRparm = vExps.get(index);
                index++;
                ArrayList<Value> temp = new ArrayList<>();
                temp.add(vRparm);
                makeCall(LlvmModule.putInt, temp);   // 调用putint函数输出
            } else {
                makeCallPutStr(string);
            }
        }

    }

    /**
     * ForStmt → LVal '=' Exp { ',' LVal '=' Exp }
     *
     * @param forStmt
     */
    public void visitForStmt(ForStmt forStmt) {
        if (forStmt == null) {
            return;
        }
        ArrayList<LVal> lvals = forStmt.getLvals();
        ArrayList<Exp> exps = forStmt.getExps();
        for (int i = 0; i < lvals.size(); i++) {
            LVal lval = lvals.get(i);
            Exp exp = exps.get(i);

            Value vLval = getLValPointer(lval);
            Value vExp = visitExp(exp);

            makeStore(vExp, vLval);
        }
    }

    /**
     * Exp → AddExp
     *
     * @param exp
     * @return 存储Exp的值的变量名称(Value名称 - - % v)
     */
    public Value visitExp(Exp exp) {
        return visitAddExp(exp.getAddExp());
    }


    /**
     * AddExp → MulExp | AddExp ('+' | '−') MulExp
     * AddExp → MulExp {('+' | '−') MulExp}
     * 如果canCalValue为true的话，则代表在编译期访问AddExp，得到编译期常量ConstantData
     */
    public Value visitAddExp(AddExp addExp) {
        ArrayList<MulExp> mulExps = addExp.getMulExps();
        ArrayList<Token> op_tokens = addExp.getOp_tokens();
        if (cancalValue) {   // 编译期计算
            int ans = 0;
            ans = ((ConstantData) visitMulExp(mulExps.get(0))).getNum();
            for (int i = 1; i < mulExps.size(); i++) {
                int temp = ((ConstantData) visitMulExp(mulExps.get(i))).getNum();
                if (op_tokens.get(i - 1).isType(TokenType.PLUS)) {
                    ans = ans + temp;
                } else {
                    ans = ans - temp;
                }
            }
            return new ConstantData(LLVMType.INT32, ans);
        } else {   // 返回储存AddExp运算结果的变量名称
            Value ans = visitMulExp(mulExps.get(0));
            for (int i = 1; i < mulExps.size(); i++) {
                Value temp = visitMulExp(mulExps.get(i));
                Token op_token = op_tokens.get(i - 1);
                ans = makeCompute(op_token, ans, temp);
            }
            return ans;
        }
    }

    /**
     * MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
     * MulExp → UnaryExp { ('*' | '/' | '%') UnaryExp}
     * 如果canCalValue为true的话，则代表在编译期访问MulExp，得到编译期常量ConstantData
     */
    public Value visitMulExp(MulExp mulExp) {
        ArrayList<UnaryExp> unaryExps = mulExp.getUnaryExps();
        ArrayList<Token> op_tokens = mulExp.getOpTokens();
        if (cancalValue) {   // 编译期常量，返回constantData
            int ans = 0;
            ans = ((ConstantData) visitUnaryExp(unaryExps.get(0))).getNum();
            for (int i = 1; i < unaryExps.size(); i++) {
                int temp = ((ConstantData) visitUnaryExp(unaryExps.get(i))).getNum();
                Token op_token = op_tokens.get(i - 1);
                if (op_token.isType(TokenType.MULT)) {
                    ans = ans * temp;
                } else if (op_token.isType(TokenType.DIV)) {
                    ans = ans / temp;
                } else if (op_token.isType(TokenType.MOD)) {
                    ans = ans % temp;
                } else {
                    System.err.println("Unknown op token: " + op_token);
                }
            }
            return new ConstantData(LLVMType.INT32, ans);
        } else { // 返回存储中间结果的变量名称
            Value ans = visitUnaryExp(unaryExps.get(0));
            for (int i = 1; i < unaryExps.size(); i++) {
                Value temp = visitUnaryExp(unaryExps.get(i));
                Token op_token = op_tokens.get(i - 1);
                ans = makeCompute(op_token, ans, temp);
            }
            return ans;
        }
    }

    /**
     * LVal → Ident ['[' Exp ']']
     *
     * @return LVal* 表达式定位对象的指针，即在符号表中存储的变量指针, 一般用作 Lval = Exp时候使用
     */
    public Value getLValPointer(LVal lval) {
        Token ident = lval.getIdent();
        Exp exp = lval.getExp();

        Value vLVal = curSymTable.find(ident.getValue());

        LLVMType ident_type = ((PointerType) vLVal.getType()).getTargetType();
        if (ident_type.isInteger()) {   // 整型数据
            return vLVal;
        } else if (ident_type.isArray()) {    // ident是数组类型
            // 因为现在所属情况是对于ident[Exp] 进行赋值，但是数组不能整体赋值，所以一定存在Exp,访问数组某个元素
            Value vExp = visitExp(exp);
            Getelemntptr getelemntptr = makeGetelemntptr(vLVal, vExp);
            return getelemntptr;
        } else if (ident_type.isPointer()) {
            // 如果是指针，则一般是将数组作为参数传递到某个函数内部调用时候为二重指针
            // 此时需要先load出真正的ident的指针
            Value array = makeLoad(vLVal);
            Value vExp = visitExp(exp);
            Getelemntptr getelemntptr = makeGetelemntptr(array, vExp, true);
            return getelemntptr;
        } else {
            System.err.println("Unhandled lval type: " + ident_type + "in getLValPointer");
            return null;
        }
    }


    /**
     * 得到存储左值值的变量名称
     * LVal → Ident ['[' Exp ']']
     *
     * @param lval
     * @return
     */
    public Value getLValValue(LVal lval) {
//        System.err.println(lval);
        // 符号表中查到的都是ident类型的指针类型
        Token ident = lval.getIdent();
        Exp exp = lval.getExp();
        Value pointer = curSymTable.find(ident.getValue());

        if (pointer.getType().isInteger()) {
            return pointer;
        }

        LLVMType target_type = ((PointerType) pointer.getType()).getTargetType();
        if (target_type.isInteger() && ((BaseIntegerType) target_type).isInt32()) {    // int a;
            if (cancalValue) {
                if (pointer instanceof GlobalVar) {
                    return new ConstantData(LLVMType.INT32, ((ConstantData) ((GlobalVar) pointer).getInit()).getNum());
                } else {
                    return new ConstantData(LLVMType.INT32, ((ConstantData) ((Alloca) pointer).getInit()).getNum());
                }
            }
            Value temp = makeLoad(pointer);
            return temp;
        } else if (target_type.isArray()) {
            // int a[10];    // int [10]
            if (exp != null) {
                if (cancalValue) {
                    Value vExp = visitExp(exp);
                    int index = ((ConstantData) vExp).getNum();
                    if (pointer instanceof GlobalVar) {
                        return ((ConstantArray) ((GlobalVar) pointer).getInit()).getElement(index);
                    } else {
                        return ((ConstantArray) ((Alloca) pointer).getInit()).getElement(index);
                    }
                }

                Value vExp = visitExp(exp);
                Getelemntptr getelemntptr = makeGetelemntptr(pointer, vExp);
                return makeLoad(getelemntptr);
            } else {
                // 数组名用作传参，此时表达式值等价于第一个元素的地址，必须是int*类型
                Getelemntptr getelemntptr = makeGetelemntptr(pointer);
                return getelemntptr;
            }
        } else if (target_type.isPointer()) {
            // int a[ 10];
            // func(a)
            // void func(int[] b){
            //  b[10];
            //  }
            // alloca
            // store int *
            //
            // 在函数中使用传进来的参数时候，再找到的是二重指针
            Value array_elem_pointer = makeLoad(pointer);   // 取出的是数组首元素地址
            if (exp == null) {
                return array_elem_pointer;
            } else {
                Value vExp = visitExp(exp);
                Getelemntptr getelemntptr = makeGetelemntptr(array_elem_pointer, vExp, true);
                return makeLoad(getelemntptr);
            }
        } else {
            System.err.println("Unhandled lval type: " + target_type + "in getLValValue");
            return null;
        }

    }

    /**
     * PrimaryExp → '(' Exp ')' | LVal | Number
     */
    public Value visitPrimaryExp(PrimaryExp primaryExp) {
        if (primaryExp.isFromExp()) {
            return visitExp(primaryExp.getExp());
        } else if (primaryExp.isFromLVal()) {
            return getLValValue(primaryExp.getLVal());
        } else {
            return new ConstantData(LLVMType.INT32, primaryExp.getNumber().getValue());
        }

    }

    /**
     * FuncRParams → Exp { ',' Exp }
     */
    public ArrayList<Value> visitFuncRParams(FuncRParams funcRParams) {
        if (funcRParams == null) {
            return null;
        }
        ArrayList<Exp> exps = funcRParams.getExps();
        ArrayList<Value> rParams = new ArrayList<>();
        for (Exp exp : exps) {
            rParams.add(visitExp(exp));
        }
        return rParams;
    }

    /**
     * UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
     *
     * @param unaryExp
     * @return
     */
    public Value visitUnaryExp(UnaryExp unaryExp) {
        if (unaryExp.isFromPrimaryExp()) {
            return visitPrimaryExp(unaryExp.getPrimaryExp());
        } else if (unaryExp.isFromFuncCall()) {   // Ident '(' [FuncRParams] ')'
            FuncRParams funcRParams = unaryExp.getFuncRParams();
            ArrayList<Value> rPrams = visitFuncRParams(funcRParams);
            Function func = (Function) curSymTable.find(unaryExp.getIdent_token().getValue());
            return makeCall(func, rPrams);

        } else if (unaryExp.isFromUnaryExp()) { //UnaryOp UnaryExp
            UnaryOp unaryOp = unaryExp.getUnaryOp();
            Token op_token = unaryOp.getOpToken();
            UnaryExp subUnaryExp = unaryExp.getUnaryExp();
            if (cancalValue) {
                int ans = ((ConstantData) visitUnaryExp(subUnaryExp)).getNum();
                if (op_token.isType(TokenType.MINU)) {
                    ans = 0 - ans;
                } else if (op_token.isType(TokenType.NOT)) {
                    ans = (ans == 0) ? 1 : 0;
                }
                return new ConstantData(LLVMType.INT32, ans);
            } else {
                Value temp = visitUnaryExp(subUnaryExp);
                Value ans;
                if (op_token.isType(TokenType.MINU)) {
                    ans = makeCompute(op_token, new ConstantData(LLVMType.INT32, 0), temp);
                } else if (op_token.isType(TokenType.NOT)) {
                    Value ans_i1 = makeIcmp(op_token, temp, new ConstantData(LLVMType.INT32, 0));
                    ans = makeZext(ans_i1, LLVMType.INT32);     // TODO   这是合理或者说是必要的吗
                } else {
                    ans = temp;
                }
                return ans;
            }
        } else {
            System.err.println("Unhandled unary op: " + unaryExp);
            return null;
        }
    }


    /**
     * Cond → LOrExp
     */
    public void visitCond(Cond cond, BasicBlock successBlock, BasicBlock faliureBlock) {
        if (cond == null) {
            return;
        }
        LOrExp lOrExp = cond.getLOrExp();
        visitLOrExp(lOrExp, successBlock, faliureBlock);
    }

    /**
     * LOrExp → LAndExp | LOrExp '||' LAndExp
     */
    public void visitLOrExp(LOrExp lorExp, BasicBlock successBlock, BasicBlock faliureBlock) {
        ArrayList<LAndExp> lAndExps = lorExp.getLAndExps();
        for (int i = 0; i < lAndExps.size(); i++) {
            if (i != lAndExps.size() - 1) {
                BasicBlock nextBlock = makeBasicBlock();
                visitLAndExp(lAndExps.get(i), successBlock, nextBlock);    // 最后一条一定是跳转语句，所以之后进入下一条基本块
                curBBlock = nextBlock;
                curFunc.moveBasicBlock2End(curBBlock);
            } else {
                visitLAndExp(lAndExps.get(i), successBlock, faliureBlock);
            }
        }
    }

    /**
     * LAndExp → EqExp | LAndExp '&&' EqExp
     */
    public void visitLAndExp(LAndExp lAndExp, BasicBlock successBlock, BasicBlock failureBlock) {
        ArrayList<EqExp> eqExps = lAndExp.getEqExps();
        for (int i = 0; i < eqExps.size(); i++) {
            Value eq_ans = visitEqExp(eqExps.get(i));
            if (i != eqExps.size() - 1) {
                BasicBlock nextBlock = makeBasicBlock();
                // 分支跳转指令， 如果成功，进入下一个block接受考验；否则，进入失败块

                makeBranch(eq_ans, nextBlock, failureBlock);
                curBBlock = nextBlock;
                curFunc.moveBasicBlock2End(curBBlock);

            } else {
                makeBranch(eq_ans, successBlock, failureBlock);
            }
        }
    }

    /**
     * RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
     * RelExp → AddExp {('<' | '>' | '<=' | '>=') AddExp}
     * 因为后期在EqExp中可能存在两个RelExp(AddExp)比较是否相等，所以如果单独的由
     * 一个AddExp构成的RelExp，我们返回的依旧是i32类型的visitAddExp()
     * 返回类型如果未经过比较，则是i32类型，否则是i1类型
     */
    public Value visitRelExp(RelExp relExp) {
        ArrayList<AddExp> addExps = relExp.getAddExps();
        ArrayList<Token> op_tokens = relExp.getOpTokens();
        Value ans = visitAddExp(addExps.get(0));    // 此时一定是i32类型的
        for (int i = 1; i < addExps.size(); i++) {
            AddExp nextAddExp = addExps.get(i);
            Value nextOperand = visitAddExp(nextAddExp);
            Token op_token = op_tokens.get(i - 1);
            if (ans.isINT1()) {   // 要与AddExp比较大小，所以统一转换成i32类型
                ans = makeZext(ans, LLVMType.INT32);
            }
            ans = makeIcmp(op_token, ans, nextOperand);
        }
        return ans;
    }


    /**
     * EqExp → RelExp | EqExp ('==' | '!=') RelExp
     * EqExp → RelExp {('==' | '!=') RelExp}
     * 注意每个子节点中的RelExp的返回类型不一定是i1的
     * 返回的一定是一条Icmp指令，i1类型
     */
    public Value visitEqExp(EqExp eqExp) {
        ArrayList<RelExp> relExps = eqExp.getRelExps();
        ArrayList<Token> op_tokens = eqExp.getOpTokens();
        Value ans = visitRelExp(relExps.get(0));
        if (relExps.size() == 1) {
            if (ans.isINT1()) {
                ans = makeZext(ans, LLVMType.INT32);
            }
            ans = makeIcmp(new Token(TokenType.NEQ, " ", -1), ans, new ConstantData(LLVMType.INT32, 0));
        } else {
            for (int i = 1; i < relExps.size(); i++) {
                RelExp nextRelExp = relExps.get(i);
                Value nextOperand = visitRelExp(nextRelExp);
                Token op_token = op_tokens.get(i - 1);
                if (ans.isINT1()) {
                    ans = makeZext(ans, LLVMType.INT32);
                }
                if (nextOperand.isINT1()) {
                    nextOperand = makeZext(nextOperand, LLVMType.INT32);
                }
                ans = makeIcmp(op_token, ans, nextOperand);
            }
        }
        return ans;
    }

    public void addRetInstr2Func() {
        Instruction instr = curBBlock.getLastInstr();
        if (!(instr instanceof Ret)) {    //TODO只有这一种情况吗
            makeRet(null);
        }
//        curBBlock.addInstruction(instr);

    }

    public void enter() {
        LVIRSymbolTable symTable = new LVIRSymbolTable(curSymTable);
        curSymTable.addChild(symTable);
        curSymTable = symTable;
    }

    public void leave() {
        curSymTable = curSymTable.getParent();
    }

}
