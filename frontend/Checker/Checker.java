package frontend.Checker;

import Utils.Error;
import Utils.ErrorLog;
import Utils.ErrorType;
import frontend.Checker.Symbol.ConstSymbol;
import frontend.Checker.Symbol.FuncSymbol;
import frontend.Checker.Symbol.Symbol;
import frontend.Checker.Symbol.SymbolType;
import frontend.Checker.Symbol.VarSymbol;
import frontend.Lexer.Token;
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
import frontend.Parser.AST.Number;
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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Collections.min;

public class Checker {
    private int id_cnt;   // 统计到目前一共出现了多少域
    private SymbolTable rootTable;
    private SymbolTable curTable;
    // isVoidFunc=true, 需要在解析到return函数时标记错误
    // isVoidFunc=false, 需要在checkFuncDef()中判断bolck中最后一个stmt是否为returnSubStmt
    private boolean isVoidFunc;   // 标记当前在check的函数是否是void
    private int loopnum;    // 主要记录当前check处处于存换内第几层，用于break与continue子句的判断

    public Checker() {
        id_cnt = 1;
        this.rootTable = new SymbolTable(1, null);
        this.curTable = rootTable;
        this.isVoidFunc = false;
        this.loopnum = 0;
    }


    public SymbolTable getRootTable(CompUnit compUnit) {
        FuncSymbol funcSymbol = new FuncSymbol(SymbolType.IntFunc, "getint", new ArrayList<>());
        rootTable.addSymbol(funcSymbol);
        checkCompUnit(compUnit);
        rootTable.removeSymbol(funcSymbol);
        return rootTable;
    }

    public void addError(ErrorType errorType, int lineNumber) {
        Error error = new Error(errorType, lineNumber);
        ErrorLog errorLog = ErrorLog.getInstance();
        errorLog.addError(error);
    }

    public boolean isGlobal() {
        return this.curTable.getId() == 1;   // 判断当前符号表是不是最外层符号表
    }

    /**
     * 进入一个新的域, block/或者函数体
     */
    public void enter() {
        id_cnt = id_cnt + 1;
        SymbolTable symbolTable = new SymbolTable(id_cnt, curTable);    //创建一个新的表，子节点保存父节点
        curTable.addChildrenTable(symbolTable);     // 父节点中保存子节点
        this.curTable = symbolTable;
    }

    /**
     * 离开当前域，block或者函数体结束
     */
    public void leave() {
        this.curTable = curTable.getParentTable();
    }


    /**
     * CompUnit → {Decl} {FuncDef} MainFuncDef
     */
    public void checkCompUnit(CompUnit compUnit) {
        ArrayList<Decl> decls = compUnit.getDecls();
        ArrayList<FuncDef> funcDefs = compUnit.getFuncDefs();
        MainFuncDef mainFuncDef = compUnit.getMainFuncDef();
        for (Decl decl : decls) {
            checkDecl(decl);
        }
        for (FuncDef funcDef : funcDefs) {
            checkFuncDef(funcDef);
        }
        checkMainFuncDef(mainFuncDef);
    }

    /**
     * Decl → ConstDecl | VarDecl
     */
    public void checkDecl(Decl decl) {
        if (decl instanceof ConstDecl) {
            checkConstDecl((ConstDecl) decl);
        } else if (decl instanceof VarDecl) {
            checkVarDecl((VarDecl) decl);
        } else {
            System.err.println("Error: invalid decl: " + decl);
        }
    }

    /**
     * ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
     */
    public void checkConstDecl(ConstDecl constDecl) {
        ArrayList<ConstDef> constDefs = constDecl.getConstDefs();
        for (ConstDef constDef : constDefs) {
            checkConstDef(constDef);
        }
    }

