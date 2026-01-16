package frontend.Parser.AST.Stmt;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.BlockItem;
import frontend.Parser.AST.Exp.Cond;
import frontend.Parser.AST.Node;

/**
 * Stmt → 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // j
 */
public class IfSubStmt extends Node implements Stmt, BlockItem {
    private Cond cond;
    private Stmt stmt_if;
    private Stmt stmt_else;

    public IfSubStmt(Cond cond, Stmt stmt_if, Stmt stmt_else) {
        this.cond = cond;
        this.stmt_if = stmt_if;
        this.stmt_else = stmt_else;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Token.TokenPrint(TokenType.IFTK));
        sb.append(Token.TokenPrint(TokenType.LPARENT));
        sb.append(cond.toString());
        sb.append(Token.TokenPrint(TokenType.RPARENT));
        sb.append(stmt_if.toString());
        if (stmt_else != null) {
            sb.append(Token.TokenPrint(TokenType.ELSETK));
            sb.append(stmt_else.toString());
        }
        sb.append("<Stmt>\n");
        return sb.toString();
    }
}
