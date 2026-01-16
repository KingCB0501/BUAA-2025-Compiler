package optimize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Stack;

import llvm.Const.ConstantData;
import llvm.IRBuilder;
import llvm.LlvmModule;
import llvm.Use;
import llvm.User;
import llvm.UserClass.BasicBlock;
import llvm.UserClass.Function;
import llvm.UserClass.Instruction;
import llvm.Value;
import llvm.instruction.Alloca;
import llvm.instruction.Load;
import llvm.instruction.Phi;
import llvm.instruction.Store;
import llvm.type.LLVMType;
import llvm.type.PointerType;

public class Mem2Reg {
    // 每一个alloca对应一个变量
    private static Stack<Value> writeStack = new Stack<>();       // 变量的写入栈
    private static ArrayList<BasicBlock> readBbList = new ArrayList<>();    // 变量的读入基本块
    private static ArrayList<BasicBlock> writeBbList = new ArrayList<>();   // 变量的写入基本块
    private static ArrayList<Instruction> readInstrList = new ArrayList<>();   // 变量的读取指令  load
    private static ArrayList<Instruction> writeInstrList = new ArrayList<>();   // 变量的写入指令 store

    public static void work(LlvmModule module) {
        for (Function func : module.getFunctions()) {
            BasicBlock entry = func.getBasicBlock(0);
            ArrayList<Instruction> tempEntryInstrs = new ArrayList<>(entry.getInstructions());
            for (Instruction instr : tempEntryInstrs) {
                if (instr instanceof Alloca alloca) {
                    if (((PointerType) alloca.getType()).getTargetType().isInteger())
//                    if (!((PointerType) alloca.getType()).getTargetType().isArray())
                    {     // 不是数组变量，也不是指针变量(在参数)
                        writeStack.clear();
                        readBbList.clear();
                        writeBbList.clear();
                        readInstrList.clear();
                        writeInstrList.clear();
                        addPhi(alloca);
                        rename(alloca, entry);
                    }
                }
            }
        }
    }

    public static void addPhi(Alloca alloca) {
        LLVMType varType = ((PointerType) alloca.getType()).getTargetType();
        for (Use use : alloca.getUses()) {
            User user = use.getUser();    // 使用该alloca的地方， 一般只有store与load指令
            BasicBlock host = (BasicBlock) user.getHost();    // user指令所属的基本块
            if (!host.isLived()) {     // 如果该基本块已经消除，则不进行多余操作。因为即使删除了基本块，但是user-use关系没有删除
                continue;
            }
            if (user instanceof Store store) {
                if (!writeBbList.contains(host)) {
                    writeBbList.add(host);
                }
                writeInstrList.add(store);
            } else if (user instanceof Load load) {
                if (!readBbList.contains(host)) {
                    readBbList.add(host);
                }
                readInstrList.add(load);
            }
        }

        HashSet<BasicBlock> visited = new HashSet<>();
        ArrayList<BasicBlock> workBBList = new ArrayList<>(writeBbList);    // 对于alloca进行写入的基本块，进行了重定义，其支配边界需要进行phi

        while (!workBBList.isEmpty()) {
            BasicBlock curBb = workBBList.remove(0);
            for (BasicBlock dfBb : curBb.domFro) {
                if (!visited.contains(dfBb)) {
                    visited.add(dfBb);
                    if (!writeBbList.contains(dfBb)) {
//                        writeBbList.add(dfBb);       // 这边产生了问题
                        workBBList.add(dfBb);   // TODO
                    }
                    Phi phi = IRBuilder.makePhi(dfBb, varType);     // 写入块的支配边界意味着该变量可能有来自多重定义，插入phi指令

//                    if (!readBbList.contains(dfBb)) {
//                        readBbList.add(dfBb);    // 这边产生了问题
//                    }
                    readInstrList.add(phi);
                    writeInstrList.add(phi);
                }
            }
        }
    }

    public static void rename(Alloca alloca, BasicBlock curBb) {
        int counter = 0;      // 记录当前基本块对alloca对应的变量进行了几次写入，用于维护writeStack
        Iterator<Instruction> iter = curBb.getInstructions().iterator();
        while (iter.hasNext()) {
            Instruction instr = iter.next();
            if (instr.equals(alloca)) {
                iter.remove();
            } else if (instr instanceof Phi phi && writeInstrList.contains(phi)) {  // 是针对当前alloca变量的phi指令
                // phi指令是针对于该变量的新一个定义点
                writeStack.push(phi);    // writeStack栈顶的是alloca的最新定义
                counter = counter + 1;
            } else if (instr instanceof Store store && writeInstrList.contains(store)) {
                counter = counter + 1;
                writeStack.push(store.getValue4Store());
                store.dropAllReferences();
                iter.remove();
            } else if (instr instanceof Load load && readInstrList.contains(load)) {    // 是当前该变量的读取指令
                // 此处不应该从内存中load变量的值，直接取stack栈顶的即是该变量的最新定义值
                Value curValue = writeStack.empty() ? new ConstantData(LLVMType.INT32, 0, true) : writeStack.peek();
                // 如果写入栈为空，则读取内存默认值0，否则读取最新定义点(stack栈顶)

                // 删除前需要更新所有使用load的value与load使用的value的信息
                // 所有需要使用到load指令值的地方全部换成curValue
                load.replacAllUsesWith(curValue);
                // 删除load用到的value中存储的uses关系
                load.dropAllReferences();
                iter.remove();
            }
        }

        // 对于所有后继基本块中的该变量的phi指令，将来自于curBb的value填入phi中
        for (BasicBlock backBB : curBb.getBackBBlocks()) {
            // 遍历基本块中的所有phi指令（不再假设phi只在开头）
            for (Instruction instr : backBB.getInstructions()) {
                if (instr instanceof Phi phi && readInstrList.contains(phi)) {
                    Value valueFromCurBb = writeStack.empty() ? new ConstantData(LLVMType.INT32, 0, true) : writeStack.peek();
                    phi.fill(curBb, valueFromCurBb);
                }
            }
        }

        // 对于curBb的直接支配块，在alloca的变量使用上是一体的，直接支配块可以继承当前块的写入栈
        for (BasicBlock immeDomTo : curBb.immeDomTos) {
            rename(alloca, immeDomTo);
        }

        // 退出当前写入栈，避免对兄弟路径造成影响
        while (counter > 0) {
            counter--;
            writeStack.pop();
        }

    }
}
