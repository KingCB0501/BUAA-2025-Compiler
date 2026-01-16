package frontend.Checker.Symbol;

public class Symbol {
    private SymbolType type;
    private String name;

    public Symbol(SymbolType type, String name) {
        this.type = type;
        this.name = name;
    }

    public SymbolType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean isConst() {
        return false;
    }

    public boolean isArray() {
        return this.type == SymbolType.ConstIntArray || this.type == SymbolType.IntArray || this.type == SymbolType.StaticIntArray;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(" ");
        sb.append(type);
        sb.append("\n");
        return sb.toString();
    }

}
