package frontend.Parser.AST;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;

import java.util.ArrayList;

public class FuncFParams extends Node {
    private ArrayList<FuncFParam> params;

    public FuncFParams(ArrayList<FuncFParam> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(params.get(0).toString());
        for (int i = 1; i < params.size(); i++) {
            sb.append(Token.TokenPrint(TokenType.COMMA));
            sb.append(params.get(i).toString());
        }
        sb.append("<FuncFParams>\n");
        return sb.toString();
    }
}
