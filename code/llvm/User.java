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
        operand.addUse(this);   //添加使用关系
    }

    public void removeOperand(Value operand) {
        operands.remove(operand);
    }

    public Value getOperand(int index) {
        if (index < operands.size()) {
            return operands.get(index);
        } else {
            return null;
        }
    }

    public ArrayList<Value> getOperands() {
        return operands;
    }

    public void replaceOperand(Value oldValue, Value newValue) {

        for (int i = 0; i < operands.size(); i++) {
            if (operands.get(i) == oldValue) {
                operands.set(i, newValue);
//                oldValue.removeUse(this);      // 目前先不删除，因为删除会影响replacAllUsesWith(Value newValue)的循环调用
                newValue.addUse(this);         // 将当前 User 添加到新值的使用列表
            }
        }
    }

    // 删除该user与所有操作数之间的引用关系
    public void dropAllReferences() {
        for (Value operand : operands) {
            operand.removeUse(this);
        }
    }
}
