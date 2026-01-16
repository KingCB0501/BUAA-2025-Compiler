package llvm.UserClass;

import backend.Reg;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import llvm.Value;
import llvm.type.BaseIntegerType;
import llvm.type.FuncIRType;
import llvm.type.LLVMType;

public class Function extends Value {

    private ArrayList<FParam> fParams = new ArrayList<>();
    private ArrayList<BasicBlock> basicBlocks = new ArrayList<>();

    public Function(String funcname, LLVMType retType, ArrayList<BaseIntegerType> fParamTypes) {
        super("@" + funcname, new FuncIRType((BaseIntegerType) retType, fParamTypes));
        this.fParams = new ArrayList<>();
        for (int i = 0; i < fParamTypes.size(); i++) {
            fParams.add(new FParam(i, fParamTypes.get(i)));
        }
    }

    public LLVMType getRetType() {
        return ((FuncIRType) this.getType()).getRetType();
    }

    public void addBasicBlock(BasicBlock b) {
        basicBlocks.add(b);
    }

    public void addBasicBlockBeforBb(BasicBlock beforeBb, BasicBlock newBb) {
        int index = basicBlocks.indexOf(beforeBb);
        basicBlocks.add(index, newBb);
    }

    public void moveBasicBlock2End(BasicBlock b) {
        if (basicBlocks.contains(b)) {
//            basicBlocks.remove(b);
//            basicBlocks.add(b);
        } else {
            System.err.println("Error: basicBlocks do not contain block" + b);
        }
    }

    public ArrayList<FParam> getFParams() {
        return fParams;
    }

    public ArrayList<BasicBlock> getBasicBlocks() {
        return basicBlocks;
    }

    public void saveOnlyBBlocks(ArrayList<BasicBlock> bBlocks) {
//        basicBlocks.removeIf(b -> !bBlocks.contains(b));
        Iterator<BasicBlock> iter = basicBlocks.iterator();
        while (iter.hasNext()) {
            BasicBlock b = iter.next();
            if (!bBlocks.contains(b)) {
                b.set2Dead();
                iter.remove();
            }
        }
    }

    public BasicBlock getBasicBlock(int index) {
        return basicBlocks.get(index);
    }

    public BasicBlock getFirstBasicBlock() {
        return basicBlocks.get(0);
    }

    public FParam getFParam(int index) {
        return fParams.get(index);
    }

    public ArrayList<Instruction> getAllInstrs() {
        ArrayList<Instruction> instrs = new ArrayList<>();
        for (BasicBlock b : basicBlocks) {
            instrs.addAll(b.getInstructions());
        }
        return instrs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("define dso_local ");
        sb.append(this.getType().toString());
        sb.append(" ");
        sb.append(this.getName());
        sb.append("(");
        for (int i = 0; i < fParams.size(); i++) {
            sb.append(fParams.get(i).toString());
            if (i < fParams.size() - 1) {
                sb.append(", ");
            }
        }
        sb.append(") {");
        sb.append("\n");
        for (BasicBlock b : basicBlocks) {
            sb.append(b.toString());
            sb.append("\n");
        }
        sb.append("}");
        sb.append("\n");
        return sb.toString();
    }


    ///  -----------Optimize-----------
    /**
     * 计算控制流图
     * BaiscBlock frontBasicBlocks与 backBasicBlocks
     */
    public void calcCFG() {
        // 对每个基本块进行DFS遍历，将从起始节点出发的所有路径走一遍
        BasicBlock entryBlock = this.getBasicBlock(0);
        for (BasicBlock bb : basicBlocks) {
            bb.resetIsVisited();
            // 清空前驱和后继，防止多次调用时累积旧数据
            bb.clearFrontBBlocks();
            bb.clearBackBBlocks();
        }
        entryBlock.findFrontAndBackBBlock();
    }