    /**
     * ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal // b
     */
    public void checkConstDef(ConstDef constDef) {
        Token ident = constDef.getIdent();
        ConstExp constExp = constDef.getConstExp();
        ConstInitVal constInitVal = constDef.getConstInitVal();

        if (curTable.hasSymbol(ident.getValue())) {   // 同一作用域重定义, b
            addError(ErrorType.b, ident.getLineNumber());
        }
        // constIntArray
        if (constExp != null) {
            curTable.addSymbol(new ConstSymbol(SymbolType.ConstIntArray, ident.getValue(), isGlobal()));
            checkConstExp(constExp);
        } else {
            curTable.addSymbol(new ConstSymbol(SymbolType.ConstInt, ident.getValue(), isGlobal()));
        }
        checkConstInival(constInitVal);
    }

    /**
     * ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}'
     */
    public void checkConstInival(ConstInitVal constInitVal) {
        ArrayList<ConstExp> constExps = constInitVal.getConstExps();
        boolean having_brace = constInitVal.get_having_brace();
        for (ConstExp constExp : constExps) {
            checkConstExp(constExp);
        }

    }

    /**
     * VarDecl → [ 'static' ] BType VarDef { ',' VarDef } ';'
     */
    public void checkVarDecl(VarDecl varDecl) {
        ArrayList<VarDef> varDefs = varDecl.getVarDefs();
        boolean isStatic = varDecl.getIsStatic();
        for (VarDef varDef : varDefs) {
            checkVarDef(varDef, isStatic);
        }
    }

    /**
     * VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal // b
     */
    public void checkVarDef(VarDef varDef, boolean isStatic) {
        Token ident = varDef.getIdent();
        ConstExp constExp = varDef.getConstExp();
        InitVal initVal = varDef.getInitVal();
        if (curTable.hasSymbol(ident.getValue())) {   // 查重定义
            addError(ErrorType.b, ident.getLineNumber());
        }
        if (constExp != null) {  // 数组
            if (isStatic) {   // 是否静态
                curTable.addSymbol(new VarSymbol(SymbolType.StaticIntArray, ident.getValue(), isGlobal()));
            } else {
                curTable.addSymbol(new VarSymbol(SymbolType.IntArray, ident.getValue(), isGlobal()));
            }
        } else {
            if (isStatic) {
                curTable.addSymbol(new VarSymbol(SymbolType.StaticInt, ident.getValue(), isGlobal()));
            } else {
                curTable.addSymbol(new VarSymbol(SymbolType.Int, ident.getValue(), isGlobal()));
            }
        }

        if (constExp != null) {
            checkConstExp(constExp);
        }
        if (initVal != null) {
            checkInitVal(initVal);
        }

    }

    /**
     * InitVal → Exp | '{' [ Exp { ',' Exp } ] '}'
     */
    public void checkInitVal(InitVal initVal) {
        ArrayList<Exp> exps = initVal.getExps();
        for (Exp exp : exps) {
            checkExp(exp);
        }
    }


    /**
     * FuncDef → FuncIRType Ident '(' [FuncFParams] ')' Block // b g
     */
    public void checkFuncDef(FuncDef funcDef) {
        FuncType funcType = funcDef.getFuncType();
        Token ident = funcDef.getIdent_token();
        FuncFParams funcFParams = funcDef.getFuncFparams();
        Block block = funcDef.getBlock();

        if (curTable.hasSymbol(ident.getValue())) {
            addError(ErrorType.b, ident.getLineNumber());
        }
        if (funcType.isInt()) {  // 如果int函数，需要判断block最后一条语句是否为returnSubStmt
            // TODO 需要加强判断 return [exp]; 的exp是否为空吗
            ArrayList<BlockItem> blockItems = block.getBlockItems();
            if (blockItems.isEmpty() || !(blockItems.get((blockItems.size() - 1)) instanceof ReturnSubStmt)) {
                addError(ErrorType.g, block.getLineNumber());
            }
        } else {
            isVoidFunc = true;
        }

        SymbolType funcSymType = funcType.isInt() ? SymbolType.IntFunc : SymbolType.VoidFunc;
        FuncSymbol funcSymbol = new FuncSymbol(funcSymType, ident.getValue(), funcFParams);
        curTable.addSymbol(funcSymbol);

        enter();  // 进入新的域，形参属于函数体内部的域
        if (funcFParams != null) {
            checkFuncFParams(funcFParams);
        }

        checkBlock(block);
        leave();   //离开当前域
        isVoidFunc = false;
    }

