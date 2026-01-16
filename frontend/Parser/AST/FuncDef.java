package frontend.Parser.AST;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;

public class FuncDef {
    private FuncType funcType;
    private Token ident_token;
    private FuncFParams funcFparams;
    private Block block;

    public FuncDef(FuncType funcType, Token ident_token, FuncFParams funcFparams, Block block) {
        this.funcType = funcType;
        this.ident_token = ident_token;
        this.funcFparams = funcFparams;
        this.block = block;
    }

    public FuncType getFuncType() {
        return funcType;
    }
    public Token getIdent_token() {
        return ident_token;
    }
    public FuncFParams getFuncFparams() {
        return funcFparams;
    }
    public Block getBlock() {
        return block;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(funcType.toString());
        sb.append(ident_token.toString());
        sb.append(Token.TokenPrint(TokenType.LPARENT));
        if (funcFparams != null) {
            sb.append(funcFparams.toString());
        }
        sb.append(Token.TokenPrint(TokenType.RPARENT));
        sb.append(block.toString());
        sb.append("<FuncDef>\n");
        return sb.toString();
    }
}
