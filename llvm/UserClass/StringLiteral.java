package llvm.UserClass;

import llvm.Value;
import llvm.type.ArrayType;
import llvm.type.LLVMType;
import llvm.type.PointerType;

public class StringLiteral extends Value {
    private String llvmStr;
    private String oldStr;

    public StringLiteral(int strcnt, String str) {
        super("@str" + strcnt, new PointerType(getLiteralType(str)));
        this.llvmStr = deleteEnter(str);
        this.oldStr = str;
    }

    private static LLVMType getLiteralType(String literal) {
        String delete = deleteEnter(literal);
        int enterNum = delete.length() - literal.length();
        int length = literal.length() - enterNum;
        return new ArrayType(8, length + 1);
    }

    public String getOldStr() {
        return oldStr;
    }

    //TODO panduan
    private static String deleteEnter(String input) {
        return input.replaceAll("\\\\n", "\\\\0A");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" = ");
        sb.append("constant ");
        sb.append(((PointerType) getType()).getTargetType());
        sb.append(" c\"");
        sb.append(llvmStr);
        sb.append("\\00\"");
        sb.append("\n");
        return sb.toString();
    }
}
