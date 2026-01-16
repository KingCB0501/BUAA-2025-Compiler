package backend.Data;

/**
 * msg: .asciiz "Hello, MIPS!"    mips会自动添加\0
 * <p>
 * la $a0, msg
 * li $v0, 4
 * syscall
 */
public class AsciizData extends MipsData {
    private String asciiString;

    public AsciizData(String name, String asciiString) {
        super(name);
        this.asciiString = asciiString;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName());
        sb.append(": .asciiz ");
        sb.append("\"" + asciiString + "\"");
        return sb.toString();
    }
}
