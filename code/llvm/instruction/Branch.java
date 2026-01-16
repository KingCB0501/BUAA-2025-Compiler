package llvm.instruction;

import llvm.UserClass.BasicBlock;
import llvm.UserClass.Instruction;
import llvm.Value;
import llvm.type.LLVMType;

public class Branch extends Instruction {

    public Branch(Value ans, BasicBlock trueBranch, BasicBlock falseBranch) {
        super(LLVMType.VOID);
        this.addOperand(ans);
        this.addOperand(trueBranch);
        this.addOperand(falseBranch);
    }

    public Value getCond() {
        return getOperand(0);
    }

    public Value getTrueBBlock() {
        return getOperand(1);
    }

    public Value getFalseBBlock() {
        return getOperand(2);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("br i1 ");
        sb.append(getOperand(0).getName());
        sb.append(", label ");
        sb.append(getOperand(1).getName());
        sb.append(", label ");
        sb.append(getOperand(2).getName());
        return sb.toString();
    }
}