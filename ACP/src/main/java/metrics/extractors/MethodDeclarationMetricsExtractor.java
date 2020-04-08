package metrics.extractors;

import com.intellij.lang.FileASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.source.tree.PsiErrorElementImpl;
import models.features.feature.Feature;
import models.features.feature.FeatureItem;
import models.features.features_vector.IFeaturesVector;
import org.apache.commons.lang3.StringUtils;

public class MethodDeclarationMetricsExtractor {
    public static void calculate(Project project, PsiFile file, String fragment, IFeaturesVector vec) {
        traverse(file, fragment, vec);
    }

    private static boolean traverse(PsiElement node, String text, IFeaturesVector vec) {
        String nodeType = node.toString();

        if (!(nodeType.contains("PsiJavaFile") || nodeType.contains("PsiClass"))) {
            if (node.toString().contains("PsiMethod")) {
                String nodeText = node.getText();
                if (nodeText.contains(text)) {
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
                if (traverse(child, text, vec)) {
                    return true;
                }
            }
        }



        return false;
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
}
