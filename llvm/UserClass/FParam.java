package llvm.UserClass;

import llvm.Value;
import llvm.type.LLVMType;

public class FParam extends Value {
    public FParam(int index, LLVMType type) {
        super("%a" + index, type);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getType());
        sb.append(" ");
        sb.append(this.getName());
        return sb.toString();
    }
}
