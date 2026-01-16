package frontend.Lexer;

import Utils.Error;
import Utils.ErrorLog;
import Utils.ErrorType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;

public class Lexer {
    private final PushbackReader reader;
    private int lineNumber;
    private int temp;

    public Lexer(String filename) throws IOException {
        File file = new File(filename);
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(file);
        } catch (FileNotFoundException e) {
            System.err.println("testfile not found");
            System.exit(1);
        }
        reader = new PushbackReader(fileReader);
        lineNumber = 1;
        temp = 0;
    }

    private int read() throws IOException {
        int ch = reader.read();
        if (ch == '\n') {
            lineNumber++;
        }
        return ch;
    }

    private void unread(int temp) throws IOException {
        if (temp != -1) {
            if (temp == '\n') {
                lineNumber--;
            }
            reader.unread(temp);
        }
    }

    /**
     * 跳过空白字符，处理行号
     */
    private void skipSpace() throws IOException {
        temp = read();
        while ((temp != -1) && Character.isWhitespace(temp)) {
            temp = read();
        }
        unread(temp);
    }

    /**
     * 跳过注释: /*或者//
     */
    private void skipComment(int ch) throws IOException {
        if (ch == '/') {
            while (temp != -1 && temp != '\n') {
                temp = read();
            }
        } else if (ch == '*') {
            while (temp != -1) {
                temp = read();
                if (temp == '*') {
                    temp = read();
                    if (temp == '/') {
                        return;
                    }else {
                        unread(temp);
                    }
                }
            }

        }
    }

    /**
     * 判断是否是特殊字符
     */
    private boolean isSpecialChar(int ch) {
        return ch == '!' || ch == '&' || ch == '|' || ch == '+' || ch == '-' || ch == '*' || ch == '/' || ch == '%'
                || ch == '<' || ch == '>' || ch == '=' || ch == ';' || ch == ','
                || ch == '(' || ch == ')' || ch == '{' || ch == '}' || ch == '[' || ch == ']';

    }

    /**
     * 读取特殊字符Token
     */
    private Token getSpecialToken() throws IOException {
        temp = read();
        StringBuilder sb = new StringBuilder();
        sb.append((char) temp);
        switch (temp) {
            case '!':
                temp = read();
                if (temp == '=') {
                    sb.append('=');
                    return new Token(TokenType.NEQ, sb.toString(), lineNumber);
                } else {
                    unread(temp);
                    return new Token(TokenType.NOT, sb.toString(), lineNumber);
                }
            case '&':
                temp = read();
                if (temp == '&') {
                    sb.append('&');
                    return new Token(TokenType.AND, sb.toString(), lineNumber);
                } else {
                    unread(temp);
                    ErrorLog.getInstance().addError(new Error(ErrorType.a, lineNumber));
                    return new Token(TokenType.AND, sb.toString(), lineNumber);
                }
            case '|':
                temp = read();
                if (temp == '|') {
                    sb.append('|');
                    return new Token(TokenType.OR, sb.toString(), lineNumber);
                } else {
                    unread(temp);
                    ErrorLog.getInstance().addError(new Error(ErrorType.a, lineNumber));
                    return new Token(TokenType.OR, sb.toString(), lineNumber);
                }
            case '+':
                return new Token(TokenType.PLUS, sb.toString(), lineNumber);
            case '-':
                return new Token(TokenType.MINU, sb.toString(), lineNumber);
            case '*':
                return new Token(TokenType.MULT, sb.toString(), lineNumber);
            case '/':
                temp = read();
                if (temp == '*' || temp == '/') {
                    skipComment(temp);    // 跳过注释
                    return null;
                } else {
                    unread(temp);
                    return new Token(TokenType.DIV, sb.toString(), lineNumber);
                }
            case '%':
                return new Token(TokenType.MOD, sb.toString(), lineNumber);
            case '<':
                temp = read();
                if (temp == '=') {
                    sb.append('=');
                    return new Token(TokenType.LEQ, sb.toString(), lineNumber);
                } else {
                    unread(temp);
                    return new Token(TokenType.LSS, sb.toString(), lineNumber);
                }
            case '>':
                temp = read();
                if (temp == '=') {
                    sb.append('=');
                    return new Token(TokenType.GEQ, sb.toString(), lineNumber);
                } else {
                    unread(temp);
                    return new Token(TokenType.GRE, sb.toString(), lineNumber);
                }
            case '=':
                temp = read();
                if (temp == '=') {
                    sb.append('=');
                    return new Token(TokenType.EQL, sb.toString(), lineNumber);
                } else {
                    unread(temp);
                    return new Token(TokenType.ASSIGN, sb.toString(), lineNumber);
                }
            case ';':
                return new Token(TokenType.SEMICN, sb.toString(), lineNumber);
            case ',':
                return new Token(TokenType.COMMA, sb.toString(), lineNumber);
            case '(':
                return new Token(TokenType.LPARENT, sb.toString(), lineNumber);
            case ')':
                return new Token(TokenType.RPARENT, sb.toString(), lineNumber);
            case '[':
                return new Token(TokenType.LBRACK, sb.toString(), lineNumber);
            case ']':
                return new Token(TokenType.RBRACK, sb.toString(), lineNumber);
            case '{':
                return new Token(TokenType.LBRACE, sb.toString(), lineNumber);
            case '}':
                return new Token(TokenType.RBRACE, sb.toString(), lineNumber);
            default:
                System.out.println("Unrecognized specialToken: " + sb.toString() + "in line " + lineNumber);
                return null;
        }
    }

    /**
     * 读取常量字符串
     */
    private Token getStringConst() throws IOException {
        StringBuilder sb = new StringBuilder();
        temp = read();  // 读取第一个"
        sb.append((char) temp);
        temp = read();
        while (temp != '\"') {
            sb.append((char) temp);
            temp = read();
        }
        sb.append('\"');
        return new Token(TokenType.STRCON, sb.toString(), lineNumber);
    }

    /**
     * 读取数字const字符串
     */
    private Token getIntConst() throws IOException {
        temp = read();
        StringBuilder sb = new StringBuilder();
        while (Character.isDigit(temp)) {
            sb.append((char) temp);
            temp = read();
        }
        unread(temp);
        return new Token(TokenType.INTCON, sb.toString(), lineNumber);
    }

    /**
     * 读取标识符或者保留字
     */
    private Token getIdent() throws IOException {
        temp = read();
        StringBuilder sb = new StringBuilder();
        while (Character.isLetter(temp) || Character.isDigit(temp) || temp == '_') {
            sb.append((char) temp);
            temp = read();
        }
        unread(temp);

        String string = sb.toString();
        switch (string) {
            case "const":
                return new Token(TokenType.CONSTTK, string, lineNumber);
            case "int":
                return new Token(TokenType.INTTK, string, lineNumber);
            case "static":
                return new Token(TokenType.STATICTK, string, lineNumber);
            case "break":
                return new Token(TokenType.BREAKTK, string, lineNumber);
            case "continue":
                return new Token(TokenType.CONTINUETK, string, lineNumber);
            case "if":
                return new Token(TokenType.IFTK, string, lineNumber);
            case "main":
                return new Token(TokenType.MAINTK, string, lineNumber);
            case "else":
                return new Token(TokenType.ELSETK, string, lineNumber);
            case "for":
                return new Token(TokenType.FORTK, string, lineNumber);
            case "return":
                return new Token(TokenType.RETURNTK, string, lineNumber);
            case "void":
                return new Token(TokenType.VOIDTK, string, lineNumber);
            case "printf":
                return new Token(TokenType.PRINTFTK, string, lineNumber);
            default:
                return new Token(TokenType.IDENFR, string, lineNumber);
        }
    }

    /**
     * 1. 标识符 / 保留字
     * 2. 数字常量
     * 3. 字符串常量 "开头
     * 4. 特殊字符
     *
     * @return Token
     * @throws IOException
     */
    private Token nextToken() throws IOException {
        temp = read();
        unread(temp);
        if (isSpecialChar(temp)) {
            return getSpecialToken();
        } else if (temp == '\"') {
            return getStringConst();
        } else if (Character.isDigit(temp)) {
            return getIntConst();
        } else if (temp == '_' || Character.isLetter(temp)) {
            return getIdent();
        }
        return null;
    }

    public TokenStream lex() throws IOException {
        TokenStream tokenStream = new TokenStream();
        while (true) {
            skipSpace();
            temp = read();
            if (temp == -1) {// 文件结尾
                tokenStream.addToken(new Token(TokenType.EOF, "", lineNumber));
                break;
            } else {
                unread(temp);
            }
            Token token = nextToken();
            if (token != null) {
                tokenStream.addToken(token);
            }
        }
        return tokenStream;
    }

}
