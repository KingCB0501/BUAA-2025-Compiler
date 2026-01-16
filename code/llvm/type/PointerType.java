package llvm.type;

public class PointerType extends BaseIntegerType {
    private LLVMType targetType;   // 指针指向的数据类型。可以直接写成baseIntegerTye吗？
    // TODO

    public PointerType(LLVMType targetType) {
        super(32);    // 指针类型也是32位整数数据类型
        this.targetType = targetType;
    }

    @Override
    public boolean isPointer() {
        return true;
    }

    public LLVMType getTargetType() {
        return targetType;
    }

    @Override
    public boolean isInt32() {
        return false;
    }

    @Override
    public boolean isInt1() {
        return false;
    }

    @Override
    public String toString() {
        return this.targetType.toString() + "*";
    }

    @Override
    public boolean isInteger() {
        return false;
    }

}
