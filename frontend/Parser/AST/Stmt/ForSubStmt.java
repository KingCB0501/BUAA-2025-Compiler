package frontend.Parser.AST.Stmt;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.BlockItem;
import frontend.Parser.AST.Exp.Cond;
import frontend.Parser.AST.ForStmt;
import frontend.Parser.AST.Node;

/**
 * Stmt → 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
 */
public class ForSubStmt extends Node implements BlockItem, Stmt {
    private ForStmt forStmt_1;
    private Cond cond;
    private ForStmt forStmt_2;
    private Stmt stmt;

    public ForSubStmt(ForStmt forStmt_1, Cond cond, ForStmt forStmt_2, Stmt stmt) {
        this.forStmt_1 = forStmt_1;
        this.cond = cond;
        this.forStmt_2 = forStmt_2;
        this.stmt = stmt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Token.TokenPrint(TokenType.FORTK));
        sb.append(Token.TokenPrint(TokenType.LPARENT));
        if (forStmt_1 != null) {
            sb.append(forStmt_1.toString());
        }
        sb.append(Token.TokenPrint(TokenType.SEMICN));
        if (cond != null) {
            sb.append(cond.toString());
        }
        sb.append(Token.TokenPrint(TokenType.SEMICN));
        if (forStmt_2 != null) {
            sb.append(forStmt_2.toString());
        }
        sb.append(Token.TokenPrint(TokenType.RPARENT));
        sb.append(stmt.toString());
        sb.append("<Stmt>\n");
        return sb.toString();
    }
}