    /**
     * 计算基础支配关系
     * domBys
     */
    public void calcDomBys() {
        // 初始化入口BBlock的支配关系
        BasicBlock entryBlock = this.getBasicBlock(0);
        entryBlock.domBys = new HashSet<>() {{
            add(entryBlock);
        }};   // 入口处的初始domys仅有自身

        for (int i = 1; i < basicBlocks.size(); i++) {
            basicBlocks.get(i).domBys = new HashSet<>(basicBlocks);
        }

        boolean needRecur = true;
        while (needRecur) {
            needRecur = false;
            for (int i = 1; i < this.basicBlocks.size(); i++) {
                BasicBlock bb = this.basicBlocks.get(i);
                
                // 如果没有前驱块，则只被自己支配（不可达块）
                if (bb.getFrontBBlocks().isEmpty()) {
                    HashSet<BasicBlock> newDomBys = new HashSet<>();
                    newDomBys.add(bb);
                    if (!bb.domBys.equals(newDomBys)) {
                        needRecur = true;
                    }
                    bb.domBys = newDomBys;
                    continue;
                }
                
                HashSet<BasicBlock> newDomBys = new HashSet<>(basicBlocks);
                for (BasicBlock frontBB : bb.getFrontBBlocks()) { // 取交集
                    HashSet<BasicBlock> tempDomBys = new HashSet<>();
                    for (BasicBlock frontBBDomBy : frontBB.domBys) {
                        if (newDomBys.contains(frontBBDomBy)) {
                            tempDomBys.add(frontBBDomBy);
                        }
                    }
                    newDomBys = tempDomBys;
                }
                newDomBys.add(bb);
                if (!bb.domBys.equals(newDomBys)) {
                    needRecur = true;
                }
                bb.domBys = newDomBys;
            }
        }
    }

    /**
     * 计算直接支配：严格支配(基础支配去除自身)的节点中距离当前Block最近的一个
     */
    public void calcImmeDom() {
        // 先清空所有块的直接支配关系
        for (BasicBlock bb : basicBlocks) {
            bb.immeDomTos.clear();
            bb.immeDomBy = null;
        }
        
        for (int i = 1; i < this.basicBlocks.size(); i++) {
            // 跳过第一个基本块，其不被直接支配
            // TODO 不跳过好像也没有关系
            BasicBlock curBb = this.basicBlocks.get(i);
            // 计算直接支配bb的基本块

            // 遍历基础支配bb的基本块，判断其是否是距离最近的
            // 直接支配者（immediate dominator, idom）：严格支配n，且不严格支配任何严格支配 n 的节点的节点(直观理解就是所有严格支配n的节点中离n最近的那一个)，我们称其为n的直接支配者
            for (BasicBlock domBy : curBb.domBys) {
                if (domBy.equals(curBb)) {
                    continue;  // 不属于严格支配，从而不属于直接支配
                }

                boolean isImmeDom = true;
                for (BasicBlock otherDomBy : curBb.domBys) {
                    // 判断other是否被domby支配
                    if (otherDomBy.equals(domBy)) {
                        continue;
                    }

                    if (otherDomBy.equals(curBb)) {
                        continue;
                    }

                    if (otherDomBy.domBys.contains(domBy)) {   // domBy严格支配otherDomBy，所以肯定不是直接支配
                        isImmeDom = false;
                        break;
                    }
                }
                if (isImmeDom) {
                    curBb.immeDomBy = domBy;
                    domBy.immeDomTos.add(curBb);
                    break;
                }
            }
        }
    }

    /**
     * 计算支配边界
     */
    public void calcDomFro() {
        // 先清空所有块的支配边界
        for (BasicBlock bb : basicBlocks) {
            bb.domFro.clear();
        }
        
        for (BasicBlock bb : basicBlocks) {
            for (BasicBlock backBB : bb.getBackBBlocks()) {
                BasicBlock temp = bb;
                while (temp != null && (temp == backBB || !backBB.domBys.contains(temp))) {
                    temp.domFro.add(backBB);
                    temp = temp.immeDomBy;
                }
            }
        }
    }

    private HashMap<Value, Reg> value2Reg = new HashMap<>();

    public void setValue2Reg(HashMap<Value, Reg> value2Reg) {
        this.value2Reg = value2Reg;
    }

    public HashMap<Value, Reg> getValue2Reg() {
        return value2Reg;
    }
}
