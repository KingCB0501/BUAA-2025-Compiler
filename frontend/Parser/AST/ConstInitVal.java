package frontend.Parser.AST;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.Exp.ConstExp;

import java.util.ArrayList;

public class ConstInitVal extends Node {
    private boolean having_brace;  // having_brace = true, 则是右侧 ConstInitVal
    private ArrayList<ConstExp> constExps;

    /**
     * ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}'
     */
    public ConstInitVal(ArrayList<ConstExp> constExps, boolean having_brace) {
        this.constExps = constExps;
        this.having_brace = having_brace;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (having_brace) {
            sb.append(Token.TokenPrint(TokenType.LBRACE));
            if (!constExps.isEmpty()) {
                sb.append(constExps.get(0).toString());
            }
            for (int i = 1; i < constExps.size(); i++) {
                sb.append(Token.TokenPrint(TokenType.COMMA));
                sb.append(constExps.get(i).toString());
            }
            sb.append(Token.TokenPrint(TokenType.RBRACE));
        } else {
            sb.append(constExps.get(0).toString());
        }
        sb.append("<ConstInitVal>\n");
        return sb.toString();
    }
}
