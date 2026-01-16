package frontend.Parser.AST.Stmt;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.BlockItem;
import frontend.Parser.AST.Exp.Exp;
import frontend.Parser.AST.Exp.LVal;
import frontend.Parser.AST.Node;

public class LValSubStmt extends Node implements BlockItem, Stmt {
    private LVal lval;
    private Exp exp;

    public LValSubStmt(LVal lval, Exp exp) {
        this.lval = lval;
        this.exp = exp;
    }

    public LVal getLVal() {
        return lval;
    }

    public Exp getExp() {
        return exp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lval.toString());
        sb.append(Token.TokenPrint(TokenType.ASSIGN));
        sb.append(exp.toString());
        sb.append(Token.TokenPrint(TokenType.SEMICN));
        sb.append("<Stmt>\n");
        return sb.toString();
    }
}
