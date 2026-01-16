package llvm.instruction;

import llvm.UserClass.Instruction;
import llvm.Value;
import llvm.type.BaseIntegerType;
import llvm.type.LLVMType;

public class Ret extends Instruction {

    public Ret() {
        super(LLVMType.VOID);   // ret void
    }

    public Ret(Value value) {
        super((BaseIntegerType) value.getType());
        addOperand(value);
    }

    @Override
    public String toString() {
        if (this.getOperands().isEmpty()) {
            return "ret void";
        } else {
            Value retValue = getOperands().get(0);
            StringBuilder sb = new StringBuilder();
            sb.append("ret ");
            sb.append(retValue.getType());
            sb.append(" ");
            sb.append(retValue.getName());
            return sb.toString();
        }
    }
}
