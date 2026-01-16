package frontend.Parser.AST.Stmt;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.BlockItem;
import frontend.Parser.AST.Node;

public class BreakSubStmt extends Node implements Stmt, BlockItem {

    Token break_token;

    public BreakSubStmt(Token break_token) {
        this.break_token = break_token;
    }

    public int getLineNumber() {
        return break_token.getLineNumber();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Token.TokenPrint(TokenType.BREAKTK));
        sb.append(Token.TokenPrint(TokenType.SEMICN));
        sb.append("<Stmt>\n");
        return sb.toString();
    }
}
