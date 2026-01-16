package llvm;

import llvm.type.BaseIntegerType;
import llvm.type.LLVMType;

import java.util.ArrayList;
import java.util.Iterator;

public class Value {
    private String name;
    private LLVMType type;


    public Value(String name, LLVMType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * 匿名数据存储
     */
    public Value(LLVMType type) {
        this.type = type;
    }

    public boolean isINT32() {
        if (this.getType() instanceof BaseIntegerType) {
            if (((BaseIntegerType) this.getType()).isInt32()) {
                return true;
            }
        }
        return false;
    }

    public boolean isINT1() {
        if (this.getType() instanceof BaseIntegerType) {
            if (((BaseIntegerType) this.getType()).isInt1()) {
                return true;
            }
        }
        return false;
    }

    public LLVMType getType() {
        return type;
    }

    public String getName() {
        return name;
    }


    ///  -----Optimize-----
    private ArrayList<Use> uses = new ArrayList<>(); // 当前value被谁使用

    private Value Host = null;

    public void addUse(User user) {
        Use use = new Use(user, this);
        uses.add(use);
    }

    public void removeUse(User user) {
        uses.removeIf(use -> use.getUser().equals(user));
    }


    public ArrayList<Use> getUses() {
        return uses;
    }

    public void setHost(Value host) {
        Host = host;
    }

    public Value getHost() {
        return Host;
    }

    // 将所有用到value的地方换成new Value
    public void replacAllUsesWith(Value newValue) {
        for (Use use : uses) {
            User user = use.getUser();
            user.replaceOperand(this, newValue);
        }
    }

    public boolean isNoSense() {
        return false;
    }
}
