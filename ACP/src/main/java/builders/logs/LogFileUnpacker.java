package builders.logs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class LogFileUnpacker {
    private BufferedReader fileReader;
    private LogItem currentItem;

    public LogFileUnpacker(String fileName) throws IOException {
        FileReader fileInput = new FileReader(fileName);
        this.fileReader = new BufferedReader(fileInput);

        String firstItem = fileReader.readLine();
        this.currentItem = LogItem.fromRawValue(firstItem);
    }

    public LogPair next() throws IOException {
        if (this.currentItem == LogItem.END) {
            return new LogPair(LogItem.END, "");
        }

        StringBuilder dataBuilder = new StringBuilder();

        while(true) {
            String nextLine = fileReader.readLine();
            LogItem optionalAction = LogItem.fromRawValue(nextLine);
            if (optionalAction != null) {
                String value = dataBuilder.toString();
                if (!value.isEmpty()) {
                    value = value.substring(0, value.length() - 1);
                }

                LogPair result = new LogPair(this.currentItem, value);
                this.currentItem = optionalAction;
                return result;
            } else {
                dataBuilder.append(nextLine);
                dataBuilder.append("\n");
            }
        }
    }
}
