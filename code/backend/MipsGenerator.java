package backend;

import frontend.Lexer.Token;
import java.util.ArrayList;
import java.util.HashMap;
import llvm.Const.ConstantData;
import llvm.LlvmModule;
import llvm.UserClass.BasicBlock;
import llvm.UserClass.FParam;
import llvm.UserClass.Function;
import llvm.UserClass.GlobalVar;
import llvm.UserClass.Instruction;
import llvm.UserClass.StringLiteral;
import llvm.Value;
import llvm.instruction.Alloca;
import llvm.instruction.Branch;
import llvm.instruction.Call;
import llvm.instruction.Compute;
import llvm.instruction.Copy;
import llvm.instruction.Getelemntptr;
import llvm.instruction.Icmp;
import llvm.instruction.Jump;
import llvm.instruction.Load;
import llvm.instruction.Ret;
import llvm.instruction.Store;
import llvm.instruction.Zext;
import llvm.type.LLVMType;
import llvm.type.PointerType;

public class MipsGenerator extends MipsBuilder {
    private static MipsGenerator instance;

    private MipsGenerator() {
    }

    public static MipsGenerator getInstance() {
        if (instance == null) {
            instance = new MipsGenerator();
        }
        return instance;
    }

    public MipsModule tranLlvmModule(LlvmModule llvmModule) {
        ArrayList<StringLiteral> stringLiterals = llvmModule.getStringLiterals();
        ArrayList<GlobalVar> globalVars = llvmModule.getGlobalVars();
        ArrayList<Function> functions = llvmModule.getFunctions();

        for (StringLiteral stringLiteral : stringLiterals) {
            tranStringLiteral(stringLiteral);
        }
        for (GlobalVar globalVar : globalVars) {
            tranGlobalVar(globalVar);
        }

        // 让.text段第一个函数是main函数，并且main函数通过syscall系统调用结束
        Function mainFunction = functions.get(functions.size() - 1);
        tranFunction(mainFunction);
        for (int i = 0; i < functions.size() - 1; i++) {
            tranFunction(functions.get(i));
        }

        return this.getMipsModule();
    }

    public void tranStringLiteral(StringLiteral stringLiteral) {
        String name = stringLiteral.getName().substring(1);
        String literal = stringLiteral.getOldStr();
        makeAsciizData(name, literal);
    }

    public void tranGlobalVar(GlobalVar globalVar) {
        // 全局变量一定有初始值
        // TODO 如果spaceData数据在Mars中全部自动清0的话，全0的全局变量可以用Spacedata申请
        // TODO 但是保险起见，先全部采用Word，但这可能存在大数组初始一堆的情况出现
        String name = globalVar.getName().substring(1);
        ArrayList<Integer> inits = globalVar.getInitNum();
        makeWordData(name, inits);
    }

    public void tranFunction(Function function) {
        curOffest = 0;    // 每一个函数刚开始都是一个新的栈帧
        value2offest = new HashMap<>();
        value2reg = function.getValue2Reg();
        curFunction = function;
        makeLabel(function.getName().substring(1));

        // 在当前函数的两个映射表中记录参数的存储位置
        ArrayList<FParam> fParams = function.getFParams();
        for (int i = 0; i < fParams.size(); i++) {
            FParam fParam = fParams.get(i);
            if (i < 4) {
                putReg(fParam, Reg.getArgReg(i));
            }
            subcurOffet(4);    // 参数不是int就是Int*
            putOffset(fParam, curOffest);
        }

        // 还是得先分配寄存器，不然由于指令块翻译顺序不同，可能在栈上和寄存器中都找不到变量
        for (Instruction instr : function.getAllInstrs()) {
            if (instr instanceof Copy copy) {
                // Copy 指令需要给 target 分配空间，而不是 Copy 指令本身
                Value target = copy.getTarget();
                if (findOffset4Value(target) == null && findReg4Value(target) == null) {
                    subcurOffet(4);
                    putOffset(target, curOffest);
                }
            } else if (!instr.getType().isVoid()) {
                if (findOffset4Value(instr) == null && findReg4Value(instr) == null) {
                    subcurOffet(4);
                    putOffset(instr, curOffest);
                }
            }
        }

        ArrayList<BasicBlock> basicBlocks = function.getBasicBlocks();
        for (BasicBlock basicBlock : basicBlocks) {
            tranBasicBlock(basicBlock);
        }

    }

