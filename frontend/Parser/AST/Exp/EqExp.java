package frontend.Parser.AST.Exp;

import frontend.Lexer.Token;
import frontend.Parser.AST.Node;

import java.util.ArrayList;

/**
 * EqExp → RelExp | EqExp ('==' | '!=') RelExp
 * 改写左递归为 EqExp → RelExp {('==' | '!=') RelExp}
 */
public class EqExp extends Node {
    private ArrayList<RelExp> relExps;
    private ArrayList<Token> op_tokens;

    public EqExp(ArrayList<RelExp> relExps, ArrayList<Token> op_tokens) {
        this.relExps = relExps;
        this.op_tokens = op_tokens;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(relExps.get(0).toString());
        sb.append("<EqExp>\n");
        for (int i = 1; i < relExps.size(); i++) {
            sb.append(op_tokens.get(i - 1).toString());
            sb.append(relExps.get(i).toString());
            sb.append("<EqExp>\n");
        }
        return sb.toString();
    }
}
