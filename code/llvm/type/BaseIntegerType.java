package llvm.type;

/**
 * 基础数据类型, i1, i32
 */
public class BaseIntegerType extends LLVMType {
    private int bits;

    // TODO 数据类型非常多，每个数据类型都申请一个类的话，是否会爆内存
    public BaseIntegerType(int bits) {
        this.bits = bits;
    }

    public BaseIntegerType() {
        this.bits = 32;
    }

    @Override
    public int getSize() {
        return (this.bits + 7) / 8;
    }

    /**
     * 对齐标准与大小一致
     */
    @Override
    public int getAlign() {
        return this.getSize();
    }

    @Override
    public boolean isInteger() {
        return true;
    }

    public boolean isInt1() {
        return this.bits == 1;
    }

    public boolean isInt32() {
        return this.bits == 32;
    }

    @Override
    public String toString() {
        return "i" + this.bits;
    }

}
