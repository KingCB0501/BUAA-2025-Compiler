package frontend.Parser.AST.Stmt;

import java.util.ArrayList;



class PrintfSubStmtTest {
    public static void main(String[] args) {
        String string = "string_token.getValue()%d44444";
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

        System.out.println(strings);
    }
}