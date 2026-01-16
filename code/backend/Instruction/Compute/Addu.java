package backend.Instruction.Compute;

import backend.Instruction.MipsInstr;
import backend.Reg;

public class Addu extends MipsInstr {

    private Reg rd;
    private Reg rs;
    private Reg rt;

    public Addu(Reg rd, Reg rs, Reg rt) {
        this.rd = rd;
        this.rs = rs;
        this.rt = rt;
    }

    @Override
    public String toString() {

        return "addu " + rd.toString() + ", " + rs.toString() + ", " + rt.toString();

    }
}
