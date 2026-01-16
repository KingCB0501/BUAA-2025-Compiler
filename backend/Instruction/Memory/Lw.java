package backend.Instruction.Memory;

import backend.Instruction.MipsInstr;
import backend.Reg;

public class Lw extends MipsInstr {
    private Reg rt;
    int offset;
    private Reg base;

    public Lw(Reg rt, int offset, Reg base) {
        this.rt = rt;
        this.offset = offset;
        this.base = base;
    }

    @Override
    public String toString() {
        return "lw " + rt + ", " + offset + "(" + base + ")";
    }
}
