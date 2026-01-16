package llvm.instruction;

import llvm.UserClass.Instruction;
import llvm.Value;
import llvm.type.BaseIntegerType;
import llvm.type.PointerType;

public class Load extends Instruction {
    public Load(int nameCnt, Value pointer) {
        super("%v" + nameCnt, (BaseIntegerType) ((PointerType) pointer.getType()).getTargetType());
        addOperand(pointer);
    }

    public Value getPointer() {
        return getOperand(0);
    }

    @Override
    public String toString() {
        Value pointer = getOperand(0);
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" = load ");
        sb.append(getType());
        sb.append(", ");
        sb.append(pointer.getType());
        sb.append(" ");
        sb.append(pointer.getName());
        return sb.toString();

    }
}
