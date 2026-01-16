package frontend.Parser.AST.Stmt;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.BlockItem;
import frontend.Parser.AST.Exp.Exp;
import frontend.Parser.AST.Node;

import java.util.ArrayList;

/**
 * Stmt → 'printf''('StringConst {','Exp}')'';'
 */
public class PrintfSubStmt extends Node implements BlockItem, Stmt {
    private Token string_token;
    private ArrayList<Exp> exps;

    public PrintfSubStmt(Token string_token, ArrayList<Exp> exps) {
        this.string_token = string_token;
        this.exps = exps;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Token.TokenPrint(TokenType.PRINTFTK));
        sb.append(Token.TokenPrint(TokenType.LPARENT));
        sb.append(string_token.toString());
        for (Exp exp : exps) {
            sb.append(Token.TokenPrint(TokenType.COMMA));
            sb.append(exp.toString());
        }
        sb.append(Token.TokenPrint(TokenType.RPARENT));
        sb.append(Token.TokenPrint(TokenType.SEMICN));
        sb.append("<Stmt>\n");
        return sb.toString();
    }
}
