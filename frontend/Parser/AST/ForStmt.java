package frontend.Parser.AST;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.Exp.Exp;
import frontend.Parser.AST.Exp.LVal;

import java.util.ArrayList;

/**
 * ForStmt → LVal '=' Exp { ',' LVal '=' Exp }
 */
public class ForStmt extends Node {
    private ArrayList<LVal> lvals;    // <LVal, Exp>对
    private ArrayList<Exp> exps;

    public ForStmt(ArrayList<LVal> lvals, ArrayList<Exp> exps) {
        this.lvals = lvals;
        this.exps = exps;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lvals.get(0).toString());
        sb.append(Token.TokenPrint(TokenType.ASSIGN));
        sb.append(exps.get(0).toString());
        for (int i = 1; i < exps.size(); i++) {
            sb.append(Token.TokenPrint(TokenType.COMMA));
            sb.append(lvals.get(i).toString());
            sb.append(Token.TokenPrint(TokenType.ASSIGN));
            sb.append(exps.get(i).toString());
        }
        sb.append("<ForStmt>\n");
        return sb.toString();
    }
}
