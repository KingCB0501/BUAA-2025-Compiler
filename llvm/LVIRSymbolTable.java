package llvm;

import java.util.ArrayList;
import java.util.HashMap;

public class LVIRSymbolTable {
    private LVIRSymbolTable parent;
    private HashMap<String, Value> symbols;
    private ArrayList<LVIRSymbolTable> children;


    public LVIRSymbolTable(LVIRSymbolTable parent) {
        this.parent = parent;
        this.symbols = new HashMap<>();
        this.children = new ArrayList<>();
    }

    public boolean isRootSymbolTable() {
        return this.parent == null;
    }

    public LVIRSymbolTable getParent() {
        return this.parent;
    }

    public void addChild(LVIRSymbolTable child) {
        this.children.add(child);
    }

    public void addSymbol(String name, Value value) {
        this.symbols.put(name, value);
    }

    public Value find(String name) {
        if (this.symbols.containsKey(name)) {
            return this.symbols.get(name);
        }

        LVIRSymbolTable temp = this.parent;
        while (temp != null) {
            if (temp.symbols.containsKey(name)) {
                return temp.symbols.get(name);
            }
            temp = temp.parent;
        }
        return null;
    }
}
