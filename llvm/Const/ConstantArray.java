package llvm.Const;

import llvm.Value;
import llvm.type.ArrayType;
import llvm.type.LLVMType;

import java.util.ArrayList;

/**
 * 主要用作赋值的常量数组
 */
public class ConstantArray extends Value implements Constant {
    private ArrayList<Integer> arrayData;
    private boolean isZero;

    public ConstantArray(LLVMType type, ArrayList<Integer> arrayData) {
        super(type);
        this.arrayData = arrayData;
        int subLength = ((ArrayType) type).getLength() - arrayData.size();
        for (int i = 0; i < subLength; i++) {
            this.arrayData.add(0);
        }

        isZero = true;
        for (int i = 0; i < arrayData.size(); i++) {
            if (arrayData.get(i) != 0) {
                isZero = false;
                break;
            }
        }
    }

    public ConstantArray(LLVMType type) {
        super(type);
        this.arrayData = new ArrayList<>();
        for (int i = 0; i < ((ArrayType) type).getLength(); i++) {
            this.arrayData.add(0);
        }
        isZero = true;
    }

    public ArrayList<Integer> getArrayData() {
        return arrayData;
    }

    public int getLength() {
        return arrayData.size();
    }

    public ConstantData getElement(int index) {
        if (index < arrayData.size()) {
            return new ConstantData(((ArrayType) getType()).getElementType(), arrayData.get(index));
        }

        System.err.println("出错啦" + this + "in ConstantArray");
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        LLVMType elemType = ((ArrayType) getType()).getElementType();
        sb.append("[");
        for (int i = 0; i < arrayData.size(); i++) {
            sb.append(elemType);
            sb.append(" ");
            sb.append(arrayData.get(i));
            if (i != arrayData.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public boolean iszero() {
        return isZero;
    }

}
