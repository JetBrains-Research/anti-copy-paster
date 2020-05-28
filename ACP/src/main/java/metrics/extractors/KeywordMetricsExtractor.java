package metrics.extractors;

import metrics.utils.Trie;
import models.features.feature.Feature;
import models.features.feature.FeatureItem;
import models.features.features_vector.IFeaturesVector;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class KeywordMetricsExtractor {
    private static final List<String> allKeywords = Arrays.asList(
            "abstract", "continue", "for", "new", "switch", "assert",
            "default", "package", "synchronized", "boolean", "do", "if",
            "private", "this", "break", "double", "implements", "protected",
            "throw", "byte", "else", "import", "public", "throws", "case",
            "enum", "instanceof", "return", "transient", "catch", "extends",
            "int", "short", "try", "char", "final", "interface", "static",
            "void", "class", "finally", "long", "strictfp", "volatile",
            "const", "float", "native", "super", "while");

    private static Trie keywordsTrie = new Trie(allKeywords);
    private static final int FIRST_METRIC_ID = 6;

    public static void calculate(final String codeFragment, int linesCount, IFeaturesVector fVec) {
        HashMap<String, Integer> counts = KeywordMetricsExtractor.keywordsTrie.calculate(codeFragment);

        int id = FIRST_METRIC_ID;
        for (String keyword: KeywordMetricsExtractor.allKeywords) {
            Integer count = counts.getOrDefault(keyword, 0);
            fVec.addFeature(new FeatureItem(Feature.fromId(id++), count));
            fVec.addFeature(new FeatureItem(Feature.fromId(id++), (double)count / linesCount));
        }
    }
}