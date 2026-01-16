package frontend.Parser;

import Utils.Error;
import Utils.ErrorLog;
import Utils.ErrorType;
import frontend.Lexer.Token;
import frontend.Lexer.TokenStream;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.Block;
import frontend.Parser.AST.BlockItem;
import frontend.Parser.AST.CompUnit;
import frontend.Parser.AST.ConstDecl;
import frontend.Parser.AST.ConstDef;
import frontend.Parser.AST.Exp.Cond;
import frontend.Parser.AST.Exp.ConstExp;
import frontend.Parser.AST.ConstInitVal;
import frontend.Parser.AST.Decl;
import frontend.Parser.AST.Exp.AddExp;
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

public class Parser {
    private TokenStream tokenStream;
    private static Parser parser = null;
    private static int errorLock = 0;


    private Parser(TokenStream tokenStream) {
        this.tokenStream = tokenStream;
    }

    public static Parser getInstance(TokenStream tokenStream) {
        if (parser == null) {
            parser = new Parser(tokenStream);
        }
        return parser;
    }

    public void checkParseError(TokenType tokenType) {
        if (tokenType == TokenType.SEMICN) { // ';'
            if (tokenStream.peek().isType(TokenType.SEMICN)) {
                tokenStream.popCurrentToken();
            } else if (errorLock == 0) {
                ErrorLog.getInstance().addError(new Error(ErrorType.i, tokenStream.last().getLineNumber()));
            }
        } else if (tokenType == TokenType.RPARENT) {  // ')'
            if (tokenStream.peek().isType(TokenType.RPARENT)) {
                tokenStream.popCurrentToken();
            } else if (errorLock == 0) {
                ErrorLog.getInstance().addError(new Error(ErrorType.j, tokenStream.last().getLineNumber()));
            }
        } else if (tokenType == TokenType.RBRACK) { // ']'
            if (tokenStream.peek().isType(TokenType.RBRACK)) {
                tokenStream.popCurrentToken();
            } else if (errorLock == 0) {
                ErrorLog.getInstance().addError(new Error(ErrorType.k, tokenStream.last().getLineNumber()));
            }
        }
    }

    /**
     * CompUnit → {Decl} {FuncDef} MainFuncDef
     */
    public CompUnit parseCompUnit() {
        ArrayList<Decl> decls = new ArrayList<>();
        ArrayList<FuncDef> funcDefs = new ArrayList<>();
        MainFuncDef mainFuncDef = null;
        while (true) {
            Token token1 = tokenStream.peek(1);
            Token token2 = tokenStream.peek(2);
            Token token3 = tokenStream.peek(3);
//            if (token1.isType(TokenType.EOF) || token2.isType(TokenType.EOF) || token3.isType(TokenType.EOF)) {
//                break;
//            }
            // FuncDef
            if ((token1.isType(TokenType.INTTK) || token1.isType(TokenType.VOIDTK))
                    && token2.isType(TokenType.IDENFR) && token3.isType(TokenType.LPARENT)) {
                funcDefs.add(parseFuncDef());
            } else if (token1.isType(TokenType.INTTK) && token2.isType(TokenType.MAINTK)) {  // int main
                mainFuncDef = parseMainFuncDef();
            } else if (token1.isType(TokenType.CONSTTK) || token1.isType(TokenType.INTTK) || token1.isType(TokenType.STATICTK)) {
                decls.add(parseDecl());
            } else {
                break;
            }
        }
//        System.out.println(tokenStream.peek().getLineNumber());
        return new CompUnit(decls, funcDefs, mainFuncDef);
    }

    /**
     * Decl → ConstDecl | VarDecl
     */
    public Decl parseDecl() {
        Token token1 = tokenStream.peek();
        if (token1.isType(TokenType.CONSTTK)) {
            return parseConstDecl();
        } else {
            return parseVarDecl();
        }
    }

    /**
     * ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // i
     */
    public ConstDecl parseConstDecl() {
        tokenStream.popCurrentToken();  // const
        tokenStream.popCurrentToken();   // int
        ArrayList<ConstDef> constDefs = new ArrayList<>();
        constDefs.add(parseConstDef()); // constDef
        while (tokenStream.peek().isType(TokenType.COMMA)) {   //,
            tokenStream.popCurrentToken();   // 跳过','
            constDefs.add(parseConstDef());
        }
        checkParseError(TokenType.SEMICN);
        return new ConstDecl(constDefs);
    }

