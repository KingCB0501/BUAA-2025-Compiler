package llvm.instruction;


import llvm.UserClass.BasicBlock;
import llvm.UserClass.Instruction;
import llvm.Value;
import llvm.type.LLVMType;

//无条件跳转
public class Jump extends Instruction {

    /**
     * @param targetBlock 要跳转到的基本块
     */
    public Jump(BasicBlock targetBlock) {
        super(LLVMType.VOID);    // jump指令返回值为void
        addOperand(targetBlock);
    }

    public Value getTargetBBlock() {
        return getOperand(0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("br label ");
        sb.append(getOperand(0).getName());
        return sb.toString();
    }

}