    /**
     * MainFuncDef → 'int' 'main' '(' ')' Block // g
     */
    public void checkMainFuncDef(MainFuncDef mainFuncDef) {
        Block block = mainFuncDef.getBlock();
        ArrayList<BlockItem> blockItems = block.getBlockItems();
        if (blockItems.isEmpty() || !(blockItems.get((blockItems.size() - 1)) instanceof ReturnSubStmt)) {
            addError(ErrorType.g, block.getLineNumber());
        }
        enter();
        checkBlock(block);
        leave();
    }

    /**
     * FuncIRType → 'void' | 'int'
     */
    public void checkFuncType(FuncType funcType) {

    }


    /**
     * FuncFParams → FuncFParam { ',' FuncFParam }
     */
    public void checkFuncFParams(FuncFParams funcFParams) {
        ArrayList<FuncFParam> parmas = funcFParams.getParams();
        for (FuncFParam param : parmas) {
            checkFuncFParam(param);
        }
    }

    /**
     * FuncFParam → BType Ident ['[' ']'] // b
     */
    public void checkFuncFParam(FuncFParam funcFParam) {
        Token ident = funcFParam.getIdent_token();
        boolean has_brack = funcFParam.has_brack();

        if (curTable.hasSymbol(ident.getValue())) {
            addError(ErrorType.b, ident.getLineNumber());
        }
        if (has_brack) {
            curTable.addSymbol(new VarSymbol(SymbolType.IntArray, ident.getValue(), isGlobal()));
        } else {
            curTable.addSymbol(new VarSymbol(SymbolType.Int, ident.getValue(), isGlobal()));
        }
    }

    /**
     * Block → '{' { BlockItem } '}'
     */
    public void checkBlock(Block block) {
        ArrayList<BlockItem> blockItems = block.getBlockItems();
        for (BlockItem blockItem : blockItems) {
            checkBlockItem(blockItem);
        }
    }

    /**
     * BlockItem → Decl | Stmt
     */
    public void checkBlockItem(BlockItem blockItem) {
        if (blockItem instanceof Decl) {
            checkDecl((Decl) blockItem);
        } else if (blockItem instanceof Stmt) {
            checkStmt((Stmt) blockItem);
        }
    }

    /**
     * 语句 Stmt → LVal '=' Exp ';' // h
     * | [Exp] ';'
     * | Block
     * | 'if' '(' Cond ')' Stmt [ 'else' Stmt ]
     * | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // h
     * | 'break' ';' | 'continue' ';' // m
     * | 'return' [Exp] ';' // f
     * | 'printf''('StringConst {','Exp}')'';' // l
     */
    public void checkStmt(Stmt stmt) {
        if (stmt instanceof LValSubStmt) {
            checkLValSubStmt((LValSubStmt) stmt);
        } else if (stmt instanceof ExpSubStmt) {
            checkExpSubStmt((ExpSubStmt) stmt);
        } else if (stmt instanceof BlockSubStmt) {
            checkBlockSubStmt((BlockSubStmt) stmt);
        } else if (stmt instanceof IfSubStmt) {
            checkIfSubStmt((IfSubStmt) stmt);
        } else if (stmt instanceof ForSubStmt) {
            checkForSubStmt((ForSubStmt) stmt);
        } else if (stmt instanceof BreakSubStmt) {
            checkBreakSubStmt((BreakSubStmt) stmt);
        } else if (stmt instanceof ContinueSubStmt) {
            checkContinueSubStmt((ContinueSubStmt) stmt);
        } else if (stmt instanceof ReturnSubStmt) {
            checkReturnSubStmt((ReturnSubStmt) stmt);
        } else if (stmt instanceof PrintfSubStmt) {
            checkPrintfSubStmt((PrintfSubStmt) stmt);
        } else {
            System.err.println("Unhandled stmt type: " + stmt.getClass().getName() + "in checkStmt");
        }
    }

