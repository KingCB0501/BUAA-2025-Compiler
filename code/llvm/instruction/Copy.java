package llvm.instruction;

import llvm.UserClass.Instruction;
import llvm.Value;
import llvm.type.VoidType;

public class Copy extends Instruction {

    // TODO 需要设置指令类型吗
    public Copy(Value target, Value from) {
        super(new VoidType());
        addOperand(target);
        addOperand(from);
    }

    public Value getTarget() {
        return getOperand(0);
    }

    public Value getFrom() {
        return getOperand(1);
    }

    public void setFrom(Value from) {
        Value oldFrom = getOperand(1);
        replaceOperand(oldFrom, from);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("copy ");
        sb.append(getTarget().getName());
        sb.append(", ");
        sb.append(getFrom().getName());
        return sb.toString();
    }

}
