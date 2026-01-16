package frontend.Parser.AST.Exp;

import frontend.Parser.AST.Node;

public class ConstExp extends Node {
    private AddExp addExp;

    public ConstExp(AddExp addExp) {
        this.addExp = addExp;
    }

    public AddExp getAddExp() {
        return addExp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(addExp.toString());
        sb.append("<ConstExp>\n");
        return sb.toString();
    }
}
