package frontend.Checker.Symbol;

public class ConstSymbol extends Symbol {
    private Boolean isGlobal;

    public ConstSymbol(SymbolType symbolType, String name, Boolean isGlobal) {
        super(symbolType, name);
        this.isGlobal = isGlobal;
    }

    public Boolean isGlobal() {
        return isGlobal;
    }

    @Override
    public boolean isConst() {
        return true;
    }
}
