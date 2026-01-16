package backend.Instruction.Shift;

import backend.Instruction.MipsInstr;
import backend.Reg;

public class Sll extends MipsInstr {
    private Reg rd;
    private Reg rt;

    private int s;

    public Sll(Reg rd, Reg rt, int s) {
        this.rd = rd;
        this.rt = rt;
        this.s = s;
    }

    @Override
    public String toString() {
        return "sll " + rd + ", " + rt + ", " + s;
    }
}
