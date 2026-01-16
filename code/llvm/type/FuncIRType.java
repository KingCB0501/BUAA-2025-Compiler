package llvm.type;

import java.util.ArrayList;

public class FuncIRType extends LLVMType {
    private BaseIntegerType retType;   // 返回值类型
    private ArrayList<BaseIntegerType> fParamTypes;

    public FuncIRType(BaseIntegerType retType, ArrayList<BaseIntegerType> fParamTypes) {
        this.retType = retType;
        this.fParamTypes = fParamTypes;
    }

    @Override
    public boolean isFunction() {
        return true;
    }


    public LLVMType getRetType() {
        return this.retType;
    }

    @Override
    public int getSize() {
        return this.retType.getSize();
    }

    @Override
    public int getAlign() {
        return this.retType.getAlign();
    }

    @Override
    public String toString() {
        return retType.toString();
    }
}
