package llvm.instruction;

import llvm.UserClass.Instruction;
import llvm.Value;
import llvm.type.LLVMType;

public class Store extends Instruction {
    public Store(Value value, Value pointer) {
        super(LLVMType.VOID);
        addOperand(value);
        addOperand(pointer);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Value value = getOperand(0);
        Value pointer = getOperand(1);
        sb.append("store ");
        sb.append(value.getType());
        sb.append(" ");
        sb.append(value.getName());
        sb.append(", ");
        sb.append(pointer.getType());
        sb.append(" ");
        sb.append(pointer.getName());
        return sb.toString();
    }
}
