package optimize;

import llvm.LlvmModule;
import llvm.UserClass.BasicBlock;
import llvm.UserClass.Function;
import llvm.UserClass.Instruction;
import llvm.instruction.Branch;
import llvm.instruction.Call;
import llvm.instruction.Jump;

import java.util.ArrayList;
import java.util.HashSet;

public class preOptimize {

    // 删除死的basicblock与func
    public static void deleteDeadCode(LlvmModule module) {
        deleteDeadBBlock(module);   // 删除死的BBlock
        // 删除 死的函数
        deleteDeadFunc(module);
    }


    // 删除无法到达的函数
    public static void deleteDeadFunc(LlvmModule module) {
        ArrayList<Function> liveFuncs = new ArrayList<>();
        Function mainFunc = module.getMainFunc();
        findLiveFunc(mainFunc, liveFuncs);
        module.saveOnlyFunctions(liveFuncs);
    }

    public static void findLiveFunc(Function func, ArrayList<Function> liveFuncs) {
        if (!liveFuncs.contains(func)) {
            liveFuncs.add(func);
            HashSet<Function> callFuncs = new HashSet<>();
            for (BasicBlock bb : func.getBasicBlocks()) {
                for (Instruction inst : bb.getInstructions()) {
                    if (inst instanceof Call) {
                        Function callFunc = ((Call) inst).getFunction();
                        callFuncs.add(callFunc);
                    }
                }
            }
            for (Function f : callFuncs) {
                // TODO 是否需要特判如果调用的是库函数的话，忽略访问， 但是应该不需要
                findLiveFunc(f, liveFuncs);
            }
        }
    }

    public static void deleteDeadBBlock(LlvmModule module) {
        for (Function func : module.getFunctions()) {
            ArrayList<BasicBlock> liveBBlocks = new ArrayList<>();
            BasicBlock entryBlock = func.getBasicBlock(0);
            findLiveBBlock(entryBlock, liveBBlocks);   // entryBBlock是函数入口的BasicBlock
            func.saveOnlyBBlocks(liveBBlocks);    // 删除不是liveBBlocks中的BBlock
        }
    }

    public static void findLiveBBlock(BasicBlock bb, ArrayList<BasicBlock> liveBBlocks) {
        if (!liveBBlocks.contains(bb)) {
            liveBBlocks.add(bb);
            Instruction endInstr = bb.getLastInstr();
//            for(Instruction endInstr : bb.getInstructions())  // TODO 这么写好像有问题
            {
                if (endInstr instanceof Jump) {
                    BasicBlock targetBBlock = (BasicBlock) ((Jump) endInstr).getTargetBBlock();
                    findLiveBBlock(targetBBlock, liveBBlocks);
                } else if (endInstr instanceof Branch) {
                    BasicBlock trueBBlock = (BasicBlock) ((Branch) endInstr).getTrueBBlock();
                    findLiveBBlock(trueBBlock, liveBBlocks);
                    BasicBlock falseBBlock = (BasicBlock) ((Branch) endInstr).getFalseBBlock();
                    findLiveBBlock(falseBBlock, liveBBlocks);
                }
            }

        }
    }

    public static void analyzeFuncDom(LlvmModule module) {
        for (Function func : module.getFunctions()) {
            func.calcCFG();  // 分析程序的控制流图
            func.calcDomBys();  // 计算基础支配关系
            func.calcImmeDom();   // 计算基本块的直接支配块集合与被直接支配块集合
            func.calcDomFro();   // 计算分配边界
        }
    }
}
