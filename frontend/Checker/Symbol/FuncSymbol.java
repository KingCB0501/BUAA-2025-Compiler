package frontend.Checker.Symbol;

import frontend.Parser.AST.FuncFParam;
import frontend.Parser.AST.FuncFParams;
import frontend.Parser.AST.FuncType;

import java.util.ArrayList;

public class FuncSymbol extends Symbol {
    private ArrayList<SymbolType> paramsType;    // 存储形参类型

    public FuncSymbol(SymbolType funcType, String funcName, ArrayList<SymbolType> paramsType) {
        super(funcType, funcName);
        this.paramsType = paramsType;
    }

    public FuncSymbol(SymbolType funcType, String funcName, FuncFParams funcFParams) {
        super(funcType, funcName);
        this.paramsType = new ArrayList<>();

        ArrayList<FuncFParam> funcFParamsList = funcFParams != null ? funcFParams.getParams() : new ArrayList<>();
        for (FuncFParam fParam : funcFParamsList) {
            if (fParam.has_brack()) {
                paramsType.add(SymbolType.IntArray);
            } else {
                paramsType.add(SymbolType.Int);
            }
        }
    }


    public ArrayList<SymbolType> getParamsType() {
        return paramsType;
    }

    @Override
    public boolean isConst() {
        return false;
    }
}
