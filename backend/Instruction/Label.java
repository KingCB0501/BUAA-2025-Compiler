package backend.Instruction;

public class Label extends MipsInstr {
    private String label;

    public Label(String label) {
        this.label = label;
    }

    public String toString() {
        return label + ":\n";
    }
}