    public void tranBasicBlock(BasicBlock basicBlock) {
        makeLabel(basicBlock.getName().substring(1));
        ArrayList<Instruction> instructions = basicBlock.getInstructions();
        for (Instruction instr : instructions) {
            tranInstruction(instr);
        }
    }

    public void tranInstruction(Instruction instr) {
        if (instr instanceof Copy) {
            tranCopy((Copy) instr);
        } else if (instr instanceof Alloca) {
            tranAlloca((Alloca) instr);
        } else if (instr instanceof Compute) {
            tranCompute((Compute) instr);
        } else if (instr instanceof Getelemntptr) {
            tranGetelemntptr((Getelemntptr) instr);
        } else if (instr instanceof Load) {
            tranLoad((Load) instr);
        } else if (instr instanceof Store) {
            tranStore((Store) instr);
        } else if (instr instanceof Icmp) {
            tranIcmp((Icmp) instr);
        } else if (instr instanceof Jump) {
            tranJump((Jump) instr);
        } else if (instr instanceof Branch) {
            tranBranch((Branch) instr);
        } else if (instr instanceof Zext) {
            tranZext((Zext) instr);
        } else if (instr instanceof Call) {
            tranCall(((Call) instr));
        } else if (instr instanceof Ret) {
            tranRet((Ret) instr);
        }
    }

    public void tranCopy(Copy copy) {
        Value from = copy.getFrom();
        Value target = copy.getTarget();
        Reg fromReg = findReg4Value(from);
        Reg targetReg = findReg4Value(target);

        if (targetReg == null) {
            targetReg = Reg.k0;
        }

        if (from instanceof ConstantData fromConstData) {
            makeLi(targetReg, fromConstData.getNum());
        } else if (fromReg != null) {
            makeMove(targetReg, fromReg);
        } else {
            makeLw(targetReg, findOffset4Value(from), Reg.sp);
        }
        Integer offest = findOffset4Value(target);
        if (findReg4Value(target) == null) {
            if (offest == null) {
                subcurOffet(4);
                putOffset(target, curOffest);
                offest = curOffest;
            }
            makeSw(targetReg, findOffset4Value(target), Reg.sp);
        }
    }


    public void tranAlloca(Alloca alloc) {
        LLVMType targetType = ((PointerType) alloc.getType()).getTargetType();    // 该alloc所分配的变量的类型，要在栈上为其申请内存区域
        int targetsize = targetType.getSize();     // 所需要大小
        subcurOffet(targetsize);    // 分配大小之后，还要将其地址($sp + curoffest)赋值给alloca变量
        if (findReg4Value(alloc) != null) {     // 如果alloca存在寄存器上面，则直接将该值赋值寄存器，但是好像不可能发生?
            // $reg = $sp + curoffest
            // TODO 其实addi $reg, $sp, imm可以用，但是imm是16位数据，可能无法足够表征 curOffest
            // TODO 16位应该足够了
            // 所以直接先将curOffest存在寄存器，然后寄存器与寄存器做加法
            Reg allocReg = findReg4Value(alloc);
            makeLi(allocReg, curOffest);
            makeAdd(allocReg, allocReg, Reg.sp);
        } else {     // 不在寄存器上面，就在栈上面
            makeLi(Reg.k0, curOffest);
            makeAdd(Reg.k0, Reg.k0, Reg.sp);
            if (findOffset4Value(alloc) == null) {
                // 栈上面也没有存，就申请一片空间存
                subcurOffet(4);
                putOffset(alloc, curOffest);
            }

            makeSw(Reg.k0, findOffset4Value(alloc), Reg.sp);
        }
    }

