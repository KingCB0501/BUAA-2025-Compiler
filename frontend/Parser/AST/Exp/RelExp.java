package frontend.Parser.AST.Exp;

import frontend.Lexer.Token;
import frontend.Parser.AST.Node;

import java.util.ArrayList;

/**
 * RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
 * 改写左递归 RelExp → AddExp {('<' | '>' | '<=' | '>=') AddExp}
 */
public class RelExp extends Node {
    private ArrayList<AddExp> addExps;
    private ArrayList<Token> op_tokens;

    public RelExp(ArrayList<AddExp> addExps, ArrayList<Token> op_tokens) {
        this.addExps = addExps;
        this.op_tokens = op_tokens;
    }

    public ArrayList<AddExp> getAddExps() {
        return addExps;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(addExps.get(0).toString());
        sb.append("<RelExp>\n");
        for (int i = 1; i < addExps.size(); i++) {
            sb.append(op_tokens.get(i - 1).toString());
            sb.append(addExps.get(i).toString());
            sb.append("<RelExp>\n");
        }
        return sb.toString();
    }
}
