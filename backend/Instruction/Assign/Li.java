package backend.Instruction.Assign;

import backend.Instruction.MipsInstr;
import backend.Reg;

public class Li extends MipsInstr {

    private Reg reg;
    private int num;

    public Li(Reg reg, int num) {
        this.reg = reg;
        this.num = num;
    }

    @Override
    public String toString() {
        return "li " + reg.toString() + ", " + num;
    }
}
