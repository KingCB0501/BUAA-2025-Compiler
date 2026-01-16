package backend.Instruction.Compare;

import backend.Instruction.MipsInstr;
import backend.Reg;

public class CompareMips extends MipsInstr {
    public enum Op {
        SLT, SLE, SGT, SGE, SEQ, SNE
    }

    private Op op;
    private Reg rd;
    private Reg rs;
    private Reg rt;

    public CompareMips(Op op, Reg rd, Reg rs, Reg rt) {
        this.op = op;
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
    }

    public String toString() {
        return op.toString().toLowerCase() + " " + rd.toString() + ",  " + rs.toString() + ", " + rt.toString();
    }
}