    /**
     * Stmt → LVal '=' Exp ';' // h
     */
    public void checkLValSubStmt(LValSubStmt lValSubStmt) {
        LVal lVal = lValSubStmt.getLVal();
        Exp exp = lValSubStmt.getExp();

        Symbol symbol = curTable.findSymbol(lVal.getIdent().getValue());
        if (symbol != null) {   // 此处检查被赋值变量是否为常量， 被赋值变量是否被定义由LVal → Ident ['[' Exp ']'] // c 判断
            if (symbol instanceof ConstSymbol) {
                addError(ErrorType.h, lVal.getIdent().getLineNumber());
            }
        }
        checkLVal(lVal);
        checkExp(exp);
    }

    /**
     * Stmt → [Exp] ';'
     */
    public void checkExpSubStmt(ExpSubStmt expSubStmt) {
        Exp exp = expSubStmt.getExp();
        if (exp != null) {
            checkExp(exp);
        }

    }

    /**
     * Stmt → Block
     */
    public void checkBlockSubStmt(BlockSubStmt blockSubStmt) {
        Block block = blockSubStmt.getBlock();
        enter();  // 进入一个新的域
        checkBlock(block);
        leave();
    }

    /**
     * Stmt →'if' '(' Cond ')' Stmt [ 'else' Stmt ]
     */
    public void checkIfSubStmt(IfSubStmt ifSubStmt) {
        Cond cond = ifSubStmt.getCond();
        Stmt if_stmt = ifSubStmt.getStmt_if();
        Stmt else_stmt = ifSubStmt.getStmt_else();
        checkCond(cond);
        checkStmt(if_stmt);
        if (else_stmt != null) {
            checkStmt(else_stmt);
        }
    }

    /**
     * Stmt → 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt    // h
     * h 错误留给ForStmt与 Stmt中解决，此处判断应该冗余？
     */
    public void checkForSubStmt(ForSubStmt forSubStmt) {
        ForStmt forStmt_1 = forSubStmt.getForStmt_1();
        Cond cond = forSubStmt.getCond();
        ForStmt forStmt_2 = forSubStmt.getForStmt_2();
        Stmt stmt = forSubStmt.getStmt();
        if (forStmt_1 != null) {
            checkForStmt(forStmt_1);
        }
        if (cond != null) {
            checkCond(cond);
        }
        if (forStmt_2 != null) {
            checkForStmt(forStmt_2);
        }
        loopnum = loopnum + 1;
        checkStmt(stmt);
        loopnum = loopnum - 1;
    }

    /**
     * Stmt → 'break' ';' // m
     */
    public void checkBreakSubStmt(BreakSubStmt breakSubStmt) {
        if (loopnum == 0) {
            addError(ErrorType.m, breakSubStmt.getLineNumber());
        }
    }

    /**
     * Stmt → 'continue' ';' // m
     */
    public void checkContinueSubStmt(ContinueSubStmt continueSubStmt) {
        if (loopnum == 0) {
            addError(ErrorType.m, continueSubStmt.getLineNumber());
        }
    }

    /**
     * Stmt →  'return' [Exp] ';' f
     */
    public void checkReturnSubStmt(ReturnSubStmt returnSubStmt) {
        Exp exp = returnSubStmt.getExp();
        if (isVoidFunc) {
            if (exp != null) {    //TODO 如果是void函数，是不能出现return语句还是不能出现 return exp;  但是可以出现 return ;
                addError(ErrorType.f, returnSubStmt.getLineNumber());
            }
        }
        if (exp != null) {
            checkExp(exp);
        }
    }

