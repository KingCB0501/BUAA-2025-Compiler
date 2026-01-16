package backend.Instruction.Branch;

import backend.Instruction.MipsInstr;

public class Jal extends MipsInstr {
    private String name;

    public Jal(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "jal " + name;
    }
}
