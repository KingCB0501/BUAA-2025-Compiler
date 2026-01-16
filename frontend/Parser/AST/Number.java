package frontend.Parser.AST;

import frontend.Lexer.Token;

public class Number {
    private Token number_token;

    public Number(Token number_token) {
        this.number_token = number_token;
    }

    public int getValue() {
        String numString = number_token.getValue();
        return Integer.parseInt(numString);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(number_token.toString());
        sb.append("<Number>\n");
        return sb.toString();
    }
}
