package frontend.Parser.AST.Exp;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.Node;

/**
 * LVal → Ident ['[' Exp ']']
 */
public class LVal extends Node {
    private Token ident_token;
    private Exp exp;

    public LVal(Token ident_token, Exp exp) {
        this.ident_token = ident_token;
        this.exp = exp;
    }

    public Token getIdent() {
        return ident_token;
    }

    public Exp getExp() {
        return exp;
    }

    public Token tryGetIdent() {
        if (exp != null) {
            return null;   // 例如a[1] 这种形式实际上是Int, 但是直接返回ident查询符号表会得到其为intArray,导致类型判断出错
        } else {
            return ident_token;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident_token.toString());
        if (exp != null) {
            sb.append(Token.TokenPrint(TokenType.LBRACK));
            sb.append(exp.toString());
            sb.append(Token.TokenPrint(TokenType.RBRACK));
        }
        sb.append("<LVal>\n");
        return sb.toString();
    }
}
