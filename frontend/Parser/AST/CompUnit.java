package frontend.Parser.AST;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.Exp.Exp;

import java.util.ArrayList;

public class CompUnit extends Node {

    private ArrayList<Decl> decls;
    private ArrayList<FuncDef> funcDefs;
    private MainFuncDef mainFuncDef;

    /**
     * CompUnit → {Decl} {FuncDef} MainFuncDef
     */
    public CompUnit(ArrayList<Decl> decls, ArrayList<FuncDef> funcDefs, MainFuncDef mainFuncDef) {
        this.decls = decls;
        this.funcDefs = funcDefs;
        this.mainFuncDef = mainFuncDef;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Decl decl : decls) {
            sb.append(decl.toString());
        }
        for (FuncDef funcDef : funcDefs) {
            sb.append(funcDef.toString());
        }
        sb.append(mainFuncDef.toString());
        sb.append("<CompUnit>\n");
        return sb.toString();
    }


}