    public void tranCompute(Compute compute) {
        Token op_token = compute.getOpToken();
        Value left = compute.getLeftOperand();
        Value right = compute.getRightOperand();

        Reg leftReg = Reg.k0;
        Reg rightReg = Reg.k1;

        if (left instanceof ConstantData vleft) {
            makeLi(leftReg, vleft.getNum());
        } else if (findReg4Value(left) != null) {
            leftReg = findReg4Value(left);
        } else if (findOffset4Value(left) != null) {
            // 如果在偏移记录表中没有则一定出问题了
            makeLw(leftReg, findOffset4Value(left), Reg.sp);
        } else {
            System.err.println("Error: undefined operand");
        }

        if (right instanceof ConstantData vright) {
            makeLi(rightReg, vright.getNum());
        } else if (findReg4Value(right) != null) {
            rightReg = findReg4Value(right);
        } else if (findOffset4Value(right) != null) {
            makeLw(rightReg, findOffset4Value(right), Reg.sp);
        } else {
            System.err.println("Error: undefined operand");
        }

        if (findReg4Value(compute) != null) {
            makeCompute(compute.getOpToken(), findReg4Value(compute), leftReg, rightReg);
        } else {
            Integer offest = findOffset4Value(compute);
            if (offest == null) {
                subcurOffet(4);
                putOffset(compute, curOffest);
                offest = curOffest;
            }
            makeCompute(compute.getOpToken(), Reg.k0, leftReg, rightReg);
            makeSw(Reg.k0, offest, Reg.sp);
        }
    }


    public void tranGetelemntptr(Getelemntptr getelemntptr) {
        Value baseValue = getelemntptr.getBaseValue();
        Value offestValue = getelemntptr.getOffestValue();
        Reg baseReg = Reg.k0;
        Reg offestReg = Reg.k1;

        if (baseValue instanceof GlobalVar || baseValue instanceof StringLiteral) {
            makeLa(baseReg, baseValue.getName().substring(1));
        } else if (findReg4Value(baseValue) != null) {    // TODO 这个移动到前面去
            baseReg = findReg4Value(baseValue);
        } else {
            makeLw(baseReg, findOffset4Value(baseValue), Reg.sp);
        }

        if (offestValue instanceof ConstantData voff) {
            makeLi(offestReg, voff.getNum() * 4);    // TODO 这里可能是1吗
        } else if (findReg4Value(offestValue) != null) {
            // 不能直接在原寄存器上做 sll，会修改原值
            // 使用临时寄存器 $k1 来存储乘4后的结果
            Reg srcReg = findReg4Value(offestValue);
            makeSll(offestReg, srcReg, 2);    // offestReg($k1) = srcReg * 4
        } else {
            Integer offest = findOffset4Value(offestValue);   // 一定能找到
            makeLw(offestReg, offest, Reg.sp);
            makeSll(offestReg, offestReg, 2);    // 这里 offestReg 是临时的，可以修改
        }

        if (findReg4Value(getelemntptr) != null) {
            makeAdd(findReg4Value(getelemntptr), baseReg, offestReg);
            // 同时存到栈上，保证后续 Call 传参时能从栈上取到正确的值
            Integer offest = findOffset4Value(getelemntptr);
            if (offest != null) {
                makeSw(findReg4Value(getelemntptr), offest, Reg.sp);
            }
        } else {
            Integer offest = findOffset4Value(getelemntptr);
            if (offest == null) {
                subcurOffet(4);
                putOffset(getelemntptr, curOffest);
                offest = curOffest;
            }
            makeAdd(Reg.k0, baseReg, offestReg);
            makeSw(Reg.k0, offest, Reg.sp);
        }

    }

    public void tranLoad(Load load) {
        Value pointer = load.getPointer();
        Reg pointerReg = Reg.k0;

        if (pointer instanceof GlobalVar || pointer instanceof StringLiteral) {
            makeLa(pointerReg, pointer.getName().substring(1));
        } else if (findReg4Value(pointer) != null) {
            pointerReg = findReg4Value(pointer);
        } else {
            if (findOffset4Value(pointer) == null) {
                System.out.println(load);
            }
            makeLw(pointerReg, findOffset4Value(pointer), Reg.sp);
        }

        if (findReg4Value(load) != null) {
            makeLw(findReg4Value(load), 0, pointerReg);
        } else {
            Integer offest = findOffset4Value(load);
            if (offest == null) {
                subcurOffet(4);
                putOffset(load, curOffest);
                offest = curOffest;
            }
            makeLw(Reg.k0, 0, pointerReg);
//            System.out.println(offest);
            makeSw(Reg.k0, offest, Reg.sp);
        }

    }

