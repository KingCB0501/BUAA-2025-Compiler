package frontend.Parser.AST.Exp;

import frontend.Parser.AST.Node;

public class Cond extends Node {
    private LOrExp lOrExp;

    public Cond(LOrExp lOrExp) {
        this.lOrExp = lOrExp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(lOrExp.toString());
        sb.append("<Cond>\n");
        return sb.toString();
    }
}