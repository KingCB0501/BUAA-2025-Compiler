package backend.Instruction.Branch;

import backend.Instruction.MipsInstr;

public class J extends MipsInstr {
    String label;

    public J(String label) {
        this.label = label;
    }

    public String toString() {
        return "j " + label;
    }
}
