package backend.Instruction.Assign;

import backend.Instruction.MipsInstr;
import backend.Reg;

public class Move extends MipsInstr {
    private Reg target;
    private Reg from;

    public Move(Reg target, Reg from) {
        this.from = from;
        this.target = target;
    }

    public String toString() {
        return "move " + target.toString() + ", " + from.toString();
    }
}
