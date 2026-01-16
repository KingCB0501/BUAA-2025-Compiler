package optimize;

import backend.Reg;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import llvm.IRBuilder;
import llvm.LlvmModule;
import llvm.UserClass.BasicBlock;
import llvm.UserClass.Function;
import llvm.UserClass.Instruction;
import llvm.Value;
import llvm.instruction.Branch;
import llvm.instruction.Copy;
import llvm.instruction.Jump;
import llvm.instruction.Phi;
import llvm.type.LLVMType;

public class RemovePhi {
    public static void removePhi(LlvmModule module) {
        for (Function func : module.getFunctions()) {
            ArrayList<BasicBlock> bbs = new ArrayList<>(func.getBasicBlocks());   // 必须构造新List，因为在后续函数中会修改List

            HashMap<Value, Reg> value2Reg = func.getValue2Reg();
            for (BasicBlock bb : bbs) {
                insertCopy2Bb(bb, value2Reg);
            }

        }
    }

    /**
     * 分析当前基本块中的所有Phi指令
     * 转换为Copy指令，并按前驱块分组
     * 删除原有的Phi指令
     * curBB输入：curBb（包含Phi指令的基本块）
     * 输出：HashMap<BasicBlock, ArrayList<Copy>> 前驱块 → 该前驱块应该插入的Copy指令列表
     */
    public static HashMap<BasicBlock, ArrayList<Copy>> tranPhi2Copy(BasicBlock curBB) {
        HashMap<BasicBlock, ArrayList<Copy>> frontBB2Copy = new HashMap<>();
        for (BasicBlock frontBB : curBB.getFrontBBlocks()) {
            frontBB2Copy.put(frontBB, new ArrayList<>());
        }

        Iterator<Instruction> iter = curBB.getInstructions().iterator();
        while (iter.hasNext()) {
            Instruction instr = iter.next();
            if (!(instr instanceof Phi phi)) {
                continue;
            }
            // 当前指令是Phi指令

            HashMap<BasicBlock, Value> frontBB2Value = phi.getFrontBB2Value();
            // 使用迭代器删除不可达的前驱块及其对应的值
            Iterator<Map.Entry<BasicBlock, Value>> phiIter = frontBB2Value.entrySet().iterator();
            while (phiIter.hasNext()) {
                Map.Entry<BasicBlock, Value> entry = phiIter.next();
                BasicBlock frontBB = entry.getKey();
                Value value = entry.getValue();
                // 如果该前驱块不在当前块的实际前驱列表中，则删除
                if (!frontBB2Copy.containsKey(frontBB)) {
                    phiIter.remove();
                    phi.removeOperand(value);
                    value.removeUse(phi);
                }
            }

            for (BasicBlock frontBB : frontBB2Value.keySet()) {
                Value value = frontBB2Value.get(frontBB);

                // 对于未填充的phi项(value为null)或占位使用的<frontBB, Value> 直接跳过
                if (value == null || value.isNoSense()) {
                    continue;
                }
                // 确保frontBB在frontBB2Copy中存在
                if (!frontBB2Copy.containsKey(frontBB)) {
                    continue;
                }
                Copy copy = new Copy(phi, value);    // phi = copy(value)
                copy.setHost(frontBB);
                frontBB2Copy.get(frontBB).add(copy);
            }
            iter.remove();
        }
        return frontBB2Copy;
    }


    /**
     * 将phi直接转换后的copy指令列表存在并行赋值问题
     * phi指令行为是并行赋值
     * copy列表式串行赋值
     * 需要在copy列表插入中间值消除冲突
     */
    public static ArrayList<Copy> solveParallelError(ArrayList<Copy> oldCopyList, BasicBlock curBB) {
        ArrayList<Copy> newCopyList = new ArrayList<>();
        for (int i = 0; i < oldCopyList.size(); i++) {
            Copy oldCopy = oldCopyList.get(i);   // 检查该指令的target是否是后续指令的from
            ArrayList<Copy> needReplace = new ArrayList<>();   // 后续冲突指令列表
            for (int j = i + 1; j < oldCopyList.size(); j++) {
                Copy backCopy = oldCopyList.get(j);
                if (backCopy.getFrom().equals(oldCopy.getTarget())) {
                    needReplace.add(backCopy);
                }
            }
            if (!needReplace.isEmpty()) {
                Value midValue = makeMidValue(oldCopy.getTarget().getType());
                Copy midCopy = new Copy(midValue, oldCopy.getTarget());   // 先将target的旧值存在midVal里面
                midCopy.setHost(curBB);
                newCopyList.add(0, midCopy);
                for (Copy copy : needReplace) {
                    copy.setFrom(midValue);
                }
            }

            newCopyList.add(oldCopy);
        }
        return newCopyList;
    }

