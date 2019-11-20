package ru.hse.kirilenko.refactorings.utils;

import ru.hse.kirilenko.refactorings.collectors.CSVBuilder;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static ru.hse.kirilenko.refactorings.utils.OutputUtils.printLn;

public class KeywordsCalculator {
    public static List<String> allKeywords = Arrays.asList(
            "abstract", "continue", "for", "new", "switch", "assert", "default", "package", "synchronized", "boolean", "do", "if", "private", "this", "break", "double", "implements", "protected", "throw", "byte", "else", "import", "public", "throws", "case", "enum", "instanceof", "return", "transient", "catch", "extends", "int", "short", "try", "char", "final", "interface", "static", "void", "class", "finally", "long", "strictfp", "volatile", "const", "float", "native", "super", "while");

    public static Trie keywordsTrie = new Trie(allKeywords);

    public static void calculate(String codeFragmentString, int fragLinesCount, PrintWriter out) {
        HashMap<String, Integer> counts = KeywordsCalculator.keywordsTrie.calculate(codeFragmentString);

        printLn("---KEYWORDS---", out);
        for (String keyword: KeywordsCalculator.allKeywords) {
            Integer count = counts.get(keyword);
            if (count == null) {
                count = 0;
            }

            printLn(keyword + ": " + count, out);
            printLn(keyword + " AVG: " + (double)count / fragLinesCount, out);
            CSVBuilder.shared.addStr(count);
            CSVBuilder.shared.addStr((double)count / fragLinesCount);
        }
    }

    public static void calculateCSV(String codeFragmentString, int fragLinesCount) {
        HashMap<String, Integer> counts = KeywordsCalculator.keywordsTrie.calculate(codeFragmentString);

        for (String keyword: KeywordsCalculator.allKeywords) {
            Integer count = counts.get(keyword);
            if (count == null) {
                count = 0;
            }

            CSVBuilder.shared.addStr(count);
            CSVBuilder.shared.addStr((double)count / fragLinesCount);
        }
    }
}
