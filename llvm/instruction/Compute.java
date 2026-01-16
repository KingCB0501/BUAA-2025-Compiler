package llvm.instruction;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import llvm.UserClass.Instruction;
import llvm.Value;
import llvm.type.LLVMType;

public class Compute extends Instruction {
    private Token op_token;

    public Compute(int nameCnt, Token op_token, Value leftOperand, Value rightOperand) {
        super("%v" + nameCnt, LLVMType.INT32);
        this.op_token = op_token;
        addOperand(leftOperand);
        addOperand(rightOperand);
    }

    public Token getOpToken() {
        return op_token;
    }

    public Value getLeftOperand() {
        return getOperand(0);
    }

    public Value getRightOperand() {
        return getOperand(1);
    }

    public String getOpString() {
        if (op_token.isType(TokenType.PLUS)) {
            return "add";
        } else if (op_token.isType(TokenType.MINU)) {
            return "sub";
        } else if (op_token.isType(TokenType.MULT)) {
            return "mul";
        } else if (op_token.isType(TokenType.DIV)) {
            return "sdiv";
        } else if (op_token.isType(TokenType.MOD)) {
            return "srem";
        } else {
            System.err.println("Unknown op token: " + op_token);
            return "";
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(" = ");
        sb.append(getOpString());
        sb.append(" i32 ");
        sb.append(getOperand(0).getName());
        sb.append(", ");
        sb.append(getOperand(1).getName());
        return sb.toString();
    }
}
