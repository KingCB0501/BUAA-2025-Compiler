package llvm.type;

/**
 * 数据类型
 */
public class LLVMType {
    // 简单理解，就是LLVM-IR中每一个标识符所对应的数据类型，数组类型，函数类型，指针类型，基础整数数据类型(i1, i32)
    // 参考C语言系统级编程中的对象七元组

    public static BaseIntegerType INT1 = new BaseIntegerType(1);   // 实在是用的太多了，每次new太麻烦了
    public static BaseIntegerType INT8 = new BaseIntegerType(8);
    public static BaseIntegerType INT32 = new BaseIntegerType(32);
    public static VoidType VOID = new VoidType();


    /**
     * 是否是指针数据
     */
    public boolean isPointer() {
        return false;
    }

    public boolean isArray() {
        return false;
    }

    public boolean isFunction() {
        return false;
    }

    public boolean isVoid() {
        return false;
    }

    /**
     * 是否是基础整数类型
     */
    public boolean isInteger() {
        return false;
    }

    /**
     * 返回该类型数据所需要的内存字节大小
     */
    public int getSize() {
        return 0;
    }

    public int getAlign() {
        return 0;
    }

    @Override
    public String toString() {
        return "LLVMType";
    }
}
