package frontend.Parser.AST;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.Exp.Exp;

import java.util.ArrayList;

public class InitVal extends Node {
    private ArrayList<Exp> exps;
    private boolean having_brace;   // "{"

    public InitVal(ArrayList<Exp> exps, boolean having_brace) {
        this.exps = exps;
        this.having_brace = having_brace;
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (having_brace) {
            sb.append(Token.TokenPrint(TokenType.LBRACE));
            if (!exps.isEmpty()) {
                sb.append(exps.get(0).toString());
                for (int i = 1; i < exps.size(); i++) {
                    sb.append(Token.TokenPrint(TokenType.COMMA));
                    sb.append(exps.get(i).toString());
                }
            }
            sb.append(Token.TokenPrint(TokenType.RBRACE));
        } else {
            sb.append(exps.get(0).toString());
        }
        sb.append("<InitVal>\n");
        return sb.toString();
    }
}
