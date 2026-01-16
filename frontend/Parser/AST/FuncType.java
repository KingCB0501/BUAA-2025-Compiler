package frontend.Parser.AST;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;

public class FuncType {
    private Token token;   // void或者int

    public FuncType(Token token) {
        this.token = token;
    }

    public Token getToken() {
        return token;
    }

    public boolean isVoid() {
        return this.token.isType(TokenType.VOIDTK);
    }

    public boolean isInt() {
        return this.token.isType(TokenType.INTTK);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(token.toString());
        sb.append("<FuncType>\n");
        return sb.toString();
    }
}
