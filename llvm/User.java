package llvm;

import llvm.type.LLVMType;

import java.util.ArrayList;

public class User extends Value {
    private ArrayList<Value> operands;

    public User(String name, LLVMType type) {
        super(name, type);
        operands = new ArrayList<>();
    }

    public User(LLVMType type) {
        super(type);
        operands = new ArrayList<>();
    }

    public void addOperand(Value operand) {
        operands.add(operand);
    }

    public Value getOperand(int index) {
        if (index < operands.size()) {
            return operands.get(index);
        }else {
            return null;
        }
    }

    public ArrayList<Value> getOperands() {
        return operands;
    }
}
