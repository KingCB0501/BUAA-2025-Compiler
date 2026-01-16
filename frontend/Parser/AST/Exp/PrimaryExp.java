package frontend.Parser.AST.Exp;

import frontend.Checker.Symbol.SymbolType;
import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.Node;
import frontend.Parser.AST.Number;

public class PrimaryExp extends Node {
    private Exp exp;
    private LVal lval;
    private Number number;

    public PrimaryExp(Exp exp, LVal lval, Number number) {
        this.exp = exp;
        this.lval = lval;
        this.number = number;
    }

    public Exp getExp() {
        return exp;
    }

    public LVal getLVal() {
        return lval;
    }

    public Number getNumber() {
        return number;
    }

    public Token tryGetIdent() {
        if (exp != null) {
            return exp.tryGetIdent();
        } else if (lval != null) {
            return lval.tryGetIdent();
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (exp != null) {
            sb.append(Token.TokenPrint(TokenType.LPARENT));
            sb.append(exp.toString());
            sb.append(Token.TokenPrint(TokenType.RPARENT));
        } else if (lval != null) {
            sb.append(lval.toString());
        } else if (number != null) {
            sb.append(number.toString());
        }
        sb.append("<PrimaryExp>\n");
        return sb.toString();
    }
}