    /**
     * ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal // k
     */
    public ConstDef parseConstDef() {
        Token identToken = tokenStream.popCurrentToken();  // Ident
        ConstExp constExp = null;
        if (tokenStream.peek().isType(TokenType.LBRACK)) {  // 如果是ident[constExp]形式
            tokenStream.popCurrentToken(); // 跳过'['
            constExp = parseConstExp();
            checkParseError(TokenType.RBRACK);
        }
        tokenStream.popCurrentToken(); // '='
        ConstInitVal constInitVal = parseConstInitVal();
        return new ConstDef(identToken, constExp, constInitVal);
    }

    /**
     * ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}'
     */
    public ConstInitVal parseConstInitVal() {
        boolean have_brace = false;
        ArrayList<ConstExp> constExps = new ArrayList<>();
        if (tokenStream.peek().isType(TokenType.LBRACE)) {
            have_brace = true;
            tokenStream.popCurrentToken(); // '{'
            if (!tokenStream.peek().isType(TokenType.RBRACE)) {
                constExps.add(parseConstExp());
                while (tokenStream.peek().isType(TokenType.COMMA)) {
                    tokenStream.popCurrentToken();  // ','
                    constExps.add(parseConstExp());
                }
            }
            tokenStream.popCurrentToken();   // '}'
        } else {
            constExps.add(parseConstExp());
        }
        return new ConstInitVal(constExps, have_brace);
    }


    /**
     * VarDecl → [ 'static' ] BType VarDef { ',' VarDef } ';' // i
     */
    public VarDecl parseVarDecl() {
        boolean isStatic = false;
        if (tokenStream.peek().isType(TokenType.STATICTK)) {  // 检查是否是static
            isStatic = true;
            tokenStream.popCurrentToken();
        }
        tokenStream.popCurrentToken();  // int_token
        ArrayList<VarDef> varDefs = new ArrayList<>();
        varDefs.add(parseVarDef());
        while (tokenStream.peek().isType(TokenType.COMMA)) {
            tokenStream.popCurrentToken(); // 跳过','
            varDefs.add(parseVarDef());
        }
        checkParseError(TokenType.SEMICN);
        return new VarDecl(isStatic, varDefs);
    }

    /**
     * VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal // k
     */
    public VarDef parseVarDef() {
        Token ident_token = tokenStream.popCurrentToken();
        ConstExp constExp = null;
        InitVal initVal = null;
        if (tokenStream.peek().isType(TokenType.LBRACK)) {
            tokenStream.popCurrentToken();  // '[
            constExp = parseConstExp();
            checkParseError(TokenType.RBRACK);
        }
        if (tokenStream.peek().isType(TokenType.ASSIGN)) {
            tokenStream.popCurrentToken(); // '='
            initVal = parseInitVal();
        }
        return new VarDef(ident_token, constExp, initVal);
    }

    /**
     * InitVal → Exp | '{' [ Exp { ',' Exp } ] '}'
     */
    public InitVal parseInitVal() {
        ArrayList<Exp> exps = new ArrayList<>();
        boolean have_brace = false;
        if (tokenStream.peek().isType(TokenType.LBRACE)) {
            tokenStream.popCurrentToken(); // '{'
            have_brace = true;
            if (!tokenStream.peek().isType(TokenType.RBRACE)) {
                exps.add(parseExp());
            }
            while (tokenStream.peek().isType(TokenType.COMMA)) {
                tokenStream.popCurrentToken();
                exps.add(parseExp());
            }
            tokenStream.popCurrentToken(); // '}'
        } else {
            exps.add(parseExp());
        }
        return new InitVal(exps, have_brace);
    }


    /**
     * FuncDef → FuncType Ident '(' [FuncFParams] ')' Block // j
     */
    public FuncDef parseFuncDef() {
        FuncType funcType = parseFuncType();
        Token ident_token = tokenStream.popCurrentToken();
        FuncFParams funcFparams = null;
        Block block = null;
        tokenStream.popCurrentToken();  // '('

        if (tokenStream.peek().isType(TokenType.INTTK)) {
//            System.out.println("1");
            funcFparams = parseFuncFParams();
        }
        checkParseError(TokenType.RPARENT);
        block = parseBlock();
        return new FuncDef(funcType, ident_token, funcFparams, block);
    }

