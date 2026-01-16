package frontend.Parser.AST;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.Exp.Exp;

import java.util.ArrayList;

public class FuncRParams extends Node {
    private ArrayList<Exp> exps;

    public FuncRParams(ArrayList<Exp> exps) {
        this.exps = exps;
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(exps.get(0).toString());
        for (int i = 1; i < exps.size(); i++) {
            sb.append(Token.TokenPrint(TokenType.COMMA));
            sb.append(exps.get(i).toString());
        }
        sb.append("<FuncRParams>\n");
        return sb.toString();
    }
}