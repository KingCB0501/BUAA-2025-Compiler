package frontend.Checker;

import frontend.Checker.Symbol.Symbol;
import frontend.Lexer.Token;

import java.util.ArrayList;

public class SymbolTable {
    private int id;   // 作用域序号
    private ArrayList<Symbol> symbols;      // 存储当前作用域下的symbol
    private SymbolTable parent;     // 存放父作用域的符号表, 最外层作用域无父作用域
    private ArrayList<SymbolTable> children;     // 存放当前作用域的直接子作用域的符号表

    public SymbolTable(int id, SymbolTable parent) {
        this.id = id;
        this.parent = parent;
        this.symbols = new ArrayList<>();
        this.children = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    /**
     * 获取父作用域的符号表
     */
    public SymbolTable getParentTable() {
        return parent;
    }

    /**
     * 获取直接子作用域的符号表
     */
    public ArrayList<SymbolTable> getChildrenTables() {
        return children;
    }

    /**
     * 获取当前作用域的符号表
     */
    public ArrayList<Symbol> getSymbols() {
        return symbols;
    }

    /**
     * 增加当前作用域的符号
     */
    public void addSymbol(Symbol symbol) {
        symbols.add(symbol);
    }

    /**
     * 增加当前子作用域的符号表
     */
    public void addChildrenTable(SymbolTable symbolTable) {
        children.add(symbolTable);
    }

    /**
     * 判断当前作用域内是否有名称为flag_name的符号
     */
    public boolean hasSymbol(String flag_name) {
        for (Symbol symbol : symbols) {
            if (symbol.getName().equals(flag_name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 在当前域及父作用域查找名为flag_name的symbol
     */
    public Symbol findSymbol(String flag_name) {
        SymbolTable curTable = this;
        while (curTable != null) {
            for (Symbol symbol : curTable.symbols) {
                if (symbol.getName().equals(flag_name)) {
                    return symbol;
                }
            }
            curTable = curTable.getParentTable();
        }
        return null;
    }

    /**
     * 专门用于最后删除roottable的getint项
     */
    public void removeSymbol(Symbol symbol) {
        symbols.remove(symbol);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Symbol symbol : symbols) {
            sb.append(this.id);
            sb.append(" ");
            sb.append(symbol.toString());
        }
        for (SymbolTable child : children) {
            sb.append(child.toString());
        }
        return sb.toString();
    }
}
