package llvm.UserClass;

import java.util.ArrayList;
import java.util.HashSet;
import llvm.Value;
import llvm.instruction.Branch;
import llvm.instruction.Jump;
import llvm.instruction.Phi;
import llvm.type.LabelType;

public class BasicBlock extends Value {
    private ArrayList<Instruction> instructions = new ArrayList<>();

    public BasicBlock(int namecnt) {
        super("%b" + namecnt, new LabelType());
    }

    public BasicBlock(String errorStage) {
        super("%b" + errorStage, new LabelType());
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

    public Instruction getFirstInstr() {
        if (instructions.isEmpty()) {
            return null;
        }
        return instructions.get(0);
    }

    public ArrayList<Instruction> getInstructions() {
        return instructions;
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


    /// --------- optimize---------
    private boolean isVisited = false;
    /// -----CFG-----
    private HashSet<BasicBlock> frontBBlocks = new HashSet<>();   // 前驱基本块
    private HashSet<BasicBlock> backBBlocks = new HashSet<>();    // 后继基本块

    /// -----支配关系-----
    public HashSet<BasicBlock> domBys = new HashSet<>();  // 基础支配关系(被支配)
    public HashSet<BasicBlock> immeDomTos = new HashSet<>();   // 该基本块直接支配的基本块集合
    public BasicBlock immeDomBy;    // 直接支配当前基本块的基本块
    public HashSet<BasicBlock> domFro = new HashSet<>();     // 支配边界

    private boolean isLived = true;

    public void setIsVisited() {
        isVisited = true;
    }

    public void resetIsVisited() {
        isVisited = false;
    }

    public boolean isVisited() {
        return isVisited;
    }

    public HashSet<BasicBlock> getFrontBBlocks() {
        return frontBBlocks;
    }

    public HashSet<BasicBlock> getBackBBlocks() {
        return backBBlocks;
    }

    public void addFrontBBlock(BasicBlock bb) {
        frontBBlocks.add(bb);
    }

    public void addBackBBlock(BasicBlock bb) {
        backBBlocks.add(bb);
    }

    public void clearFrontBBlocks() {
        frontBBlocks.clear();
    }

    public void clearBackBBlocks() {
        backBBlocks.clear();
    }

    public void findFrontAndBackBBlock() {
        if (this.isVisited()) {
            return;
        }
        this.setIsVisited();
        Instruction ins = this.getLastInstr();
        if (ins instanceof Jump) {
            BasicBlock targetBBlock = (BasicBlock) ((Jump) ins).getTargetBBlock();
            this.addBackBBlock(targetBBlock);
            targetBBlock.addFrontBBlock(this);
            targetBBlock.findFrontAndBackBBlock();
        } else if (ins instanceof Branch) {
            BasicBlock trueBBlock = (BasicBlock) ((Branch) ins).getTrueBBlock();
            BasicBlock falseBBlock = (BasicBlock) ((Branch) ins).getFalseBBlock();

            this.addBackBBlock(trueBBlock);
            trueBBlock.addFrontBBlock(this);
            trueBBlock.findFrontAndBackBBlock();

            this.addBackBBlock(falseBBlock);
            falseBBlock.addFrontBBlock(this);
            falseBBlock.findFrontAndBackBBlock();
        }
    }


    // 将所有的alloc指令放置到每个函数的entry块
    public void addInstr2Head(Instruction ins) {
        instructions.add(0, ins);
    }

    public void set2Dead() {
        this.isLived = false;
    }

    public boolean isLived() {
        return isLived;
    }

    /// ------Optimize    RegAlloca------
    public HashSet<Value> defSet = new HashSet<>();    // 先定义再使用
    public HashSet<Value> useSet = new HashSet<>();    // 先使用再定义
    public HashSet<Value> liveOutSet = new HashSet<>();    //基本块结尾处活跃变量
    public HashSet<Value> liveInSet = new HashSet<>();    // 基本块开始处活跃变量

    /**
     * 计算defSet与useSet
     */
    public void computeDefUseSet() {
        defSet = new HashSet<>();
        useSet = new HashSet<>();
        
        // 先单独处理所有Phi指令，因为Phi的操作数都来自前驱块
        for (Instruction instr : instructions) {
            if (instr instanceof Phi phi) {
                for (Value val : phi.getOperands()) {
                    if (isIFG(val)) {
                        useSet.add(val);
                    }
                }
            }
        }
        
        // 再处理所有指令（包括Phi）的def-use关系
        for (Instruction instr : instructions) {
            // 非Phi指令的操作数处理
            if (!(instr instanceof Phi)) {
                for (Value val : instr.getOperands()) {
                    if (isIFG(val) && !defSet.contains(val)) {
                        useSet.add(val);
                    }
                }
            }

            // defSet处理
            if (!instr.getType().isVoid() && !useSet.contains(instr)) {
                defSet.add(instr);
            }
        }
    }

    // TODO 理解

    /**
     * 变量可能的
     *
     * @param value
     * @return
     */
    private boolean isIFG(Value value) {
        return value instanceof Instruction || value instanceof FParam || value instanceof GlobalVar;
    }

}
