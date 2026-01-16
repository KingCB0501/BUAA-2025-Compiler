package frontend.Parser.AST.Stmt;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.BlockItem;
import frontend.Parser.AST.Exp.Exp;
import frontend.Parser.AST.Node;

public class ReturnSubStmt extends Node implements BlockItem, Stmt {
    private Exp exp;

    public ReturnSubStmt(Exp exp) {
        this.exp = exp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Token.TokenPrint(TokenType.RETURNTK));
        if (exp != null) {
            sb.append(exp.toString());
        }
        sb.append(Token.TokenPrint(TokenType.SEMICN));
        sb.append("<Stmt>\n");
        return sb.toString();
    }
}
