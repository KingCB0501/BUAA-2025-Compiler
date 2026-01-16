package llvm.instruction;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import llvm.UserClass.Instruction;
import llvm.Value;
import llvm.type.LLVMType;

public class Icmp extends Instruction {

    public enum OpType {
        EQ,     // ==
        NE,     // !=
        SGT,    // >
        SGE,    // >=
        SLT,    // <
        SLE     // <=
    }


    private OpType op;

    public Icmp(int nameCnt, Token op_token, Value leftOperand, Value rightOperand) {
        super("%v" + nameCnt, LLVMType.INT1);
        this.op = tranToken2OpType(op_token);
        addOperand(leftOperand);
        addOperand(rightOperand);
    }

    public OpType getOp() {
        return op;
    }

    public Value getLeftOperand() {
        return getOperand(0);
    }

    public Value getRightOperand() {
        return getOperand(1);
    }


    /**
     * LSS,          // <
     * LEQ,          // <=
     * GRE,          // >
     * GEQ,          // >=
     * EQL,          // ==
     * NEQ,          // !=
     *
     * @param op_token
     * @return
     */
    private OpType tranToken2OpType(Token op_token) {
        if (op_token.isType(TokenType.LSS)) {
            return OpType.SLT;
        } else if (op_token.isType(TokenType.LEQ)) {
            return OpType.SLE;
        } else if (op_token.isType(TokenType.GRE)) {
            return OpType.SGT;
        } else if (op_token.isType(TokenType.GEQ)) {
            return OpType.SGE;
        } else if (op_token.isType(TokenType.EQL)) {
            return OpType.EQ;
        } else if (op_token.isType(TokenType.NEQ)) {
            return OpType.NE;
        } else if (op_token.isType(TokenType.NOT)) {
            return OpType.EQ;    // 与0比较
        }
        return OpType.EQ;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" = icmp ");
        sb.append(op.toString().toLowerCase());
        sb.append(" i32 ");
        sb.append(getOperand(0).getName());
        sb.append(", ");
        sb.append(getOperand(1).getName());
        return sb.toString();
    }
}