    /**
     * MainFuncDef → 'int' 'main' '(' ')' Block // j
     */
    public MainFuncDef parseMainFuncDef() {
        tokenStream.popCurrentToken(); // int
        tokenStream.popCurrentToken(); // main
        tokenStream.popCurrentToken(); // (
        checkParseError(TokenType.RPARENT);
        Block block = parseBlock();
        return new MainFuncDef(block);
    }

    /**
     * FuncType → 'void' | 'int'
     */
    public FuncType parseFuncType() {
        Token funcType_token = tokenStream.popCurrentToken();
        return new FuncType(funcType_token);
    }

    /**
     * FuncFParams → FuncFParam { ',' FuncFParam }
     */
    public FuncFParams parseFuncFParams() {
        ArrayList<FuncFParam> params = new ArrayList<>();
        params.add(parseFuncFParam());
        while (tokenStream.peek().isType(TokenType.COMMA)) {
            tokenStream.popCurrentToken();
            params.add(parseFuncFParam());
        }
        return new FuncFParams(params);
    }

    /**
     * FuncFParam → BType Ident ['[' ']'] // k
     */
    public FuncFParam parseFuncFParam() {
        tokenStream.popCurrentToken();  // int
        Token ident_token = tokenStream.popCurrentToken(); // ident
        boolean have_brack = false;
        if (tokenStream.peek().isType(TokenType.LBRACK)) {
            tokenStream.popCurrentToken();
            have_brack = true;
            checkParseError(TokenType.RBRACK);
        }
        return new FuncFParam(ident_token, have_brack);
    }

    /**
     * Block → '{' { BlockItem } '}'
     */
    public Block parseBlock() {
        tokenStream.popCurrentToken();  // "{"
        ArrayList<BlockItem> blockItems = new ArrayList<>();
        while (!tokenStream.peek().isType(TokenType.RBRACE)) {
//            System.out.println(tokenStream.peek());
//            System.out.println("1");
            blockItems.add(parseBlockItem());
        }
        Token rp_token = tokenStream.popCurrentToken();  // '}'
        return new Block(blockItems, rp_token.getLineNumber());

    }

    /**
     * BlockItem → Decl | Stmt
     */
    public BlockItem parseBlockItem() {
        Token t1 = tokenStream.peek();
        if (t1.isType(TokenType.CONSTTK) || t1.isType(TokenType.STATICTK) || t1.isType(TokenType.INTTK)) {
            return parseDecl();
        } else {
            return parseStmt();
        }
    }

    /**
     * Stmt六大类
     * Stmt → LVal '=' Exp ';' // i
     * | [Exp] ';' // i
     * | Block
     * | 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // j
     * | 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
     * | 'break' ';' | 'continue' ';' // i
     * | 'return' [Exp] ';' // i
     * | 'printf''('StringConst {','Exp}')'';' // i j
     */
    public Stmt parseStmt() {
        Token t1 = tokenStream.peek();
        if (t1.isType(TokenType.LBRACE)) {
            return parseBlockSubStmt();
        } else if (t1.isType(TokenType.IFTK)) {
            return parseIfSubStmt();
        } else if (t1.isType(TokenType.FORTK)) {
            return parseForSubStmt();
        } else if (t1.isType(TokenType.BREAKTK)) {
            return parseBreakSubStmt();
        } else if (t1.isType(TokenType.CONTINUETK)) {
            return parseContinueSubStmt();
        } else if (t1.isType(TokenType.RETURNTK)) {
            return parseReturnSubStmt();
        } else if (t1.isType(TokenType.PRINTFTK)) {
            return parsePrintfSubStmt();
        }
        // 判断[Exp] ';'   error: i
        // 与 LVal '=' 'Exp' ';'   error:  i
        // LVal → Ident ['[' Exp ']']

        // Exp的首字符落在UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp 之一
        //  PrimaryExp → '(' Exp ')' | LVal | Number   ||| ( 、 Number
        //  Ident '(' [FuncRParams] ')'                ||| "Ident ("
        //  UnaryOp → '+' | '−' | '!'

        // 如果是 +、 -、 ！、(、Number、Ident(、;则可以判定为ExpSubStmt
        Token t2 = tokenStream.peek(2);
        if (t1.isType(TokenType.PLUS) || t1.isType(TokenType.MINU) || t1.isType(TokenType.NOT)
                || t1.isType(TokenType.LPARENT) || t1.isType(TokenType.INTCON)
                || (t1.isType(TokenType.IDENFR) && t2.isType(TokenType.LPARENT))
                || t1.isType(TokenType.SEMICN)) {
            return parseExpSubStmt();
        }
        // 否则 先解析 LVal，之后根据是否有 =判段类型
        int watchPoint = tokenStream.getTokenIndex();  // 解析LVal前记住该位置
        errorLock++;   //  停止语法分析错误记录, 类似与mips中临时变量的维护
        parseLVal();
        errorLock--;   // 重新开始语法分析错误记录
        if (tokenStream.peek().isType(TokenType.ASSIGN)) {
            tokenStream.setTokenIndex(watchPoint);
            return parseLValSubStmt();
        } else {
            tokenStream.setTokenIndex(watchPoint);
            return parseExpSubStmt();
        }

    }

