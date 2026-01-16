package frontend.Checker.Symbol;

public class VarSymbol extends Symbol {
    private Boolean isGlobal;

    public VarSymbol(SymbolType symbolType, String name, Boolean isGlobal) {
        super(symbolType, name);
        this.isGlobal = isGlobal;
    }

    public Boolean isGlobal() {
        return isGlobal;
    }

    @Override
    public boolean isConst() {
        return false;
    }
}
