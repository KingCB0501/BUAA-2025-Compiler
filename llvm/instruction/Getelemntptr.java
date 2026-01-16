package llvm.instruction;

import llvm.UserClass.Instruction;
import llvm.Value;
import llvm.type.ArrayType;
import llvm.type.BaseIntegerType;
import llvm.type.LLVMType;
import llvm.type.PointerType;

/**
 * 对于数组a[5]获取a[3]的地址有如下两种方法
 * ; 方法一
 * %1 = getelementptr [5 x i32], [5 x i32]* @a, i32 0, i32 3
 * <p>
 * ; 方法二
 * %2 = getelementptr [5 x i32], [5 x i32]* @a, i32 0, i32 0
 * %3 = getelementptr i32, i32* %2, i32 3
 * <p>
 * 这里我们只采用方法一
 */


public class Getelemntptr extends Instruction {
    private LLVMType base_type;


    /**
     * %1 = getelementptr [5 x i32], [5 x i32]* @a, i32 0, i32 3
     * %v_namecnt = getelementptr base_type, base_type* base_value, left_value, right_value
     */
    public Getelemntptr(int nameCnt, Value base_value, Value left_value, Value right_value) {
        super("%v" + nameCnt, new PointerType(getPointerElemType(base_value)));
        this.base_type = ((PointerType) (base_value.getType())).getTargetType();
        addOperand(base_value);
        addOperand(left_value);
        addOperand(right_value);
    }

    public Getelemntptr(int nameCnt, Value base_value, Value offest) {
        super("%v" + nameCnt, (BaseIntegerType) base_value.getType());
        this.base_type = ((PointerType) (base_value.getType())).getTargetType();
        addOperand(base_value);
        addOperand(offest);
    }

    // getelemntptr指令类型是数组元素指针类型


    private static LLVMType getPointerElemType(Value base_value) {
        // 先取指针指向的目标数组类型，最后取出其元素类型
        LLVMType type = base_value.getType();
        if (type instanceof PointerType) {
            LLVMType targetType = ((PointerType) type).getTargetType();
            if (targetType instanceof ArrayType) {
                LLVMType elementType = ((ArrayType) targetType).getElementType();
                return elementType;
            }
        }
        System.err.println("出错 in getPointerElemType");
        return null;
    }

    @Override
    // %1 = getelementptr [5 x i32], [5 x i32]* @a, i32 0, i32 3
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" = getelementptr inbounds ");
        sb.append(base_type);
        sb.append(", ");

        Value base_value = getOperand(0);
        sb.append(base_value.getType());
        sb.append(" ");
        sb.append(base_value.getName());
        sb.append(", ");
        Value left_value = getOperand(1);
        sb.append(left_value.getType());
        sb.append(" ");
        sb.append(left_value.getName());
        Value right_value = getOperand(2);
        if (right_value != null) {
            sb.append(", ");
            sb.append(right_value.getType());
            sb.append(" ");
            sb.append(right_value.getName());
        }
        return sb.toString();
    }


}
