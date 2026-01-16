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

    public Boolean isType(TokenType type) {
        return this.type == type;
    }

    public int getLineNumber() {
        return this.lineNumber;
    }

    public static String TokenPrint(TokenType tokenType) {
        StringBuilder sb = new StringBuilder();
        sb.append(tokenType.toString());
        sb.append(" ");
        switch (tokenType) {
            case CONSTTK:
                sb.append("const");
                break;
            case INTTK:
                sb.append("int");
                break;
            case STATICTK:
                sb.append("static");
                break;
            case BREAKTK:
                sb.append("break");
                break;
            case CONTINUETK:
                sb.append("continue");
                break;
            case IFTK:
                sb.append("if");
                break;
            case MAINTK:
                sb.append("main");
                break;
            case ELSETK:
                sb.append("else");
                break;
            case NOT:
                sb.append("!");
                break;
            case AND:
                sb.append("&&");
                break;
            case OR:
                sb.append("||");
                break;
            case FORTK:
                sb.append("for");
                break;
            case RETURNTK:
                sb.append("return");
                break;
            case VOIDTK:
                sb.append("void");
                break;
            case PLUS:
                sb.append("+");
                break;
            case MINU:
                sb.append("-");
                break;
            case PRINTFTK:
                sb.append("printf");
                break;
            case MULT:
                sb.append("*");
                break;
            case DIV:
                sb.append("/");
                break;
            case MOD:
                sb.append("%");
                break;
            case LSS:
                sb.append("<");
                break;
            case LEQ:
                sb.append("<=");
                break;
            case GEQ:
                sb.append(">=");
                break;
            case GRE:
                sb.append(">");
                break;
            case EQL:
                sb.append("==");
                break;
            case NEQ:
                sb.append("!=");
                break;
            case SEMICN:
                sb.append(";");
                break;
            case COMMA:
                sb.append(",");
                break;
            case LPARENT:
                sb.append("(");
                break;
            case RPARENT:
                sb.append(")");
                break;
            case LBRACK:
                sb.append("[");
                break;
            case RBRACK:
                sb.append("]");
                break;
            case LBRACE:
                sb.append("{");
                break;
            case RBRACE:
                sb.append("}");
                break;
            case ASSIGN:
                sb.append("=");
                break;
            default:
                sb.append("error");
                break;
        }

        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(type.toString());
        sb.append(" ");
        if (value != null) {
            sb.append(value);
        }
//        sb.append(" ").append(lineNumber);
        sb.append("\n");
        return sb.toString();
    }

}