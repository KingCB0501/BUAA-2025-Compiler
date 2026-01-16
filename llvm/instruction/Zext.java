package llvm.instruction;

import llvm.UserClass.Instruction;
import llvm.Value;
import llvm.type.BaseIntegerType;

public class Zext extends Instruction {

    public Zext(int nameCnt, Value oldValue, BaseIntegerType targetType) {
        super("%v" + nameCnt, targetType);
        addOperand(oldValue);
    }

    public Value getOldValue() {
        return getOperand(0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" = zext ");
        sb.append(getOperand(0).getType());
        sb.append(" ");
        sb.append(getOperand(0).getName());
        sb.append(" to ");
        sb.append(getType());
        return sb.toString();
    }
}