    public void tranStore(Store store) {
        Value value = store.getValue();
        Value Pointer = store.getPointer();
        Reg valueReg = Reg.k0;
        Reg pointerReg = Reg.k1;
        if (Pointer instanceof GlobalVar || Pointer instanceof StringLiteral) {
            makeLa(pointerReg, Pointer.getName().substring(1));
        } else if (findReg4Value(Pointer) != null) {
            pointerReg = findReg4Value(Pointer);
        } else {
            makeLw(pointerReg, findOffset4Value(Pointer), Reg.sp);
        }

        if (value instanceof ConstantData vNum) {
            makeLi(valueReg, vNum.getNum());
        } else if (findReg4Value(value) != null) {
            valueReg = findReg4Value(value);
        } else {  // 一定能找到
            makeLw(valueReg, findOffset4Value(value), Reg.sp);
        }

        // store的值一定不会对应寄存器
        makeSw(valueReg, 0, pointerReg);
    }

    public void tranIcmp(Icmp icmp) {
        Icmp.OpType op = icmp.getOp();
        Value leftop = icmp.getLeftOperand();
        Value rightop = icmp.getRightOperand();
        Reg leftReg = Reg.k0;
        Reg rightReg = Reg.k1;

        if (leftop instanceof ConstantData leftopNum) {
            makeLi(leftReg, leftopNum.getNum());
        } else if (findReg4Value(leftop) != null) {
            leftReg = findReg4Value(leftop);
        } else {
            makeLw(leftReg, findOffset4Value(leftop), Reg.sp);
        }

        if (rightop instanceof ConstantData rightopNum) {
            makeLi(rightReg, rightopNum.getNum());
        } else if (findReg4Value(rightop) != null) {
            rightReg = findReg4Value(rightop);
        } else {
            makeLw(rightReg, findOffset4Value(rightop), Reg.sp);
        }

        if (findReg4Value(icmp) != null) {
            makeCompare(icmp.getOp(), findReg4Value(icmp), leftReg, rightReg);
        } else {
            Integer offest = findOffset4Value(icmp);
            if (offest == null) {    // 虽然大小是1字节，但是方便起见我们还是用4字节存储
                subcurOffet(4);
                putOffset(icmp, curOffest);
                offest = curOffest;
            }
            makeCompare(icmp.getOp(), Reg.k0, leftReg, rightReg);
            makeSw(Reg.k0, offest, Reg.sp);
        }
    }

    public void tranJump(Jump jump) {
        String label = jump.getTargetBBlock().getName().substring(1);
        makeJ(label);
    }

    public void tranBranch(Branch branch) {
        // 转换逻辑，先判断cond是否不等于$zero, 不等于时候转移到trueBBlock, bne下一条指令无条件跳转到false

        Value cond = branch.getCond();
        Value trueBBlock = branch.getTrueBBlock();
        Value falseBBlock = branch.getFalseBBlock();
        Reg condReg = Reg.k0;
        if (cond instanceof ConstantData condConst) {
            if (condConst.getNum() != 0) {
                makeJ(trueBBlock.getName().substring(1));
            } else {
                makeJ(falseBBlock.getName().substring(1));
            }
            return;
        }
        if (findReg4Value(cond) != null) {
            condReg = findReg4Value(cond);
        } else {
            makeLw(condReg, findOffset4Value(cond), Reg.sp);
        }
        makeBne(condReg, Reg.zero, trueBBlock.getName().substring(1));
        makeJ(falseBBlock.getName().substring(1));
    }

    public void tranZext(Zext zext) {
        Value oldValue = zext.getOldValue();
        Reg oldReg = Reg.k0;
        if (oldValue instanceof ConstantData oldValNum) {
            makeLi(oldReg, oldValNum.getNum());
        } else if (findReg4Value(oldValue) != null) {
            oldReg = findReg4Value(oldValue);
        } else {
            makeLw(oldReg, findOffset4Value(oldValue), Reg.sp);
        }

        if (findReg4Value(zext) != null) {
            makeAdd(findReg4Value(zext), oldReg, Reg.zero);
        } else {
            Integer offest = findOffset4Value(zext);
            if (offest == null) {
                subcurOffet(4);
                putOffset(zext, curOffest);
                offest = curOffest;
            }
            makeSw(oldReg, offest, Reg.sp);
        }


    }

