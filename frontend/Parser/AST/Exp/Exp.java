package frontend.Parser.AST.Exp;

import frontend.Parser.AST.Node;

public class Exp extends Node {
    private AddExp addExp;

    public Exp(AddExp addExp) {
        this.addExp = addExp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(addExp.toString());
        sb.append("<Exp>\n");
        return sb.toString();
    }
}
