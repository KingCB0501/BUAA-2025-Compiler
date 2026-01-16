package frontend.Parser.AST.Exp;

import frontend.Lexer.Token;
import frontend.Parser.AST.Node;

import java.util.ArrayList;

/**
 * MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
 * 改写为 MulExp → UnaryExp {('*' | '/' | '%') UnaryExp}
 */
public class MulExp extends Node {
    private ArrayList<UnaryExp> unaryExps;
    private ArrayList<Token> op_tokens;

    public MulExp(ArrayList<UnaryExp> unaryExps, ArrayList<Token> op_tokens) {
        this.unaryExps = unaryExps;
        this.op_tokens = op_tokens;
    }

    public ArrayList<UnaryExp> getUnaryExps() {
        return unaryExps;
    }

    public Token tryGetIdent() {
        return unaryExps.get(0).tryGetIdent();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(unaryExps.get(0).toString());
        sb.append("<MulExp>\n");
        for (int i = 1; i < unaryExps.size(); i++) {
            sb.append(op_tokens.get(i - 1).toString());
            sb.append(unaryExps.get(i).toString());
            sb.append("<MulExp>\n");
        }
        return sb.toString();
    }
}
