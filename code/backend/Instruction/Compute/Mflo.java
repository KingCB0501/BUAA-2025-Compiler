package backend.Instruction.Compute;

import backend.Instruction.MipsInstr;
import backend.Reg;

public class Mflo extends MipsInstr {
    private Reg rd;

    public Mflo(Reg rd) {
        this.rd = rd;
    }

    public String toString() {
        return "mflo " + rd;
    }
}
