package frontend.Lexer;

import java.util.ArrayList;

public class TokenStream {
    private final ArrayList<Token> tokenList;
    private int tokenIndex = 0;

    public TokenStream() {
        tokenList = new ArrayList<>();
    }

    public void addToken(Token token) {
        tokenList.add(token);
    }

    public int getTokenIndex() {
        return tokenIndex;
    }

    public void setTokenIndex(int tokenIndex) {
        this.tokenIndex = tokenIndex;
    }

    public Token popCurrentToken() {
        tokenIndex += 1;
        return tokenList.get(tokenIndex - 1);
    }

    public Token peek() {
        if (tokenIndex >= tokenList.size()) {
            return new Token(TokenType.EOF, "", -1);
        }
        return tokenList.get(tokenIndex);
    }

    public Token peek(int rank) {
        if (tokenIndex + rank - 1 >= tokenList.size()) {
            return new Token(TokenType.EOF, "", -1);
        }
        return tokenList.get(tokenIndex + rank - 1);
    }

    public Token last() {
        if (tokenIndex == 0) {
            return new Token(TokenType.EOF, "", -1);
        }
        return tokenList.get(tokenIndex - 1);
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
