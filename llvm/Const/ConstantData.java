package llvm.Const;

import llvm.Value;
import llvm.type.LLVMType;

public class ConstantData extends Value implements Constant {
    private int num;
    private boolean noSense = false;    // 在phi指令生成是该值是否被用作填充

    public ConstantData(LLVMType type, int num) {
        super(String.valueOf(num), type);
        this.num = num;
    }

    public ConstantData(LLVMType type, int num, boolean noSense) {
        super(String.valueOf(num), type);
        this.num = num;
        this.noSense = noSense;
    }

    public int getNum() {
        return num;
    }

    @Override
    public String toString() {
        return "" + num;
    }

    public boolean isNoSense() {
        return noSense;
    }

}
