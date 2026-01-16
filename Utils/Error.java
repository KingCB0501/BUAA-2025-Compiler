package Utils;

public class Error {
    private ErrorType type;
    private int linenumber;

    public Error(ErrorType type, int linenumber) {
        this.type = type;
        this.linenumber = linenumber;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(linenumber);
        sb.append(" ");
        sb.append(type.toString());
        return sb.toString();
    }
}