    public void tranCall(Call call) {
        Function function = call.getFunction();
        ArrayList<Value> rParams = call.getParameters();
        String funcName = function.getName().substring(1);
        if (funcName.equals("getint") || funcName.equals("putint") || funcName.equals("putch") || funcName.equals("putstr")) {
            tranSysFuncCall(call);
            return;
        }

        // 使用 call 指令的 liveRegSet 与参数寄存器进行保存
        ArrayList<Reg> regsToSave = new ArrayList<>(call.liveRegSet);
        // 添加参数寄存器 $a0-$a3
        for (int i = 0; i < 4; i++) {
            Reg argReg = Reg.getArgReg(i);
            if (!regsToSave.contains(argReg)) {
                regsToSave.add(argReg);
            }
        }

        int offset = 0;
        for (int i = 0; i < regsToSave.size(); i++) {
            offset = offset - 4;     // 保存用到的寄存器
            makeSw(regsToSave.get(i), offset + curOffest, Reg.sp);
        }
        offset = offset - 4;
        makeSw(Reg.ra, offset + curOffest, Reg.sp);    // 保存$ra寄存器
        makeAdd(Reg.k1, Reg.sp, Reg.zero);
        makeLi(Reg.k0, curOffest + offset);    // 在栈顶保存栈偏移
        makeAdd(Reg.sp, Reg.sp, Reg.k0);       // 更新栈指针
        for (int i = 0; i < rParams.size(); i++) {
            Value arg = rParams.get(i);
            if (i < 4) {
                Reg argReg = Reg.getArgReg(i);
                if (arg instanceof ConstantData argNum) {
                    makeLi(argReg, argNum.getNum());
                } else {
                    Reg fromReg = findReg4Value(arg);
                    if (fromReg != null) {
                        int regIndex = fromReg.getIndex();
                        // 如果参数在 a0-a3 中，且该寄存器已经被前面的参数覆盖
                        if (regIndex >= 4 && regIndex <= 7 && regIndex < i + 4) {
                            // 从栈上保存的位置读取
                            int savedOffset = regsToSave.indexOf(fromReg);
                            if (savedOffset != -1) {
                                makeLw(argReg, -(savedOffset + 1) * 4, Reg.k1);
                            } else {
                                makeLw(argReg, findOffset4Value(arg), Reg.k1);
                            }
                        } else if (fromReg == argReg) {
                            // 源和目标相同，不需要操作
                        } else {
                            // 直接从寄存器移动
                            makeMove(argReg, fromReg);
                        }
                    } else {
                        makeLw(argReg, findOffset4Value(arg), Reg.k1);
                    }
                }
            } else {
                if (arg instanceof ConstantData argNum) {
                    makeLi(Reg.k0, argNum.getNum());
                } else {
                    Reg fromReg = findReg4Value(arg);
                    if (fromReg != null) {
                        int regIndex = fromReg.getIndex();
                        // 如果参数在 a0-a3 中，需要从栈上读取保存的值
                        if (regIndex >= 4 && regIndex <= 7) {
                            int savedOffset = regsToSave.indexOf(fromReg);
                            if (savedOffset != -1) {
                                makeLw(Reg.k0, -(savedOffset + 1) * 4, Reg.k1);
                            } else {
                                makeLw(Reg.k0, findOffset4Value(arg), Reg.k1);
                            }
                        } else {
                            makeMove(Reg.k0, fromReg);
                        }
                    } else {
                        makeLw(Reg.k0, findOffset4Value(arg), Reg.k1);
                    }
                }
                makeSw(Reg.k0, (i + 1) * 4 * (-1), Reg.sp);
            }
        }

        makeJal(funcName);

        makeLi(Reg.k0, (curOffest + offset) * (-1));
        makeAdd(Reg.sp, Reg.sp, Reg.k0);
        makeLw(Reg.ra, offset + curOffest, Reg.sp);
        offset = 0;
        for (int i = 0; i < regsToSave.size(); i++) {
            offset = offset - 4;
            makeLw(regsToSave.get(i), offset + curOffest, Reg.sp);
        }

        if (!call.getFunction().getRetType().isVoid()) {
            if (findReg4Value(call) != null) {
                makeAdd(findReg4Value(call), Reg.v0, Reg.zero);
            } else {
                Integer offest = findOffset4Value(call);
                if (offest == null) {
                    subcurOffet(4);
                    putOffset(call, curOffest);
                    offest = curOffest;
                }
                makeSw(Reg.v0, offest, Reg.sp);
            }
        }

    }

