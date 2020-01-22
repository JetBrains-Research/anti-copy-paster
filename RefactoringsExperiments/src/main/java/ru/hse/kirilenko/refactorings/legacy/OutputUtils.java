package ru.hse.kirilenko.refactorings.legacy;

import gr.uom.java.xmi.decomposition.AbstractStatement;
import gr.uom.java.xmi.decomposition.CompositeStatementObject;
import gr.uom.java.xmi.decomposition.StatementObject;

import java.io.PrintWriter;

public final class OutputUtils {
    public static void printCompositeStatement(CompositeStatementObject statementObject, int offset, PrintWriter pw) {
        String statement = statementObject.toString();
        boolean shouldCloseBracket = false;
        if(statement.equals("{")) shouldCloseBracket = true;

        if(statement.equals("{")) {
            printLn(pushSpaces(statement, offset + 2), pw);
        } else {
            printLn(pushSpaces(statement, statementObject.getLocationInfo().getStartColumn()), pw);
        }

        for (AbstractStatement as: statementObject.getStatements()) {
            if (as instanceof CompositeStatementObject) {
                printCompositeStatement((CompositeStatementObject)as, as.getLocationInfo().getStartColumn() + 2, pw);
            } else if (as instanceof StatementObject) {
                printLn(pushSpaces(((StatementObject)as).toString(), as.getLocationInfo().getStartColumn()), pw);
            } else {
                printLn(pushSpaces("ERROR STATEMENT\n", as.getLocationInfo().getStartColumn()), pw);
            }
        }

        if (shouldCloseBracket) printLn(pushSpaces("}", offset + 2), pw);
    }

    public static void printLn(String line, PrintWriter pw) {
        if (line.endsWith("\n")) {
            if (pw == null) {
                System.out.print(line);
            } else {
                pw.print(line);
            }
        } else {
            if (pw == null) {
                System.out.println(line);
            } else {
                pw.println(line);
            }
        }
    }

    private static String pushSpaces(String to, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; ++i) {
            sb.append(' ');
        }
        sb.append(to);

        return sb.toString();
    }
}
