package frontend.Parser.AST.Stmt;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.BlockItem;
import frontend.Parser.AST.Node;

/**
 * Stmt →  'continue' ';'
 */
public class ContinueSubStmt extends Node implements BlockItem, Stmt {
    Token continue_token;

    public ContinueSubStmt(Token continue_token) {
        this.continue_token = continue_token;
    }

    public int getLineNumber() {
        return continue_token.getLineNumber();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Token.TokenPrint(TokenType.CONTINUETK));
        sb.append(Token.TokenPrint(TokenType.SEMICN));
        sb.append("<Stmt>\n");
        return sb.toString();
    }
}