    /**
     * Stmt → LVal '=' Exp ';' // i
     */
    public LValSubStmt parseLValSubStmt() {
        LVal lval = parseLVal();
        tokenStream.popCurrentToken(); // =
        Exp exp = parseExp();
        checkParseError(TokenType.SEMICN);
        return new LValSubStmt(lval, exp);

    }

    /**
     * Stmt → [Exp] ';'  // i
     */
    public ExpSubStmt parseExpSubStmt() {
        Token t1 = tokenStream.peek();
        if (t1.isType(TokenType.SEMICN)) {
            tokenStream.popCurrentToken();
            return new ExpSubStmt(null);
        } else {
            Exp exp = parseExp();
            checkParseError(TokenType.SEMICN);
            return new ExpSubStmt(exp);
        }
//        Exp exp = null;
//        if (t1.isType(TokenType.PLUS) || t1.isType(TokenType.MINU) || t1.isType(TokenType.NOT)
//                || t1.isType(TokenType.LPARENT) || t1.isType(TokenType.INTCON)
//                || (t1.isType(TokenType.IDENFR))) {
//            exp = parseExp();
//        }
//        checkParseError(TokenType.SEMICN);
//        return new ExpSubStmt(exp);
    }

    /**
     * Stmt → Block
     */
    public BlockSubStmt parseBlockSubStmt() {
        Block block = parseBlock();
        return new BlockSubStmt(block);
    }

    /**
     * Stmt →'if' '(' Cond ')' Stmt [ 'else' Stmt ] // j
     */
    public IfSubStmt parseIfSubStmt() {
        tokenStream.popCurrentToken();  // "if"
        tokenStream.popCurrentToken();  // '('
        Cond cond = parseCond();
        checkParseError(TokenType.RPARENT);
        Stmt stmt_if = parseStmt();
        Stmt stmt_else = null;
        if (tokenStream.peek().isType(TokenType.ELSETK)) {
            tokenStream.popCurrentToken();  // "else"
            stmt_else = parseStmt();
        }
        return new IfSubStmt(cond, stmt_if, stmt_else);
    }

    /**
     * Stmt → 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
     */
    public ForSubStmt parseForSubStmt() {
        tokenStream.popCurrentToken(); // for
        tokenStream.popCurrentToken(); // (
        ForStmt forStmt_1 = null;
        Cond cond = null;
        ForStmt forStmt_2 = null;
        Stmt stmt;
        if (!tokenStream.peek().isType(TokenType.SEMICN)) {
            forStmt_1 = parseForStmt();
        }
        tokenStream.popCurrentToken(); // ;
        if (!tokenStream.peek().isType(TokenType.SEMICN)) {
            cond = parseCond();
        }
        tokenStream.popCurrentToken(); // ;
        if (!tokenStream.peek().isType(TokenType.RPARENT)) {
            forStmt_2 = parseForStmt();
        }
        tokenStream.popCurrentToken(); // )
        stmt = parseStmt();
        return new ForSubStmt(forStmt_1, cond, forStmt_2, stmt);
    }

