package frontend.Parser.AST.Exp;

import frontend.Lexer.Token;
import frontend.Parser.AST.Node;

import java.util.ArrayList;

/**
 * LAndExp → EqExp | LAndExp '&&' EqExp
 * 去除左递归  LAndExp → EqExp {'&&' EqExp}
 */
public class LAndExp extends Node {
    ArrayList<EqExp> eqExps;
    ArrayList<Token> op_tokens;

    public LAndExp(ArrayList<EqExp> eqExps, ArrayList<Token> op_tokens) {
        this.eqExps = eqExps;
        this.op_tokens = op_tokens;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(eqExps.get(0).toString());
        sb.append("<LAndExp>\n");
        for (int i = 1; i < eqExps.size(); i++) {
            sb.append(op_tokens.get(i - 1).toString());
            sb.append(eqExps.get(i).toString());
            sb.append("<LAndExp>\n");
        }
        return sb.toString();
    }
}
