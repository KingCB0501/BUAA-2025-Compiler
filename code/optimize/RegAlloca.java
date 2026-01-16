package optimize;

import backend.Reg;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import llvm.LlvmModule;
import llvm.UserClass.BasicBlock;
import llvm.UserClass.Function;
import llvm.UserClass.Instruction;
import llvm.Value;
import llvm.instruction.Alloca;
import llvm.instruction.Call;
import llvm.instruction.Phi;
import llvm.type.PointerType;

public class RegAlloca {

    private static RegAlloca instance;
    private LlvmModule module;

    private HashMap<Value, Double> value2citeNum;
    private HashMap<Reg, Value> reg2value;
    private HashMap<Value, Reg> value2reg;
    private ArrayList<Reg> freeRegs;


    private RegAlloca(LlvmModule module) {
        this.module = module;
    }

    public static RegAlloca getInstance(LlvmModule module) {
        if (instance == null) {
            instance = new RegAlloca(module);
        }
        return instance;
    }

    public void alloca() {
        analyzeActivateVar();
        for (Function func : module.getFunctions()) {
            allocReg4Func(func);
        }

    }

    /// 活跃变量分析
    /// out[-BB] = ⋃ in[-后继块]
    /// in[-BB] = useSet[-BB] ∪ (out[-BB] - defSet[-BB])
    public void analyzeActivateVar() {
        for (Function func : module.getFunctions()) {
            // 基本块的in与out集合
            HashMap<BasicBlock, HashSet<Value>> bbLiveInSets = new HashMap<>();
            HashMap<BasicBlock, HashSet<Value>> bbLiveOutSets = new HashMap<>();
            // 初始化，计算各个基本块的def-use集合，初始化各个基本块的in-out集合为空
            for (BasicBlock bb : func.getBasicBlocks()) {
                bb.computeDefUseSet();
                bbLiveInSets.put(bb, new HashSet<>());
                bbLiveOutSets.put(bb, new HashSet<>());
            }

            boolean flag = false;    // 活跃变量分析是否结束(in不再变化)
            while (!flag) {
                flag = true;
                // 控制流图逆序分析---收敛更快
                ArrayList<BasicBlock> bbs = func.getBasicBlocks();
                for (int i = bbs.size() - 1; i >= 0; i--) {
                    BasicBlock curBB = bbs.get(i);
                    HashSet<Value> outSet = new HashSet<>();
                    HashSet<Value> inSet = new HashSet<>();

                    // out[-BB] = ⋃ in[-后继块]
                    for (BasicBlock backBB : curBB.getBackBBlocks()) {
                        outSet.addAll(bbLiveInSets.get(backBB));
                    }

                    // in[-BB] = useSet[-BB] ∪ (out[-BB] - defSet[-BB])
                    inSet.addAll(outSet);
                    for (Value v : curBB.defSet) {
                        inSet.remove(v);
                    }
                    inSet.addAll(curBB.useSet);

                    if (!outSet.equals(bbLiveOutSets.get(curBB)) || !inSet.equals(bbLiveInSets.get(curBB))) {
                        flag = false;
                    }

                    bbLiveInSets.put(curBB, inSet);
                    bbLiveOutSets.put(curBB, outSet);
                    curBB.liveInSet = inSet;
                    curBB.liveOutSet = outSet;
                }
            }
        }
    }

    public void allocReg4Func(Function func) {
        value2citeNum = new HashMap<>();
        reg2value = new HashMap<>();
        value2reg = new HashMap<>();
        freeRegs = getCanAllocaRegs();
        ArrayList<Instruction> instrs = func.getAllInstrs();
        int size = instrs.size();

        // 计算引用权重，越到后面越大
        for (int i = 0; i < size; i++) {
            Instruction instr = instrs.get(i);
            if (!instr.getType().isVoid()) {
                Double doubleVal = value2citeNum.get(instr);
                if (doubleVal != null) {
                    double tmp = doubleVal + (size - i) * i + 1;
                    value2citeNum.put(instr, tmp);
                } else {
                    double tmp = (size - i) * i + 1;
                    value2citeNum.put(instr, tmp);
                }
            }
            for (Value v : instr.getOperands()) {
                Double doubleVal = value2citeNum.get(v);
                if (doubleVal != null) {
                    double tmp = doubleVal + (size - i) * i + 1.2;
                    value2citeNum.put(v, tmp);
                } else {
                    double tmp = (size - i) * i + 1.2;
                    value2citeNum.put(v, tmp);
                }
            }
        }

        BasicBlock entryBb = func.getFirstBasicBlock();
        allocReg4BBlock(entryBb);

        // 计算call调用前应该存活的寄存器
        for (BasicBlock bb : func.getBasicBlocks()) {
            ArrayList<Instruction> bbInstrs = bb.getInstructions();
            for (int i = 0; i < bbInstrs.size(); i++) {
                Instruction instr = bbInstrs.get(i);
                if (!(instr instanceof Call)) {
                    continue;
                }
                // out块中活跃的变量
                HashSet<Reg> activeRegs = new HashSet<>();
                for (Value v : bb.liveOutSet) {
                    if (value2reg.containsKey(v)) {
                        Reg r = value2reg.get(v);
                        activeRegs.add(r);
                    }
                }
                // 当前基本块后续指令依旧需要的寄存器中的值
                for (int j = i + 1; j < bbInstrs.size(); j++) {
                    Instruction instr2 = bbInstrs.get(j);
                    for (Value v : instr2.getOperands()) {
                        if (value2reg.containsKey(v)) {
                            Reg r = value2reg.get(v);
                            activeRegs.add(r);
                        }
                    }
                }
                ((Call) instr).liveRegSet = activeRegs;
            }
        }

        func.setValue2Reg(value2reg);
    }

