package llvm;

import llvm.type.BaseIntegerType;
import llvm.type.LLVMType;

public class Value {
    private String name;
    private LLVMType type;

    public Value(String name, LLVMType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * 匿名数据存储
     */
    public Value(LLVMType type) {
        this.type = type;
    }

    public boolean isINT32() {
        if (this.getType() instanceof BaseIntegerType) {
            if (((BaseIntegerType) this.getType()).isInt32()) {
                return true;
            }
        }
        return false;
    }

    public boolean isINT1() {
        if (this.getType() instanceof BaseIntegerType) {
            if (((BaseIntegerType) this.getType()).isInt1()) {
                return true;
            }
        }
        return false;
    }

    public LLVMType getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
