package frontend.Parser.AST;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;

import java.util.ArrayList;

public class ConstDecl extends Node implements Decl, BlockItem {
    private ArrayList<ConstDef> constDefs;

    /**
     * ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';' // i
     */
    public ConstDecl(ArrayList<ConstDef> constDefs) {
        this.constDefs = constDefs;
    }

    public ArrayList<ConstDef> getConstDefs() {
        return constDefs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Token.TokenPrint(TokenType.CONSTTK));  // "CONSTTK const\n"
        sb.append(Token.TokenPrint(TokenType.INTTK));
        sb.append(constDefs.get(0).toString());
        for (int i = 1; i < constDefs.size(); i++) {
            sb.append(Token.TokenPrint(TokenType.COMMA));
            sb.append(constDefs.get(i).toString());
        }
        sb.append(Token.TokenPrint(TokenType.SEMICN));
        sb.append("<ConstDecl>\n");
        return sb.toString();
    }
}
