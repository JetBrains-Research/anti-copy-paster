package ru.hse.kirilenko.refactorings.legacy;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class CSVBuilder {
    public static CSVBuilder shared;

    static {
        try {
            shared = new CSVBuilder("test1.csv");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PrintWriter pw;
    private boolean isFirst = true;
    public int locs = 0;

    public CSVBuilder(String fileName) throws IOException {
        FileWriter fileWriter = new FileWriter(fileName);
        final PrintWriter printWriter = new PrintWriter(fileWriter);
        this.pw = printWriter;
    }

    public void completeLine(boolean mark) {
        if (!isFirst) {
            pw.println(" " + (mark ? "1" : "0"));
        }

        locs++;
        isFirst = false;
    }

    public void addStr(String s) {
        pw.print(s);
        pw.print(";");
    }

    public void addStr(Integer s) {
        pw.print(s);
        pw.print(";");
    }

    public void addStr(Double s) {
        pw.print(s);
        pw.print(";");
    }

    public void addStr(Boolean s) {
        pw.print(s);
        pw.print(";");
    }

    public void finish(boolean mark) {
        pw.println(" " + (mark ? "1" : "0"));
        pw.close();
    }
}
