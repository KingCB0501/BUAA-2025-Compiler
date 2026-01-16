package llvm.instruction;

import llvm.UserClass.BasicBlock;
import llvm.UserClass.Instruction;
import llvm.Value;
import llvm.type.BaseIntegerType;
import llvm.type.LLVMType;

import java.util.ArrayList;
import java.util.HashMap;

public class Phi extends Instruction {

    //    private ArrayList<BasicBlock> frontBlocks;
    private HashMap<BasicBlock, Value> frontBB2Value = new HashMap<>();

    public Phi(int nameCnt, LLVMType type, ArrayList<BasicBlock> frontBlocks) {
        super("%p" + nameCnt, (BaseIntegerType) type);
        for (BasicBlock bb : frontBlocks) {
//            addOperand(bb);
            bb.addUse(this);
            frontBB2Value.put(bb, null);
        }
    }

    public void fill(BasicBlock frontBB, Value valueFromFrontBB) {
        addOperand(valueFromFrontBB);
        if (frontBB2Value.containsKey(frontBB)) {
            frontBB2Value.put(frontBB, valueFromFrontBB);
        } else {
            System.err.println("Phi fill failed for " + frontBB);
        }
    }

    public HashMap<BasicBlock, Value> getFrontBB2Value() {
        return frontBB2Value;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" = phi ");
        sb.append(getType());
        sb.append(" ");

        int count = 0;
        int size = frontBB2Value.size();
        for (BasicBlock bb : frontBB2Value.keySet()) {
            Value v = frontBB2Value.get(bb);
            sb.append("[ ");
            sb.append(v.getName());
            sb.append(", ");
            sb.append(bb.getName());
            sb.append(" ]");

            // 在每个元素之间添加逗号分隔（最后一个不加）
            if (count < size - 1) {
                sb.append(", ");
            }
            count++;
        }
        return sb.toString();
    }

    @Override
    public void replaceOperand(Value oldValue, Value newValue) {
        for (int i = 0; i < getOperands().size(); i++) {
            if (getOperands().get(i) == oldValue) {
                getOperands().set(i, newValue);
//                oldValue.removeUse(this);      // 目前先不删除，因为删除会影响replacAllUsesWith(Value newValue)的循环调用
                newValue.addUse(this);         // 将当前 User 添加到新值的使用列表
            }
        }

        for (BasicBlock bb : frontBB2Value.keySet()) {
            Value v = frontBB2Value.get(bb);
            if (v == oldValue) {
                frontBB2Value.put(bb, newValue);
            }
        }
    }
}
