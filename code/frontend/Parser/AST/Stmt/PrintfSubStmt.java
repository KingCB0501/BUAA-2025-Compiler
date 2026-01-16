package frontend.Parser.AST.Stmt;

import frontend.Lexer.Token;
import frontend.Lexer.TokenType;
import frontend.Parser.AST.BlockItem;
import frontend.Parser.AST.Exp.Exp;
import frontend.Parser.AST.Node;

import java.util.ArrayList;

/**
 * Stmt → 'printf''('StringConst {','Exp}')'';'
 */
public class PrintfSubStmt extends Node implements BlockItem, Stmt {
    private Token string_token;
    private ArrayList<Exp> exps;
    Token printf_token;

    public PrintfSubStmt(Token string_token, ArrayList<Exp> exps, Token printf_token) {
        this.string_token = string_token;
        this.exps = exps;
        this.printf_token = printf_token;
    }

    public Token getStringToken() {
        return string_token;
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }

    public int getLineNumber() {
        return printf_token.getLineNumber();
    }

    public ArrayList<String> spiltString4d() {
        String string = string_token.getValue();
        ArrayList<String> strings = new ArrayList<>();
        // 1. 去除首尾双引号（如果存在）
        if (string.startsWith("\"") && string.endsWith("\"")) {
            string = string.substring(1, string.length() - 1);
        }

        // 2. 正则表达式切分，保留分隔符 %d
        //  (?=%d)|(?<=%d)  表示在 %d 的前后做零宽切分
        String[] parts = string.split("(?=%d)|(?<=%d)");

        // 3. 加入结果
        for (String p : parts) {
            if (!p.isEmpty()) {
                strings.add(p);
            }
        }

        return strings;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Token.TokenPrint(TokenType.PRINTFTK));
        sb.append(Token.TokenPrint(TokenType.LPARENT));
        sb.append(string_token.toString());
        for (Exp exp : exps) {
            sb.append(Token.TokenPrint(TokenType.COMMA));
            sb.append(exp.toString());
        }
        sb.append(Token.TokenPrint(TokenType.RPARENT));
        sb.append(Token.TokenPrint(TokenType.SEMICN));
        sb.append("<Stmt>\n");
        return sb.toString();
    }
}