    /**
     * Stmt → 'printf''('StringConst {','Exp}')'';' // l
     */
    public void checkPrintfSubStmt(PrintfSubStmt printfSubStmt) {

        String printf_string = printfSubStmt.getStringToken().getValue();
        ArrayList<Exp> exps = printfSubStmt.getExps();
        int count = 0;
        Pattern pattern = Pattern.compile("%d");
        Matcher matcher = pattern.matcher(printf_string);
        while (matcher.find()) {
            count++;
        }
        if (count != exps.size()) {
            addError(ErrorType.l, printfSubStmt.getLineNumber());
        }
        for (Exp exp : exps) {
            checkExp(exp);
        }
    }

    /**
     * ForStmt → LVal '=' Exp { ',' LVal '=' Exp } // h
     */
    public void checkForStmt(ForStmt forStmt) {
        ArrayList<LVal> lvals = forStmt.getLvals();
        ArrayList<Exp> exps = forStmt.getExps();
        for (LVal lval : lvals) {
            Symbol symbol = curTable.findSymbol(lval.getIdent().getValue());
            if (symbol != null) {   // 如果找到判断是否是常量值，找不到在checkLVal()中处理
                if (symbol instanceof ConstSymbol) {
                    addError(ErrorType.h, lval.getIdent().getLineNumber());
                }
            }
            checkLVal(lval);
        }
        for (Exp exp : exps) {
            checkExp(exp);
        }
    }

    /**
     * Exp → AddExp
     */
    public void checkExp(Exp exp) {
        AddExp addExp = exp.getAddExp();
        checkAddExp(addExp);
    }

    /**
     * Cond → LOrExp
     */
    public void checkCond(Cond cond) {
        LOrExp lOrExp = cond.getLOrExp();
        checkLOrExp(lOrExp);
    }

    /**
     * LVal → Ident ['[' Exp ']'] // c
     */
    public void checkLVal(LVal lVal) {
        Token ident = lVal.getIdent();
        Exp exp = lVal.getExp();
        Symbol symbol = curTable.findSymbol(ident.getValue());
        if (symbol == null || symbol instanceof FuncSymbol) {  // TODO需要排除FuncSymbol吗
            addError(ErrorType.c, ident.getLineNumber());
        }
        if (exp != null) {
            checkExp(exp);
        }
    }


    /**
     * PrimaryExp → '(' Exp ')' | LVal | Number
     */
    public void checkPrimaryExp(PrimaryExp primaryExp) {
        Exp exp = primaryExp.getExp();
        LVal lVal = primaryExp.getLVal();
        Number number = primaryExp.getNumber();
        if (exp != null) {
            checkExp(exp);
        }
        if (lVal != null) {
            checkLVal(lVal);
        }
        if (number != null) {
            checkNumber(number);
        }
    }

    /**
     * Number → IntConst
     */
    public void checkNumber(Number number) {
    }

