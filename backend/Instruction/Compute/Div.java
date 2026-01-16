package backend.Instruction.Compute;

import backend.Instruction.MipsInstr;
import backend.Reg;

public class Div extends MipsInstr {
    private Reg rs;
    private Reg rt;

    public Div(Reg rs, Reg rt) {
        this.rs = rs;
        this.rt = rt;
    }

    @Override
    public String toString() {
        return "div " + rs + ", " + rt;
    }

}
