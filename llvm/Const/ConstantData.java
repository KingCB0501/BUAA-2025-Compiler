package llvm.Const;

import llvm.Value;
import llvm.type.LLVMType;

public class ConstantData extends Value implements Constant {
    private int num;

    public ConstantData(LLVMType type, int num) {
        super(String.valueOf(num),type);
        this.num = num;
    }

    public int getNum() {
        return num;
    }

    @Override
    public String toString() {
        return "" + num;
    }

}
