package backend;

import backend.Data.AsciizData;
import backend.Data.WordData;
import backend.Instruction.Assign.Move;
import backend.Instruction.Branch.Jal;
import backend.Instruction.Branch.Jr;
import backend.Instruction.Compute.Addu;
import backend.Instruction.Branch.Bne;
import backend.Instruction.Compare.CompareMips;
import backend.Instruction.Compute.Div;
import backend.Instruction.Branch.J;
import backend.Instruction.Assign.La;
import backend.Instruction.Compute.Subu;
import backend.Instruction.Label;
import backend.Instruction.Assign.Li;
import backend.Instruction.Memory.Lw;
import backend.Instruction.Compute.Mfhi;
import backend.Instruction.Compute.Mflo;
import backend.Instruction.Compute.Mult;
import backend.Instruction.Shift.Sll;
import backend.Instruction.Memory.Sw;
import backend.Instruction.Syscall;
import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import llvm.UserClass.Function;
import llvm.Value;
import llvm.instruction.Icmp;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsBuilder {
    private MipsModule mipsModule = new MipsModule();

    protected int curOffest = 0;
    protected HashMap<Value, Reg> value2reg = new HashMap<>();
    protected HashMap<Value, Integer> value2offest = new HashMap<>();
    protected Function curFunction = null;


    public MipsModule getMipsModule() {
        return mipsModule;
    }

    public void subcurOffet(int delta) {
        curOffest = curOffest - delta;
    }


    public void makeAsciizData(String name, String literal) {
        AsciizData asciizData = new AsciizData(name, literal);
        mipsModule.addData(asciizData);
    }

    public void makeWordData(String name, ArrayList<Integer> inits) {
        WordData wordData = new WordData(name, inits);
        mipsModule.addData(wordData);
    }

    public void makeLi(Reg reg, int num) {
        Li li = new Li(reg, num);
        mipsModule.addInstruction(li);
    }

    public void makeAdd(Reg rd, Reg rs, Reg rt) {
        Addu addu = new Addu(rd, rs, rt);
        mipsModule.addInstruction(addu);
    }

    public void makeSub(Reg rd, Reg rs, Reg rt) {
        Subu subu = new Subu(rd, rs, rt);
        mipsModule.addInstruction(subu);
    }

    public void makeSw(Reg rt, int offset, Reg base) {
        // 检查16位立即数是否溢出（有符号16位范围：-32768 到 32767）
        if (offset < -32768 || offset > 32767) {
            // 偏移量超出16位范围，需要先计算实际地址
            // 使用 $at 寄存器作为临时寄存器
            makeLi(Reg.at, offset);
            makeAdd(Reg.at, Reg.at, base);
            Sw sw = new Sw(rt, 0, Reg.at);
            mipsModule.addInstruction(sw);
        } else {
            Sw sw = new Sw(rt, offset, base);
            mipsModule.addInstruction(sw);
        }
    }

    public void makeLw(Reg rt, int offset, Reg base) {
        // 检查16位立即数是否溢出（有符号16位范围：-32768 到 32767）
        if (offset < -32768 || offset > 32767) {
            // 偏移量超出16位范围，需要先计算实际地址
            // 使用 $at 寄存器作为临时寄存器
            makeLi(Reg.at, offset);
            makeAdd(Reg.at, Reg.at, base);
            Lw lw = new Lw(rt, 0, Reg.at);
            mipsModule.addInstruction(lw);
        } else {
            Lw lw = new Lw(rt, offset, base);
            mipsModule.addInstruction(lw);
        }
    }

    public void makeLa(Reg rt, String name) {
        La la = new La(rt, name);
        mipsModule.addInstruction(la);
    }

    public void makeSll(Reg rd, Reg rt, int s) {
        Sll sll = new Sll(rd, rt, s);
        mipsModule.addInstruction(sll);
    }

    public void makeCompute(Token op_token, Reg ans, Reg op1, Reg op2) {
        if (op_token.isType(TokenType.PLUS)) {
            makeAdd(ans, op1, op2);
        } else if (op_token.isType(TokenType.MINU)) {
            makeSub(ans, op1, op2);
        } else if (op_token.isType(TokenType.MULT)) {
            Mult mult = new Mult(op1, op2);
            mipsModule.addInstruction(mult);
            Mflo mflo = new Mflo(ans);
            mipsModule.addInstruction(mflo);
        } else if (op_token.isType(TokenType.DIV)) {
            Div div = new Div(op1, op2);
            mipsModule.addInstruction(div);
            Mflo mflo = new Mflo(ans);
            mipsModule.addInstruction(mflo);
        } else if (op_token.isType(TokenType.MOD)) {
            Div div = new Div(op1, op2);
            mipsModule.addInstruction(div);
            Mfhi mfhi = new Mfhi(ans);
            mipsModule.addInstruction(mfhi);
        } else {
            System.err.println("Unknown token type: " + op_token);
        }
    }

    public void makeCompare(Icmp.OpType opType, Reg rd, Reg rs, Reg rt) {
        CompareMips compareMips = null;
        if (opType == Icmp.OpType.EQ) {
            compareMips = new CompareMips(CompareMips.Op.SEQ, rd, rs, rt);
        } else if (opType == Icmp.OpType.NE) {
            compareMips = new CompareMips(CompareMips.Op.SNE, rd, rs, rt);
        } else if (opType == Icmp.OpType.SGT) {
            compareMips = new CompareMips(CompareMips.Op.SGT, rd, rs, rt);
        } else if (opType == Icmp.OpType.SGE) {
            compareMips = new CompareMips(CompareMips.Op.SGE, rd, rs, rt);
        } else if (opType == Icmp.OpType.SLT) {
            compareMips = new CompareMips(CompareMips.Op.SLT, rd, rs, rt);
        } else if (opType == Icmp.OpType.SLE) {
            compareMips = new CompareMips(CompareMips.Op.SLE, rd, rs, rt);
        } else {
            System.err.println("Unrecognized op type: " + opType);
        }
        mipsModule.addInstruction(compareMips);
    }

    public void makeLabel(String str) {
        Label label = new Label(str);
        mipsModule.addInstruction(label);
    }

    public void makeJ(String string) {
        J j = new J(string);
        mipsModule.addInstruction(j);
    }

    public void makeBne(Reg rs, Reg rt, String label) {
        Bne bne = new Bne(rs, rt, label);
        mipsModule.addInstruction(bne);
    }

    public void makeSyscall() {
        Syscall syscall = new Syscall();
        mipsModule.addInstruction(syscall);
    }

    public void makeJal(String str) {
        Jal jal = new Jal(str);
        mipsModule.addInstruction(jal);
    }

    public ArrayList<Reg> getAllocatedRegs() {
        ArrayList<Reg> regs = new ArrayList<>();
        for (Value value : value2reg.keySet()) {
            regs.add(value2reg.get(value));
        }
        return regs;
    }

    public void makeJr(Reg rs) {
        Jr jr = new Jr(rs);
        mipsModule.addInstruction(jr);
    }

    public Move makeMove(Reg target, Reg from) {
        Move move = new Move(target, from);
        mipsModule.addInstruction(move);
        return move;
    }


}
