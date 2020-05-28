package builders;

public class LogEventBuilder {
    private StringBuilder builder;

    public LogEventBuilder() {
        builder = new StringBuilder();
    }

    public void addItem(final LogItem action, final String value) {
        builder.append(action.rawValue());
        builder.append('\n');
        builder.append(value);
        builder.append('\n');
    }

    public String build() {
        builder.append(LogItem.END.rawValue());

        return builder.toString();
    }
}
