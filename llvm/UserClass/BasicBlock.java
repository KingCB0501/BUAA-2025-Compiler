package llvm.UserClass;

import llvm.Value;
import llvm.type.LabelType;

import java.util.ArrayList;

public class BasicBlock extends Value {
    private ArrayList<Instruction> instructions = new ArrayList<>();

    public BasicBlock(int namecnt) {
        super("%b" + namecnt, new LabelType());
    }

    public void addInstruction(Instruction ins) {
        if (ins == null) {
            System.err.println("Error: ins is null");
        }
        instructions.add(ins);
    }

    /**
     * 主要用作判断void函数最后一条instr是否为Ret
     *
     * @return
     */
    public Instruction getLastInstr() {
        if (instructions.isEmpty()) {
            return null;
        }
        return instructions.get(instructions.size() - 1);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName().substring(1));
        sb.append(":");
        sb.append("\n");
        for (Instruction i : instructions) {
            sb.append("    ");   // 缩进
            sb.append(i.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
