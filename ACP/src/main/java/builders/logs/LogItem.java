package builders.logs;

public enum LogItem {
    ACTION("ACTION"),
    BEGIN_OFFSET("BEGIN_OFFSET"),
    END_OFFSET("END_OFFSET"),
    FILE_PATH("FILE_PATH"),
    IS_COMPRESSED("IS_COMPRESSED"),
    FILE_CONTENT("FILE_CONTENT"),
    CODE_FRAGMENT("CODE_FRAGMENT"),
    PROJECT_ID("PROJECT_ID"),
    USER_NAME("USER_NAME"),
    END("END");

    private String rawValue;

    LogItem(String rawValue) {
        this.rawValue = rawValue;
    }

    public String rawValue() {
        return rawValue;
    }

    public static LogItem fromRawValue(final String raw) {
        switch (raw) {
            case "ACTION":
                return LogItem.ACTION;
            case "BEGIN_OFFSET":
                return LogItem.BEGIN_OFFSET;
            case "END_OFFSET":
                return LogItem.END_OFFSET;
            case "FILE_PATH":
                return LogItem.FILE_PATH;
            case "IS_COMPRESSED":
                return LogItem.IS_COMPRESSED;
            case "FILE_CONTENT":
                return LogItem.FILE_CONTENT;
            case "CODE_FRAGMENT":
                return LogItem.CODE_FRAGMENT;
            case "PROJECT_ID":
                return LogItem.PROJECT_ID;
            case "USER_NAME":
                return LogItem.USER_NAME;
            case "END":
                return LogItem.END;
            default:
                return null;
        }
    }
}