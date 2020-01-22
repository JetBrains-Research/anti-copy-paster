package ru.hse.kirilenko.refactorings.legacy;

import java.util.Arrays;
import java.util.List;

public class Tmp {
    public static List<String> allKeywords = Arrays.asList(
            "abstract", "continue", "for", "new", "switch", "assert",
            "default", "package", "synchronized", "boolean", "do", "if",
            "private", "this", "break", "double", "implements", "protected",
            "throw", "byte", "else", "import", "public", "throws", "case",
            "enum", "instanceof", "return", "transient", "catch", "extends",
            "int", "short", "try", "char", "final", "interface", "static",
            "void", "class", "finally", "long", "strictfp", "volatile",
            "const", "float", "native", "super", "while");

    public static void main(String[] args) {
        int id = 6;
        for (String str: allKeywords) {
            String capFL = str.substring(0, 1).toUpperCase() + str.substring(1);
            String name = "Keyword" + capFL + "TotalCount";
            System.out.println(name + "(\"" + name + "\", " + id + "),");
            id++;

            name = "Keyword" + capFL + "CountPerLine";
            System.out.println(name + "(\"" + name + "\", " + id + "),");
            id++;
        }
    }
}