    public void allocReg4BBlock(BasicBlock curBb) {
        HashSet<Value> noUsedSet = new HashSet<>();  // 不再使用的变量集合
        HashSet<Value> hasDefSet = new HashSet<>();
        ArrayList<Instruction> curInstrs = curBb.getInstructions();
        HashMap<Value, Instruction> value2finalUse = new HashMap<>();    // 记录value在当前基本块中的最终使用位置

        // 覆盖填写
        for (Instruction instr : curInstrs) {
            for (Value v : instr.getOperands()) {
                value2finalUse.put(v, instr);
            }
        }


        for (Instruction instr : curInstrs) {
            // 寄存器清理： 操作数寄存器与指令寄存器不冲突，可以直接提前清除
            if (!(instr instanceof Phi)) {
                for (Value v : instr.getOperands()) {
                    // 如果是该值的最后使用位置(并且不在outSet中活跃)，且占据了寄存器，可以直接清除
                    if (value2finalUse.get(v).equals(instr) && value2reg.containsKey(v) && !curBb.liveOutSet.contains(v)) {
                        Reg reg = value2reg.get(v);
                        reg2value.remove(reg);
                        noUsedSet.add(v);
                    }
                }
            }

            // 下面为instruction分配寄存器
            if (instr.getType().isVoid() || (instr instanceof Alloca alloca && ((PointerType) alloca.getType()).getTargetType().isArray())) {
                continue;     // 数组变量一定需要存储在栈上，不可分配寄存器
            }
            hasDefSet.add(instr);

            Reg res = null;
            for (Reg reg : freeRegs) {
                if (!reg2value.containsKey(reg)) {
                    res = reg;
                    break;
                }
            }
            if (res == null) {
                // 没有找到空闲寄存器，开始清除
                Double minCiteNum = Double.MAX_VALUE;
                for (Reg reg : freeRegs) {
                    Double tempCiteNum = value2citeNum.get(reg2value.get(reg));
                    if (tempCiteNum < minCiteNum) {
                        minCiteNum = tempCiteNum;
                        res = reg;
                    }
                }
                // instr重要性大于minCiteNum
                if (value2citeNum.get(instr) > minCiteNum) {
                    if (reg2value.containsKey(res)) {
                        Value resValue = reg2value.get(res);
                        value2reg.remove(resValue);
                    }
                    reg2value.put(res, instr);
                    value2reg.put(instr, res);
                }
            } else {
                reg2value.put(res, instr);
                value2reg.put(instr, res);
            }
        }

        // 临时清理(curBb被直接支配的块中不用的)寄存器，进行支配子块的寄存器分配
        for (BasicBlock bb : curBb.immeDomTos) {
            HashMap<Reg, Value> afterNoUse = new HashMap<>();
            for (Reg reg : reg2value.keySet()) {
                Value v = reg2value.get(reg);
                if (!bb.liveInSet.contains(v)) {
                    afterNoUse.put(reg, v);
                }
            }
            for (Reg reg : afterNoUse.keySet()) {
                reg2value.remove(reg);
            }
            // dfs
            allocReg4BBlock(bb);
            // 分配完直接支配子块之后，恢复
            for (Reg reg : afterNoUse.keySet()) {
                Value v = afterNoUse.get(reg);
                reg2value.put(reg, v);
            }
        }

        // 递归更新
        // 删除当前块中进行的寄存器分配
        for (Value v : hasDefSet) {
            if (value2reg.containsKey(v)) {
                Reg r = value2reg.get(v);
                reg2value.remove(r);
            }
        }

        // 恢复当前块删除的父块的变量
        for (Value v : noUsedSet) {
            if (hasDefSet.contains(v)) {
                continue;
            }
            if (!value2reg.containsKey(v)) {
                continue;
            }
            Reg r = value2reg.get(v);
            reg2value.put(r, v);
        }
    }

    private ArrayList<Reg> getCanAllocaRegs() {
        return new ArrayList<>() {{
            add(Reg.v1);
            add(Reg.gp);
            add(Reg.fp);
            add(Reg.t0);
            add(Reg.t1);
            add(Reg.t2);
            add(Reg.t3);
            add(Reg.t4);
            add(Reg.t5);
            add(Reg.t6);
            add(Reg.t7);
            add(Reg.t8);
            add(Reg.t9);
            add(Reg.s0);
            add(Reg.s1);
            add(Reg.s2);
            add(Reg.s3);
            add(Reg.s4);
            add(Reg.s5);
            add(Reg.s6);
            add(Reg.s7);
        }};
    }

}
