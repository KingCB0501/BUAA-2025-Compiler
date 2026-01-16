package backend.Instruction.Assign;

import backend.Instruction.MipsInstr;
import backend.Reg;

public class La extends MipsInstr {
    private Reg reg;
    private String gl_name;

    public La(Reg reg, String gl_name) {
        this.reg = reg;
        this.gl_name = gl_name;
    }

    public String toString() {
        return "la " + reg + ", " + gl_name;
    }
}
