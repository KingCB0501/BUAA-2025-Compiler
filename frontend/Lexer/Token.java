package frontend.Lexer;

/**
 * 词法单元
 */
public class Token {
    private TokenType type;
    private String value; // 对于标识符、整数、字符串常量，存储其值
    private int lineNumber;

    public Token(TokenType type, String value, int lineNumber) {
        this.type = type;
        this.value = value;
        this.lineNumber = lineNumber;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.toString());
        sb.append(" ");
        if (value != null) {
            sb.append(value);
        }
        sb.append("\n");
        return sb.toString();
    }

}