    /**
     * Stmt → 'break' ';' // i
     */
    public BreakSubStmt parseBreakSubStmt() {
        Token break_token = tokenStream.popCurrentToken();  // break
        checkParseError(TokenType.SEMICN);
        return new BreakSubStmt(break_token);
    }

    /**
     * Stmt → 'continue' ';' // i
     */
    public ContinueSubStmt parseContinueSubStmt() {
        Token continue_token = tokenStream.popCurrentToken();  // continue
        checkParseError(TokenType.SEMICN);
        return new ContinueSubStmt(continue_token);
    }

//    {
//        return
//        a = b
//    }

    /**
     * Stmt →  'return' [Exp] ';' // i
     */
    public ReturnSubStmt parseReturnSubStmt() {
//        tokenStream.popCurrentToken();  // return
//        Token t1 = tokenStream.peek();
//        Exp exp = null;
//        if (t1.isType(TokenType.PLUS) || t1.isType(TokenType.MINU) || t1.isType(TokenType.NOT)
//                || t1.isType(TokenType.LPARENT) || t1.isType(TokenType.INTCON)
//                || (t1.isType(TokenType.IDENFR))) {
//            exp = parseExp();
//        }
//        checkParseError(TokenType.SEMICN);
//
//        return new ReturnSubStmt(exp);
        Token return_token = tokenStream.popCurrentToken();  // return
        Token t1 = tokenStream.peek();
        if (t1.isType(TokenType.RBRACE)) {
            checkParseError(TokenType.SEMICN);
            return new ReturnSubStmt(null, return_token);
        }
        if (t1.isType(TokenType.CONSTTK) || t1.isType(TokenType.STATICTK) || t1.isType(TokenType.INTTK)
                || t1.isType(TokenType.LBRACE) || t1.isType(TokenType.IFTK)
                || t1.isType(TokenType.FORTK) || t1.isType(TokenType.BREAKTK)
                || t1.isType(TokenType.CONTINUETK) || t1.isType(TokenType.RETURNTK)
                || t1.isType(TokenType.PRINTFTK)) {
            checkParseError(TokenType.SEMICN);
            return new ReturnSubStmt(null, return_token);
        }
        if (t1.isType(TokenType.SEMICN)) {
            checkParseError(TokenType.SEMICN);
            return new ReturnSubStmt(null, return_token);
        }

        if (t1.isType(TokenType.PLUS) || t1.isType(TokenType.MINU) || t1.isType(TokenType.NOT)
                || t1.isType(TokenType.LPARENT) || t1.isType(TokenType.INTCON)
                || (t1.isType(TokenType.IDENFR) && tokenStream.peek(2).isType(TokenType.LPARENT))) {
            Exp exp = parseExp();
            checkParseError(TokenType.SEMICN);
            return new ReturnSubStmt(exp, return_token);
        } else {
            int watchPoint = tokenStream.getTokenIndex();
            errorLock++;
            parseLVal();
            errorLock--;
            if (tokenStream.peek().isType(TokenType.ASSIGN)) {
                tokenStream.setTokenIndex(watchPoint);
                checkParseError(TokenType.SEMICN);
                return new ReturnSubStmt(null, return_token);
            } else {
                tokenStream.setTokenIndex(watchPoint);
                Exp exp = parseExp();
                checkParseError(TokenType.SEMICN);
                return new ReturnSubStmt(exp, return_token);
            }
        }


    }

    /**
     * Stmt → 'printf''('StringConst {','Exp}')'';' // i j
     */
    public PrintfSubStmt parsePrintfSubStmt() {
        Token printf_token = tokenStream.popCurrentToken(); // printf
        tokenStream.popCurrentToken(); // (
        Token string_token = tokenStream.popCurrentToken(); // StringConst
        ArrayList<Exp> exps = new ArrayList<>();
        while (tokenStream.peek().isType(TokenType.COMMA)) {
            tokenStream.popCurrentToken();  // ,
            exps.add(parseExp());
        }
        checkParseError(TokenType.RPARENT);
        checkParseError(TokenType.SEMICN);
        return new PrintfSubStmt(string_token, exps, printf_token);
    }

