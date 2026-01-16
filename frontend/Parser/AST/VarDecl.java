package frontend.Parser.AST;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;

import java.util.ArrayList;

public class VarDecl extends Node implements Decl, BlockItem {
    private boolean isStatic;
    private ArrayList<VarDef> varDefs;

    /**
     * VarDecl → [ 'static' ] BType VarDef { ',' VarDef } ';' // i
     */
    public VarDecl(boolean isStatic, ArrayList<VarDef> varDefs) {
        this.isStatic = isStatic;
        this.varDefs = varDefs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isStatic) {
            sb.append(Token.TokenPrint(TokenType.STATICTK));
        }
        sb.append(Token.TokenPrint(TokenType.INTTK));
        sb.append(varDefs.get(0).toString());
        for (int i = 1; i < varDefs.size(); i++) {
            sb.append(Token.TokenPrint(TokenType.COMMA));
            sb.append(varDefs.get(i).toString());
        }
        sb.append(Token.TokenPrint(TokenType.SEMICN));
        sb.append("<VarDecl>\n");
        return sb.toString();
    }
}
