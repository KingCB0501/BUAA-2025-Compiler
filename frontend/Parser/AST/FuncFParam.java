package frontend.Parser.AST;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;

public class FuncFParam extends Node {
    private Token ident_token;
    private boolean has_brack;

    public FuncFParam(Token ident_token, boolean has_brack) {
        this.ident_token = ident_token;
        this.has_brack = has_brack;
    }

    public boolean has_brack() {
        return has_brack;
    }

    public Token getIdent_token() {
        return ident_token;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Token.TokenPrint(TokenType.INTTK));
        sb.append(ident_token.toString());
        if (has_brack) {
            sb.append(Token.TokenPrint(TokenType.LBRACK));
            sb.append(Token.TokenPrint(TokenType.RBRACK));
        }
        sb.append("<FuncFParam>\n");
        return sb.toString();
    }
}
