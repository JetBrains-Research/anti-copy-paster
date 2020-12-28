package builders.logs;

import builders.logs.LogItem;

public class LogPair {
    public LogItem type;
    public String value;

    public LogPair(LogItem type, String value) {
        this.type = type;
        this.value = value;
    }
}