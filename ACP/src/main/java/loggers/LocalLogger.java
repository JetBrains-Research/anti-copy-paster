package loggers;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LocalLogger implements ILogger {
    private PrintWriter printWriter;

    public LocalLogger(String fileName) throws IOException {
        FileWriter fileWriter = new FileWriter(fileName, true);
        final PrintWriter printWriter = new PrintWriter(fileWriter, true);
        this.printWriter = printWriter;
    }

    public void log(final String text, boolean addNewLine) {
        if (addNewLine) {
            printWriter.println(text);
        } else {
            printWriter.print(text);
        }

        printWriter.flush();
    }
}
