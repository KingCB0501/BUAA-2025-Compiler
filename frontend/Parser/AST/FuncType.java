package frontend.Parser.AST;

import frontend.Lexer.Token;

public class FuncType {
    private Token token;   // void或者int

    public FuncType(Token token) {
        this.token = token;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(token.toString());
        sb.append("<FuncType>\n");
        return sb.toString();
    }
}
