package frontend.Parser.AST.Exp;

import frontend.Lexer.Token;
import frontend.Parser.AST.Node;

import java.util.ArrayList;

/**
 * LOrExp → LAndExp | LOrExp '||' LAndExp
 * 去除左递归 LOrExp → LAndExp {'||' LAndExp}
 */
public class LOrExp extends Node {

    ArrayList<LAndExp> lAndExps;
    ArrayList<Token> op_tokens;

    public LOrExp(ArrayList<LAndExp> lAndExps, ArrayList<Token> op_tokens) {
        this.lAndExps = lAndExps;
        this.op_tokens = op_tokens;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lAndExps.get(0).toString());
        sb.append("<LOrExp>\n");
        for (int i = 1; i < lAndExps.size(); i++) {
            sb.append(op_tokens.get(i - 1).toString());
            sb.append(lAndExps.get(i).toString());
            sb.append("<LOrExp>\n");
        }
        return sb.toString();
    }
}
