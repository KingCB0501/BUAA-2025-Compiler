package frontend.Parser.AST.Exp;

import frontend.Lexer.Token;
import frontend.Parser.AST.Node;

import java.util.ArrayList;

/**
 * AddExp → MulExp | AddExp ('+' | '−') MulExp
 * 去除左递归 AddExp → MulExp {('+' | '−') MulExp}
 */
public class AddExp extends Node {
    private ArrayList<MulExp> mulExps;
    private ArrayList<Token> op_tokens;

    public AddExp(ArrayList<MulExp> mulExps, ArrayList<Token> op_tokens) {
        this.mulExps = mulExps;
        this.op_tokens = op_tokens;
    }

    public ArrayList<MulExp> getMulExps() {
        return mulExps;
    }

    public Token tryGetIdent() {
        return mulExps.get(0).tryGetIdent();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mulExps.get(0).toString());
        sb.append("<AddExp>\n");
        for (int i = 1; i < mulExps.size(); i++) {
            sb.append(op_tokens.get(i - 1).toString());
            sb.append(mulExps.get(i).toString());
            sb.append("<AddExp>\n");
        }
        return sb.toString();
    }
}