    static int midValCnt = 0;

    public static Value makeMidValue(LLVMType type) {
        Value value = new Value("%m" + midValCnt, type);
        midValCnt++;
        return value;
    }

    public static ArrayList<Copy> solveRegError(ArrayList<Copy> oldCopyList, BasicBlock curBb, HashMap<Value, Reg> value2Reg) {
        ArrayList<Copy> newCopyList = new ArrayList<>();
        for (int i = 0; i < oldCopyList.size(); i++) {
            Copy oldCopy = oldCopyList.get(i);
            ArrayList<Copy> needReplace = new ArrayList<>();
            for (int j = i + 1; j < oldCopyList.size(); j++) {
                Copy backCopy = oldCopyList.get(j);
                if (value2Reg.containsKey(oldCopy.getTarget())
                        && value2Reg.containsKey(backCopy.getFrom())
                        && value2Reg.get(backCopy.getFrom()).equals(value2Reg.get(oldCopy.getTarget()))) {
                    needReplace.add(backCopy);
                }
            }
            if (!needReplace.isEmpty()) {
                Value midValue = makeMidValue(oldCopy.getTarget().getType());
                Copy midCopy = new Copy(midValue, oldCopy.getTarget());
                midCopy.setHost(curBb);
                newCopyList.add(0, midCopy);
                for (Copy copy : needReplace) {
                    copy.setFrom(midValue);
                }
            }
            newCopyList.add(oldCopy);
        }
        return newCopyList;
    }


    public static void insertCopy2Bb(BasicBlock curBb, HashMap<Value, Reg> value2Reg) {
        // 将curBb的Phi指令转换为Copy指令，按照前驱块分组
        HashMap<BasicBlock, ArrayList<Copy>> frontBB2Copy = tranPhi2Copy(curBb);

        // 复制前驱块列表，避免在循环中修改原集合导致ConcurrentModificationException
        ArrayList<BasicBlock> frontBBList = new ArrayList<>(curBb.getFrontBBlocks());
        for (BasicBlock frontBB : frontBBList) {
            ArrayList<Copy> copyList = frontBB2Copy.get(frontBB);
            if (copyList == null || copyList.isEmpty()) {
                continue;
            }
            // 解决Phi并行化赋值与copy序列行为不相符的问题
            ArrayList<Copy> paralleCopyList = solveParallelError(copyList, curBb);
            // TODO 真的会出现寄存器共用带来的问题吗？？？
            ArrayList<Copy> endCopyList = solveRegError(paralleCopyList, curBb, value2Reg);
            // 将copy指令列表插入到frontBB与curBb的路径中间

            // frontBB的后继只有curBb，则copy直接插入到frontBB后面去
            if (frontBB.getBackBBlocks().size() == 1) {
                for (Copy copy : endCopyList) {
                    copy.setHost(frontBB);  // 设置 copy 的 host
                    ArrayList<Instruction> instrs = frontBB.getInstructions();
                    int index = instrs.size() - 1;
                    instrs.add(index, copy);
                }
            } else {
                // 生成一个中间块：frontBB→中间块→curBb
                Function hostFunc = (Function) curBb.getHost();
                BasicBlock midBb = new BasicBlock(IRBuilder.namecnt++);
                midBb.setHost(hostFunc);
                hostFunc.addBasicBlockBeforBb(curBb, midBb);   // 插入新的基本块
                for (Copy copy : endCopyList) {
                    midBb.addInstruction(copy);
                    copy.setHost(midBb);
                }
                Jump jump = new Jump(curBb);   // 添加跳转指令
                jump.setHost(midBb);
                midBb.addInstruction(jump);    // 将跳转指令添加到中间块

                // 修改frontBB最后一条跳转指令(curBB为midBb)
                Branch branch = (Branch) frontBB.getLastInstr();
                branch.replaceOperand(curBb, midBb);

                // 更新CFG前驱后继关系
                // frontBB -> midBb -> curBb
                frontBB.getBackBBlocks().remove(curBb);
                frontBB.getBackBBlocks().add(midBb);
                midBb.addFrontBBlock(frontBB);
                midBb.addBackBBlock(curBb);
                curBb.getFrontBBlocks().remove(frontBB);
                curBb.getFrontBBlocks().add(midBb);
            }

        }
    }
}