    /**
     * ForStmt → LVal '=' Exp { ',' LVal '=' Exp }
     */
    public ForStmt parseForStmt() {
        ArrayList<LVal> lvals = new ArrayList<>();
        ArrayList<Exp> exps = new ArrayList<>();
        lvals.add(parseLVal());
        tokenStream.popCurrentToken(); // =
        exps.add(parseExp());
        while (tokenStream.peek().isType(TokenType.COMMA)) {
            tokenStream.popCurrentToken();
            lvals.add(parseLVal());
            tokenStream.popCurrentToken();
            exps.add(parseExp());
        }
        return new ForStmt(lvals, exps);
    }

    /**
     * Exp → AddExp
     */
    public Exp parseExp() {
        AddExp addExp = parseAddExp();
        return new Exp(addExp);
    }

    /**
     * Cond → LOrExp
     */
    public Cond parseCond() {
        LOrExp lorExp = parseLOrExp();
        return new Cond(lorExp);
    }

    /**
     * LVal → Ident ['[' Exp ']'] // k
     */
    public LVal parseLVal() {
        Token ident_token = tokenStream.popCurrentToken();
        Exp exp = null;
        if (tokenStream.peek().isType(TokenType.LBRACK)) {
            tokenStream.popCurrentToken();  // [
            exp = parseExp();
            checkParseError(TokenType.RBRACK);
        }
        return new LVal(ident_token, exp);
    }

    /**
     * PrimaryExp → '(' Exp ')' | LVal | Number // j
     */
    public PrimaryExp parsePrimaryExp() {
        Exp exp = null;
        LVal lval = null;
        Number number = null;
        Token t1 = tokenStream.peek();
        if (t1.isType(TokenType.LPARENT)) {
            tokenStream.popCurrentToken(); // (
            exp = parseExp();
            checkParseError(TokenType.RPARENT);
        } else if (t1.isType(TokenType.INTCON)) {
            number = parseNumber();
        } else if (t1.isType(TokenType.IDENFR)) {
            lval = parseLVal();
        }
        return new PrimaryExp(exp, lval, number);
    }

    /**
     * Number → IntConst
     */
    public Number parseNumber() {
        Token number_token = tokenStream.popCurrentToken();  // intcon
        return new Number(number_token);
    }

    /**
     * UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp // j
     */
    public UnaryExp parseUnaryExp() {
        PrimaryExp primaryExp = null;
        Token ident_token = null;
        FuncRParams funcRParams = null;
        UnaryOp unaryOp = null;
        UnaryExp unaryExp = null;
        Token t1 = tokenStream.peek();
        Token t2 = tokenStream.peek(2);
        if (t1.isType(TokenType.PLUS) || t1.isType(TokenType.MINU) || t1.isType(TokenType.NOT)) {
            unaryOp = parseUnaryOp();
            unaryExp = parseUnaryExp();
        } else if (t1.isType(TokenType.IDENFR) && t2.isType(TokenType.LPARENT)) {
            ident_token = tokenStream.popCurrentToken();
            tokenStream.popCurrentToken(); // (
            Token top_token = tokenStream.peek();
            if (top_token.isType(TokenType.PLUS) || top_token.isType(TokenType.MINU) || top_token.isType(TokenType.NOT)
                    || top_token.isType(TokenType.LPARENT) || top_token.isType(TokenType.INTCON)
                    || (top_token.isType(TokenType.IDENFR))) {
                funcRParams = parseFuncRParams();
            }
            checkParseError(TokenType.RPARENT);
        } else {
            primaryExp = parsePrimaryExp();
        }
        return new UnaryExp(primaryExp, ident_token, funcRParams, unaryOp, unaryExp);
    }

    /**
     * UnaryOp → '+' | '−' | '!'
     */
    public UnaryOp parseUnaryOp() {
        Token op_token = tokenStream.popCurrentToken();
        return new UnaryOp(op_token);
    }

    /**
     * FuncRParams → Exp { ',' Exp }
     */
    public FuncRParams parseFuncRParams() {
        ArrayList<Exp> exps = new ArrayList<>();
        exps.add(parseExp());
        while (tokenStream.peek().isType(TokenType.COMMA)) {
            tokenStream.popCurrentToken();
            exps.add(parseExp());
        }
        return new FuncRParams(exps);
    }

