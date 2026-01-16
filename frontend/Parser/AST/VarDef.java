package frontend.Parser.AST;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.Exp.ConstExp;

public class VarDef extends Node {
    private Token ident;
    private ConstExp constExp;
    private InitVal initVal;

    public VarDef(Token ident, ConstExp constExp, InitVal initVal) {
        this.ident = ident;
        this.constExp = constExp;
        this.initVal = initVal;
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
        if (initVal != null) {
            sb.append(Token.TokenPrint(TokenType.ASSIGN));
            sb.append(initVal.toString());
        }
        sb.append("<VarDef>\n");
        return sb.toString();
    }
}
