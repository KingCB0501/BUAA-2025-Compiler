package frontend.Parser.AST.Exp;

import frontend.Lexer.Token;
import frontend.Parser.AST.Node;

public class UnaryOp extends Node {
    private Token op_token;

    public UnaryOp(Token op_token) {
        this.op_token = op_token;
    }

    public Token getOpToken() {
        return op_token;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(op_token.toString());
        sb.append("<UnaryOp>\n");
        return sb.toString();
    }
}
