package llvm.type;

public class ArrayType extends LLVMType {
    private LLVMType elemType;   // 数组元素的基础数据类型
    private int length;

    public ArrayType(LLVMType elemType, int length) {
        this.elemType = elemType;    // 将普通类型升级成为基础数据类型
        this.length = length;
    }

    public ArrayType(int ElemBit, int length) {
        this.elemType = new BaseIntegerType(ElemBit);
        this.length = length;

    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public int getSize() {
        return this.length * this.elemType.getSize();
    }

    @Override
    public int getAlign() {
        return this.elemType.getAlign();
    }

    public int getLength() {
        return this.length;
    }

    public LLVMType getElementType() {
        return this.elemType;
    }

    @Override
    public String toString() {
        return "[" + this.length + " x " + this.elemType + "]";
    }
}
