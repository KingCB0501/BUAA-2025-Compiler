package frontend.Parser.AST;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;

public class MainFuncDef extends Node {
    private Block block;

    public MainFuncDef(Block block) {
        this.block = block;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Token.TokenPrint(TokenType.INTTK));
        sb.append(Token.TokenPrint(TokenType.MAINTK));
        sb.append(Token.TokenPrint(TokenType.LPARENT));
        sb.append(Token.TokenPrint(TokenType.RPARENT));
        sb.append(block.toString());
        sb.append("<MainFuncDef>\n");
        return sb.toString();
    }
}
