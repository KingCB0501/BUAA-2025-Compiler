package frontend.Parser.AST;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.Exp.ConstExp;

public class ConstDef extends Node {
    private Token ident;
    private ConstExp constExp;
    private ConstInitVal constInitVal;

    /**
     * ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal // k
     */
    public ConstDef(Token ident, ConstExp constExp, ConstInitVal constInitVal) {
        this.ident = ident;
        this.constExp = constExp;
        this.constInitVal = constInitVal;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(ident.toString());
        if (constExp != null) {
            sb.append(Token.TokenPrint(TokenType.LBRACK));
            sb.append(constExp.toString());
            sb.append(Token.TokenPrint(TokenType.RBRACK));
        }
        sb.append(Token.TokenPrint(TokenType.ASSIGN));
        sb.append(constInitVal.toString());
        sb.append("<ConstDef>\n");
        return sb.toString();
    }
}
