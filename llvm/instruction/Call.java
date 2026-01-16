package llvm.instruction;

import llvm.UserClass.Function;
import llvm.UserClass.Instruction;
import llvm.Value;
import llvm.type.BaseIntegerType;

import java.util.ArrayList;

public class Call extends Instruction {

    public Call(int nameCnt, Function func, ArrayList<Value> rParams) {
        super("%v" + nameCnt, (BaseIntegerType) func.getRetType());
        addOperand(func);
        if (rParams != null) {
            for (Value rParam : rParams) {
                addOperand(rParam);
            }
        }
    }

    // TODO 什么时候可以输出=
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!this.getType().isVoid()) {
            sb.append(getName());
            sb.append(" = ");
        }
        sb.append("call ");
        sb.append(getType());
        sb.append(" ");
        Function func = (Function) getOperand(0);
        sb.append(func.getName());
        sb.append("(");
        for (int i = 1; i < this.getOperands().size(); i++) {
            sb.append(getOperand(i).getType());
            sb.append(" ");
            sb.append(getOperand(i).getName());
            if (i < this.getOperands().size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
