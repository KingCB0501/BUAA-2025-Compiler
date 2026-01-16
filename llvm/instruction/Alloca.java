package llvm.instruction;

import llvm.Const.Constant;
import llvm.Const.ConstantArray;
import llvm.UserClass.Instruction;
import llvm.type.LLVMType;
import llvm.type.PointerType;

public class Alloca extends Instruction {
    private Constant init;

    // alloca指令的类型为分配区域的变量类型的指针类型
    public Alloca(int nameCnt, LLVMType type) {
        super("%v" + nameCnt, new PointerType(type));
        this.init = null;
    }

    public Alloca(int nameCnt, LLVMType type, Constant init) {
        super("%v" + nameCnt, new PointerType(type));
        this.init = init;
    }

    public Constant getInit() {
        return this.init;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" = alloca ");
        sb.append(((PointerType) getType()).getTargetType());
        return sb.toString();
    }
}
