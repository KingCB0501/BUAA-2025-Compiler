package frontend.Lexer;

import java.util.ArrayList;

public class TokenStream {
    private final ArrayList<Token> tokenList;

    public TokenStream() {
        tokenList = new ArrayList<>();
    }

    public void addToken(Token token) {
        tokenList.add(token);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Token token : tokenList) {
            sb.append(token.toString());
        }
        return sb.toString();
    }
}
