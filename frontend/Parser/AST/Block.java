package frontend.Parser.AST;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;

import java.util.ArrayList;

public class Block extends Node {
    private ArrayList<BlockItem> blockItems;
    private int lineNumber;   //记录右花括号'}'的行号, 主要用作checker中检查return相关错误时记录行号

    public Block(ArrayList<BlockItem> blockItems, int lineNumber) {
        this.blockItems = blockItems;
        this.lineNumber = lineNumber;
    }

    public ArrayList<BlockItem> getBlockItems() {
        return blockItems;
    }

    public int getLineNumber() {
        return lineNumber;
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
