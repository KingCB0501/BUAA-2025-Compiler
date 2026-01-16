package backend.Instruction.Branch;

import backend.Instruction.MipsInstr;
import backend.Reg;

public class Jr extends MipsInstr {

    private Reg rs;

    public Jr(Reg rs) {
        this.rs = rs;
    }

    public String toString() {
        return "jr " + rs;
    }
}
