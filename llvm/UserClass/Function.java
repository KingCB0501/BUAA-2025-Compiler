package llvm.UserClass;

import llvm.Value;
import llvm.type.BaseIntegerType;
import llvm.type.FuncIRType;
import llvm.type.LLVMType;

import java.util.ArrayList;

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

    public BasicBlock getBasicBlock(int index) {
        return basicBlocks.get(index);
    }

    public FParam getFParam(int index) {
        return fParams.get(index);
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
}
