package backend.Instruction.Branch;

import backend.Instruction.MipsInstr;
import backend.Reg;

// 不等于时候跳转

public class Bne extends MipsInstr {
    private Reg rs;
    private Reg rt;
    private String label;

    public Bne(Reg rs, Reg rt, String label) {
        this.rs = rs;
        this.rt = rt;
        this.label = label;
    }

    public String toString() {
        return "bne " + rs + ", " + rt + ", " + label;
    }
}
