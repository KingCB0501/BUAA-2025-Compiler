package llvm.UserClass;

import llvm.User;
import llvm.type.BaseIntegerType;

public class Instruction extends User {
    public Instruction(String name, BaseIntegerType type) {
        super(name, type);
    }

    // 每一条指令表达式的类型 不是int_1, int_2, 就是指针类型, 或者store表达式的返回值可以看做void

    public Instruction(BaseIntegerType type) {
        super(type);
    }
}
