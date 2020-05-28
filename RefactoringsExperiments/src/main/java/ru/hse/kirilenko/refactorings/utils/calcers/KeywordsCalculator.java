package ru.hse.kirilenko.refactorings.utils.calcers;

import ru.hse.kirilenko.refactorings.csv.SparseCSVBuilder;
import ru.hse.kirilenko.refactorings.csv.models.CSVItem;
import ru.hse.kirilenko.refactorings.csv.models.Feature;
import ru.hse.kirilenko.refactorings.utils.trie.Trie;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class KeywordsCalculator {
    public static List<String> allKeywords = Arrays.asList(
            "abstract", "continue", "for", "new", "switch", "assert",
            "default", "package", "synchronized", "boolean", "do", "if",
            "private", "this", "break", "double", "implements", "protected",
            "throw", "byte", "else", "import", "public", "throws", "case",
            "enum", "instanceof", "return", "transient", "catch", "extends",
            "int", "short", "try", "char", "final", "interface", "static",
            "void", "class", "finally", "long", "strictfp", "volatile",
            "const", "float", "native", "super", "while");

    public static Trie keywordsTrie = new Trie(allKeywords);

    public static void calculateCSV(String codeFragmentString, int fragLinesCount) {
        HashMap<String, Integer> counts = KeywordsCalculator.keywordsTrie.calculate(codeFragmentString);

        int id = 6;
        for (String keyword: KeywordsCalculator.allKeywords) {
            Integer count = counts.get(keyword);
            if (count == null) {
                count = 0;
            }

            SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.fromId(id), count));
            SparseCSVBuilder.sharedInstance.addFeature(new CSVItem(Feature.fromId(id + 1), (double)count / fragLinesCount));
            id += 2;
        }
    }
}