    /**
     * UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp // c d e
     */
    public void checkUnaryExp(UnaryExp unaryExp) {
        PrimaryExp primaryExp = unaryExp.getPrimaryExp();
        Token ident_token = unaryExp.getIdent_token();
        FuncRParams funcRParams = unaryExp.getFuncRParams();
        UnaryOp unaryOp = unaryExp.getUnaryOp();
        UnaryExp unaryExp_2 = unaryExp.getUnaryExp();
        if (primaryExp != null) {
            checkPrimaryExp(primaryExp);
        }
        if (unaryOp != null) {
            checkUnaryOp(unaryOp);
            checkUnaryExp(unaryExp_2);
        }
        if (ident_token != null) {
            Symbol func_symbol = curTable.findSymbol(ident_token.getValue());
            if (func_symbol == null) {   //TODO 需要判断是否为FuncSymbol吗，应该保证了不会有函数与变量同名情况？
                addError(ErrorType.c, ident_token.getLineNumber());
            } else {
                FuncSymbol funcSymbol = (FuncSymbol) func_symbol;
                ArrayList<SymbolType> fparamsType = funcSymbol.getParamsType();   // 形参参数类型列表
                ArrayList<Exp> rparams = funcRParams != null ? funcRParams.getExps() : new ArrayList<>(); // 函数实参列表
                if (fparamsType.size() != rparams.size()) {  // 形参实参个数不匹配
                    addError(ErrorType.d, ident_token.getLineNumber());
                }
                // 检查d类错误需要首先获取实参exp的ident,其后判断其类型
                // 如果返回null(未找到标识符token),代表最终遇到了number或者Lval[exp]形式(实参是数组某一元素)，,一定为Int
                int parmCount = Math.min(fparamsType.size(), rparams.size());
                for (int i = 0; i < parmCount; i++) {
                    Token rp_token = rparams.get(i).tryGetIdent();
                    SymbolType fp_type = fparamsType.get(i);
                    if (rp_token == null) {
                        if (fp_type != SymbolType.Int) {
                            addError(ErrorType.e, ident_token.getLineNumber());
                        }
                    } else {
                        Symbol rp_symbol = curTable.findSymbol(rp_token.getValue());
                        if (rp_symbol == null) {   // 未找到该标识符，代表未定义错误，由checkLVal()与checkUnaryExp()检查
                            continue;
                        } else { //找到标识符
                            boolean is_fp_Array = fp_type == SymbolType.IntArray || fp_type == SymbolType.StaticIntArray || fp_type == SymbolType.ConstIntArray;
                            boolean is_rp_array = rp_symbol.isArray();
                            if (is_rp_array != is_fp_Array) {
                                addError(ErrorType.e, ident_token.getLineNumber());
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * UnaryOp → '+' | '−' | '!' 注：'!'仅出现在条件表达式中
     */
    public void checkUnaryOp(UnaryOp unaryOp) {
    }

    /**
     * FuncRParams → Exp { ',' Exp }
     */
    public void checkFuncRParams(FuncRParams funcRParams) {
        ArrayList<Exp> exps = funcRParams.getExps();
        for (Exp exp : exps) {
            checkExp(exp);
        }
    }

    /**
     * MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
     */
    public void checkMulExp(MulExp mulExp) {
        ArrayList<UnaryExp> unaryExps = mulExp.getUnaryExps();
        for (UnaryExp unaryExp : unaryExps) {
            checkUnaryExp(unaryExp);
        }
    }

    /**
     * AddExp → MulExp | AddExp ('+' | '−') MulExp
     */
    public void checkAddExp(AddExp addExp) {
        ArrayList<MulExp> mulExps = addExp.getMulExps();
        for (MulExp mulExp : mulExps) {
            checkMulExp(mulExp);
        }
    }

    /**
     * RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
     */
    public void checkRelExp(RelExp relExp) {
        ArrayList<AddExp> addExps = relExp.getAddExps();
        for (AddExp addExp : addExps) {
            checkAddExp(addExp);
        }
    }

    /**
     * EqExp → RelExp | EqExp ('==' | '!=') RelExp
     */
    public void checkEqExp(EqExp eqExp) {
        ArrayList<RelExp> relExps = eqExp.getRelExps();
        for (RelExp relExp : relExps) {
            checkRelExp(relExp);
        }
    }

    /**
     * LAndExp → EqExp | LAndExp '&&' EqExp
     */
    public void checkLAndExp(LAndExp landExp) {
        ArrayList<EqExp> eqExps = landExp.getEqExps();
        for (EqExp eqExp : eqExps) {
            checkEqExp(eqExp);
        }
    }

    /**
     * LOrExp → LAndExp | LOrExp '||' LAndExp
     */
    public void checkLOrExp(LOrExp lorExp) {
        ArrayList<LAndExp> lAndExps = lorExp.getLAndExps();
        for (LAndExp lAndExp : lAndExps) {
            checkLAndExp(lAndExp);
        }
    }

    /**
     * ConstExp → AddExp
     */
    public void checkConstExp(ConstExp constExp) {
        AddExp addExp = constExp.getAddExp();
        checkAddExp(addExp);
    }


}