    public void tranSysFuncCall(Call call) {
        String funcName = call.getFunction().getName().substring(1);

        if (funcName.equals("getint")) {
            // getint系统调用号为5，返回值存放在$v0中
            makeLi(Reg.v0, 5);
            makeSyscall();
            if (findReg4Value(call) != null) {
                makeAdd(findReg4Value(call), Reg.v0, Reg.zero);
            } else {
                Integer offest = findOffset4Value(call);
                if (offest == null) {
                    subcurOffet(4);
                    putOffset(call, curOffest);
                    offest = curOffest;
                }
                makeSw(Reg.v0, offest, Reg.sp);
            }
            return;
        }

        if (funcName.equals("putint")) {
            makeSw(Reg.a0, curOffest - 4, Reg.sp);
            makeLi(Reg.v0, 1);
            Value value = call.getParameters().get(0);
            if (value instanceof ConstantData valNum) {
                makeLi(Reg.a0, valNum.getNum());
            } else if (findReg4Value(value) != null) {
                makeAdd(Reg.a0, findReg4Value(value), Reg.zero);
            } else {
                makeLw(Reg.a0, findOffset4Value(value), Reg.sp);
            }
            makeSyscall();
            makeLw(Reg.a0, curOffest - 4, Reg.sp);
            return;
        }

        if (funcName.equals("putch")) {
            makeSw(Reg.a0, curOffest - 4, Reg.sp);
            makeLi(Reg.v0, 11);
            Value value = call.getParameters().get(0);
            if (value instanceof ConstantData valNum) {
                makeLi(Reg.a0, valNum.getNum());
            } else if (findReg4Value(value) != null) {
                makeAdd(Reg.a0, findReg4Value(value), Reg.zero);
            } else {
                makeLw(Reg.a0, findOffset4Value(value), Reg.sp);
            }
            makeSyscall();
            makeLw(Reg.a0, curOffest - 4, Reg.sp);
            return;
        }

        if (funcName.equals("putstr")) {
            makeSw(Reg.a0, curOffest - 4, Reg.sp);
            makeLi(Reg.v0, 4);
            Value value = call.getParameters().get(0);
            if (value instanceof StringLiteral) {
                makeLa(Reg.a0, value.getName().substring(1));
            } else if (findReg4Value(value) != null) {
                makeAdd(Reg.a0, findReg4Value(value), Reg.zero);
            } else {
                makeLw(Reg.a0, findOffset4Value(value), Reg.sp);
            }
            makeSyscall();

            makeLw(Reg.a0, curOffest - 4, Reg.sp);

            return;
        }
    }

    public void tranRet(Ret ret) {
        if (curFunction.getName().substring(1).equals("main")) {
            makeLi(Reg.v0, 10);
            makeSyscall();
        } else {
            if (!curFunction.getRetType().isVoid()) {
                Value retValue = ret.getRetValue();
                if (retValue instanceof ConstantData valNum) {
                    makeLi(Reg.v0, valNum.getNum());
                } else if (findReg4Value(retValue) != null) {
                    makeAdd(Reg.v0, findReg4Value(retValue), Reg.zero);
                } else {
                    makeLw(Reg.v0, findOffset4Value(retValue), Reg.sp);
                }
            }
            makeJr(Reg.ra);
        }
    }


    /// /////////////////////////////////////////////////////////////////

    public Reg findReg4Value(Value value) {
        if (this.value2reg.containsKey(value)) {
            return this.value2reg.get(value);
        }
        return null;
    }

    public Integer findOffset4Value(Value value) {
        if (this.value2offest.containsKey(value)) {
            return this.value2offest.get(value);
        }
        return null;
    }

    public void putReg(Value value, Reg reg) {
        value2reg.put(value, reg);
    }

    public void putOffset(Value value, int offset) {
        value2offest.put(value, offset);
    }

}


































