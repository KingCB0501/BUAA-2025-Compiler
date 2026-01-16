package frontend.Parser.AST.Exp;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.CompUnit;
import frontend.Parser.AST.FuncRParams;
import frontend.Parser.AST.Node;

/**
 * UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
 */
public class UnaryExp extends Node {
    private PrimaryExp primaryExp;
    private Token ident_token;
    private FuncRParams funcRParams;
    private UnaryOp unaryOp;
    private UnaryExp unaryExp;

    public UnaryExp(PrimaryExp primaryExp, Token ident_token, FuncRParams funcRParams, UnaryOp unaryOp, UnaryExp unaryExp) {
        this.primaryExp = primaryExp;
        this.ident_token = ident_token;
        this.funcRParams = funcRParams;
        this.unaryOp = unaryOp;
        this.unaryExp = unaryExp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (primaryExp != null) {
            sb.append(primaryExp.toString());
        } else if (ident_token != null) {
            sb.append(ident_token.toString());
            sb.append(Token.TokenPrint(TokenType.LPARENT));
            if (funcRParams != null) {
                sb.append(funcRParams.toString());
            }
            sb.append(Token.TokenPrint(TokenType.RPARENT));
        } else {
            sb.append(unaryOp.toString());
            sb.append(unaryExp.toString());
        }
        sb.append("<UnaryExp>\n");
        return sb.toString();
    }
}
