package frontend.Parser.AST;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;

import java.util.ArrayList;

public class Block extends Node {
    private ArrayList<BlockItem> blockItems;

    public Block(ArrayList<BlockItem> blockItems) {
        this.blockItems = blockItems;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Token.TokenPrint(TokenType.LBRACE));
        for (BlockItem blockItem : blockItems) {
            sb.append(blockItem.toString());
        }
        sb.append(Token.TokenPrint(TokenType.RBRACE));
        sb.append("<Block>\n");
        return sb.toString();
    }
}
