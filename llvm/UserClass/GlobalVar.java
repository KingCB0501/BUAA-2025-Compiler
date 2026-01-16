package llvm.UserClass;

import llvm.Const.Constant;
import llvm.Const.ConstantArray;
import llvm.Const.ConstantData;
import llvm.User;
import llvm.type.ArrayType;
import llvm.type.LLVMType;
import llvm.type.PointerType;

import java.util.ArrayList;

public class GlobalVar extends User {
    private Constant init;
    private boolean isConst;

    // 该构造方法对应由静态变量升级的全局变量，如有需要，可以加internal
    public GlobalVar(int globvar_cnt, LLVMType type, Constant init, boolean isConst) {
        super("@gl" + globvar_cnt, new PointerType(type));
        this.init = init;
        this.isConst = isConst;
    }

    public GlobalVar(String name, LLVMType type, Constant init, boolean isConst) {
        super("@gl" + name, new PointerType(type));
        this.init = init;
        this.isConst = isConst;
    }

    public Constant getInit() {
        return this.init;
    }

    public ArrayList<Integer> getInitNum() {
        if (init instanceof ConstantData) {
            ArrayList<Integer> initNums = new ArrayList<>();
            initNums.add(((ConstantData) init).getNum());
            return initNums;
        } else if (init instanceof ConstantArray) {
            return ((ConstantArray) init).getArrayData();
        } else {
            System.err.println("init in GlobVar is not constant ");
            return null;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" = dso_local ");
        if (isConst) {
            sb.append("constant ");
        } else {
            sb.append("global ");
        }
        // TODO init可能为null嘛
        LLVMType type = ((PointerType) getType()).getTargetType();
        sb.append(type.toString());
        sb.append(" ");
        if (type instanceof ArrayType && ((ConstantArray) init).iszero()) {
            sb.append("zeroinitializer");
        } else {
            sb.append(init.toString());
        }
        sb.append("\n");
        return sb.toString();

    }
}
