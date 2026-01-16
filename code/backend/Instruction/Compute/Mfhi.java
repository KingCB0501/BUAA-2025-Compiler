package backend.Instruction.Compute;

import backend.Instruction.MipsInstr;
import backend.Reg;

public class Mfhi extends MipsInstr {
    private Reg rd;

    public Mfhi(Reg rd) {
        this.rd = rd;
    }

    public String toString() {
        return "mfhi " + rd;
    }
}