    /**
     * MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
     * 改写为 MulExp → UnaryExp {('*' | '/' | '%') UnaryExp}
     */
    public MulExp parseMulExp() {
        ArrayList<UnaryExp> unaryExps = new ArrayList<>();
        ArrayList<Token> op_tokens = new ArrayList<>();
        unaryExps.add(parseUnaryExp());
        while (tokenStream.peek().isType(TokenType.MULT) || tokenStream.peek().isType(TokenType.DIV)
                || tokenStream.peek().isType(TokenType.MOD)) {
            op_tokens.add(tokenStream.popCurrentToken());
            unaryExps.add(parseUnaryExp());
        }
        return new MulExp(unaryExps, op_tokens);
    }

    /**
     * AddExp → MulExp | AddExp ('+' | '−') MulExp
     * 去除左递归 AddExp → MulExp {('+' | '−') MulExp}
     */
    public AddExp parseAddExp() {
        ArrayList<MulExp> mulExps = new ArrayList<>();
        ArrayList<Token> op_tokens = new ArrayList<>();
        mulExps.add(parseMulExp());
        while (tokenStream.peek().isType(TokenType.PLUS) || tokenStream.peek().isType(TokenType.MINU)) {
            op_tokens.add(tokenStream.popCurrentToken());
            mulExps.add(parseMulExp());
        }
        return new AddExp(mulExps, op_tokens);
    }

    /**
     * RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
     * 改写左递归 RelExp → AddExp {('<' | '>' | '<=' | '>=') AddExp}
     */
    public RelExp parseRelExp() {
        ArrayList<AddExp> addExps = new ArrayList<>();
        ArrayList<Token> op_tokens = new ArrayList<>();
        addExps.add(parseAddExp());
        while (tokenStream.peek().isType(TokenType.LSS) || tokenStream.peek().isType(TokenType.LEQ)
                || tokenStream.peek().isType(TokenType.GEQ) || tokenStream.peek().isType(TokenType.GRE)) {
            op_tokens.add(tokenStream.popCurrentToken());
            addExps.add(parseAddExp());
        }
        return new RelExp(addExps, op_tokens);
    }

    /**
     * EqExp → RelExp | EqExp ('==' | '!=') RelExp
     * 改写左递归为 EqExp → RelExp {('==' | '!=') RelExp}
     */
    public EqExp parseEqExp() {
        ArrayList<RelExp> relExps = new ArrayList<>();
        ArrayList<Token> op_tokens = new ArrayList<>();
        relExps.add(parseRelExp());
        while (tokenStream.peek().isType(TokenType.EQL) || tokenStream.peek().isType(TokenType.NEQ)) {
            op_tokens.add(tokenStream.popCurrentToken());
            relExps.add(parseRelExp());
        }
        return new EqExp(relExps, op_tokens);
    }

    /**
     * LAndExp → EqExp | LAndExp '&&' EqExp
     * 去除左递归  LAndExp → EqExp {'&&' EqExp}
     */
    public LAndExp parseLAndExp() {
        ArrayList<EqExp> eqExps = new ArrayList<>();
        ArrayList<Token> op_tokens = new ArrayList<>();
        eqExps.add(parseEqExp());
        while (tokenStream.peek().isType(TokenType.AND)) {
            op_tokens.add(tokenStream.popCurrentToken());
            eqExps.add(parseEqExp());
        }
        return new LAndExp(eqExps, op_tokens);
    }

    /**
     * LOrExp → LAndExp | LOrExp '||' LAndExp
     * 去除左递归 LOrExp → LAndExp {'||' LAndExp}
     */
    public LOrExp parseLOrExp() {
        ArrayList<LAndExp> lAndExps = new ArrayList<>();
        ArrayList<Token> op_tokens = new ArrayList<>();
        lAndExps.add(parseLAndExp());
        while (tokenStream.peek().isType(TokenType.OR)) {
            op_tokens.add(tokenStream.popCurrentToken());
            lAndExps.add(parseLAndExp());
        }
        return new LOrExp(lAndExps, op_tokens);
    }

    /**
     * ConstExp → AddExp
     */
    public ConstExp parseConstExp() {
        AddExp addExp = parseAddExp();
        return new ConstExp(addExp);
    }
}
