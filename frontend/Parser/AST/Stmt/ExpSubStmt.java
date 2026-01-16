package frontend.Parser.AST.Stmt;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.BlockItem;
import frontend.Parser.AST.Exp.Exp;
import frontend.Parser.AST.Node;

public class ExpSubStmt extends Node implements Stmt, BlockItem {
    private Exp exp;

    public ExpSubStmt(Exp exp) {
        this.exp = exp;
    }

    public Exp getExp() {
        return exp;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (exp != null) {
            sb.append(exp.toString());
        }
        sb.append(Token.TokenPrint(TokenType.SEMICN));
        sb.append("<Stmt>\n");
        return sb.toString();
    }
}
