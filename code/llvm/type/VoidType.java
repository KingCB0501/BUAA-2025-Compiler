package llvm.type;

public class VoidType extends BaseIntegerType {
    public VoidType() {
    }

    @Override
    public boolean isVoid() {
        return true;
    }

    @Override
    public String toString() {
        return "void";
    }

    @Override
    public boolean isInt32() {
        return false;
    }

    @Override
    public boolean isInt1() {
        return false;
    }

    @Override
    public boolean isInteger() {
        return false;
    }
}
