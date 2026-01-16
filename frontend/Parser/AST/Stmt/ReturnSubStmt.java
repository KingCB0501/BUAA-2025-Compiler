package frontend.Parser.AST.Stmt;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.BlockItem;
import frontend.Parser.AST.Exp.Exp;
import frontend.Parser.AST.Node;

public class ReturnSubStmt extends Node implements BlockItem, Stmt {
    private Exp exp;
    Token return_token;

    public ReturnSubStmt(Exp exp, Token return_token) {
        this.exp = exp;
        this.return_token = return_token;
    }

    public Exp getExp() {
        return exp;
    }

    public int getLineNumber() {
        return return_token.getLineNumber();
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
