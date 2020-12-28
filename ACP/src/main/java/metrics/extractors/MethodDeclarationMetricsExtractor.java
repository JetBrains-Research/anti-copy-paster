package metrics.extractors;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import models.features.feature.Feature;
import models.features.feature.FeatureItem;
import models.features.features_vector.IFeaturesVector;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MethodDeclarationMetricsExtractor {
    public static class ParamsScores {
        public int in, out;
        public int method_lines;
        public int max_dep, all_dep, max_rest, all_rest;
        public boolean isSet = false;
    }

    public static ParamsScores calculate(PsiFile file, String fragment, IFeaturesVector vec, HashSet<String> vars_in_fragment, HashMap<String, Integer> vars_counts_in_fragment) {
        ParamsScores scores = new ParamsScores();

        traverse(file, fragment, vec, vars_in_fragment, vars_counts_in_fragment, scores);

        return scores;

    }

    private static boolean traverse(PsiElement node, String text, IFeaturesVector vec, HashSet<String> vars_in_fragment, HashMap<String, Integer> vars_counts_in_fragment, ParamsScores scores) {
        String nodeType = node.toString();

        if (!(nodeType.contains("PsiJavaFile") || nodeType.contains("PsiClass"))) {
            if (node.toString().contains("PsiMethod")) {
                String nodeText = node.getText();
                if (nodeText.contains(text)) {
                    List<String> params = new ArrayList<>();

                    String nodeBody = node.getText();
                    int rawLocs = StringUtils.countMatches(nodeBody, "\n") + 1;
                    int cleanedLocs = cleanedLocs(nodeText, rawLocs);

                    PsiElement[] children = node.getChildren();
                    for (PsiElement el: children) {
                        if (el.toString().contains("PsiParameterList")) {

                            try {
                                params = Arrays.stream(el.toString().split("\\(")[1].split("\\)")[0].split(",")).map(s -> s.split(" ")[s.split(" ").length - 1]).collect(Collectors.toList());
                            } catch (Exception e) {
                                // skip
                            }

                            try {
                                params = Arrays.stream(el.toString().split("\\(")[1].split("\\)")[0].split(",")).map(s -> s.split(" ")[s.split(" ").length - 1]).collect(Collectors.toList());
                            } catch (Exception e) {
                                // skip
                            }
                        }
                    }

                    HashSet<String> vars_in_method = new HashSet<>();
                    HashMap<String, Integer> vars_counts_in_method = new HashMap<>();

                    dfsMethod(node, text, vars_in_method, vars_counts_in_method);

                    ArrayList<String> to_remove = new ArrayList<>();

                    for (Map.Entry<String, Integer> entry : vars_counts_in_fragment.entrySet()) {
                        if (!vars_in_method.contains(entry.getKey())) {
                            to_remove.add(entry.getKey());

                        }
                    }

                    for (String k: to_remove) {
                        vars_counts_in_method.remove(k);
                    }

                    for (String decl: vars_in_fragment) {
                        vars_counts_in_fragment.put(decl, vars_counts_in_fragment.getOrDefault(decl, 1) - 1);
                    }

                    for (String decl: vars_in_method) {
                        vars_counts_in_method.put(decl, vars_counts_in_method.getOrDefault(decl, 1) - 1);
                    }


                    HashSet<String> vars_in_method_not_fragment = new HashSet<>(vars_in_method); // copy
                    vars_in_method_not_fragment.removeAll(vars_in_fragment);

                    HashMap<String, Integer> vars_counts_in_method_not_fragment = new HashMap<>(vars_counts_in_method); // copy

                    for (Map.Entry<String, Integer> entry:  vars_counts_in_method_not_fragment.entrySet()) {
                        vars_counts_in_method_not_fragment.replace(entry.getKey(), vars_counts_in_method_not_fragment.getOrDefault(entry.getKey(), 0) - vars_counts_in_fragment.getOrDefault(entry.getKey(), 0));

                    }


                    vars_counts_in_fragment.values().removeIf(f -> f <= 0);
                    vars_counts_in_method.values().removeIf(f -> f <= 0);
                    vars_counts_in_method_not_fragment.values().removeIf(f -> f <= 0);

                    int input_params_count = 0;
                    int output_params_count = 0;

                    HashSet<String> method_args = new HashSet<>(params);

                    for (String key: vars_counts_in_fragment.keySet()) {
                        if (vars_in_method_not_fragment.contains(key) || method_args.contains(key)) {
                            input_params_count++;
                        }
                    }

                    for (String key: vars_counts_in_method_not_fragment.keySet()) {
                        if (vars_in_fragment.contains(key)) {
                            output_params_count++;
                        }
                    }

                    scores.in = input_params_count;
                    scores.out = output_params_count;
                    scores.method_lines = cleanedLocs;
                    scores.all_dep = totalDepth(nodeBody);
                    scores.max_dep = maxDepth(nodeBody);
                    String rest = nodeBody.replace(text, "");
                    scores.max_rest = maxDepth(rest);
                    scores.all_rest = totalDepth(rest);
                    scores.isSet = true;



                    int locs = StringUtils.countMatches(nodeText, "\n");
                    int dep = totalDepth(text);
                    vec.addFeature(new FeatureItem(Feature.MethodDeclarationSymbols, StringUtils.countMatches(nodeText, "\n")));
                    vec.addFeature(new FeatureItem(Feature.MethodDeclarationAverageSymbols, (double)nodeText.length() / locs));
                    vec.addFeature(new FeatureItem(Feature.MethodDeclarationDepth, dep));
                    vec.addFeature(new FeatureItem(Feature.MethodDeclarationDepthPerLine, (double)dep / locs));

                    return true;
                }
            }

            return false;
        } else {
            PsiElement[] children = node.getChildren();
            for (PsiElement child: children) {
                if (traverse(child, text, vec, vars_in_fragment, vars_counts_in_fragment, scores)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void dfsMethod(PsiElement node, String text, HashSet<String> vars, HashMap<String, Integer> vars_counts) {
        String nodeText = node.toString();
        if (nodeText.contains("PsiLocalVariable")) {
            String var = nodeText.split(":")[1];
            vars.add(var);
        }

        if (nodeText.contains("PsiIdentifier")) {
            String var = nodeText.split(":")[1];
            vars_counts.put(var, vars_counts.getOrDefault(var, 0) + 1);
        }

        PsiElement[] children = node.getChildren();
        for (PsiElement child: children) {
            dfsMethod(child, text, vars, vars_counts);
        }
    }

    public static int totalDepth(String code) {
        int dep = 0;
        int area = 0;
        int depInLine = 0;
        for (Character ch: code.toCharArray()) {
            if (ch == '{') {
                dep++;
                depInLine++;
            } else if (ch == '}') {
                dep--;
                depInLine--;
            } else if (ch == '\n'){
                int resDep = dep;
                if (depInLine > 0) {
                    resDep--;
                }
                depInLine = 0;
                area += resDep;
            }
        }

        return area;
    }

    public static int maxDepth(String code) {
        int dep = 0;
        int res = 0;
        for (Character ch: code.toCharArray()) {
            if (ch == '{') {
                dep++;
                res = Math.max(dep, res);
            } else if (ch == '}') {
                dep--;
            }
        }

        return res;
    }

    private static  int cleanedLocs(String text, int rawLocs) {
        Pattern p = Pattern.compile("/\\*[\\s\\S]*?\\*/");

        java.util.regex.Matcher m = p.matcher(text);

        int total_with_comment = 0;
        while (m.find()) {

            String lines[] = m.group(0).split("\n");
            total_with_comment += lines.length;
        }

        int total_unused = 0;
        for (String s: text.split("\n")) {
            String tmp = s.trim();
            if (tmp.isEmpty() || tmp.startsWith("//")) {
                total_unused++;
            }

        }

        return Math.max(0, rawLocs - total_unused - total_with_comment);
    }
}
