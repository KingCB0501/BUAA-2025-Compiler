package Utils;

import java.util.ArrayList;

public class ErrorLog {
    private final ArrayList<Error> errorList;
    private static ErrorLog errorLog;

    private ErrorLog() {
        errorList = new ArrayList<>();
    }

    public static ErrorLog getInstance() {
        if (errorLog == null) {
            errorLog = new ErrorLog();
        }
        return errorLog;
    }

    public void addError(Error error) {
        errorList.add(error);
    }

    public int getErrorCount() {
        return errorList.size();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Error error : errorList) {
            stringBuilder.append(error.toString());
        }
        return stringBuilder.toString();
    }
}
