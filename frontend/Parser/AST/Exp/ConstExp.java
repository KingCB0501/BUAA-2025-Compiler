package frontend.Parser.AST.Exp;

import frontend.Parser.AST.Node;

public class ConstExp extends Node {
    private AddExp addExp;

    public ConstExp(AddExp addExp) {
        this.addExp = addExp;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(addExp.toString());
        sb.append("<ConstExp>\n");
        return sb.toString();
    }
}
