package metrics.extractors;

import com.intellij.lang.FileASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import metrics.models.MemberSets;
import models.features.feature.Feature;
import models.features.feature.FeatureItem;
import models.features.features_vector.IFeaturesVector;
import org.apache.commons.lang3.StringUtils;

public class ConnectivityExtractor {

    public static void calculate(PsiElement file, String codeFragment, int linesCount, String methodName, IFeaturesVector featuresVector) {
        MemberSets memberSets = findFieldsDeclarations(file);

        int fieldMatches = 0;
        int methodMatches = 0;
        int totalMatches;
        for (String field: memberSets.fields) {
            fieldMatches += StringUtils.countMatches(codeFragment, field);
        }

        for (String method: memberSets.methods) {
            methodMatches += StringUtils.countMatches(codeFragment, method);
        }

        totalMatches = methodMatches + fieldMatches;

        featuresVector.addFeature(new FeatureItem(Feature.TotalConnectivity, totalMatches));
        featuresVector.addFeature(new FeatureItem(Feature.TotalConnectivityPerLine, (double)totalMatches / linesCount));
        featuresVector.addFeature(new FeatureItem(Feature.FieldConnectivity, fieldMatches));
        featuresVector.addFeature(new FeatureItem(Feature.FieldConnectivityPerLine, (double)fieldMatches / linesCount));
        featuresVector.addFeature(new FeatureItem(Feature.MethodConnectivity, methodMatches));
        featuresVector.addFeature(new FeatureItem(Feature.MethodConnectivityPerLine, (double)methodMatches / linesCount));
    }

    private static MemberSets findFieldsDeclarations(PsiElement root) {
        final MemberSets result = new MemberSets();
        root.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                super.visitElement(element);

                String[] elementSplit = element.toString().split(":");
                if (elementSplit[0].equals("PsiMethod")) {
                    String methodName = elementSplit[1];
                    result.methods.add(methodName);
                    result.total.add(methodName);
                }

                if (elementSplit[0].equals("PsiField")) {
                    String fieldName = elementSplit[1];
                    result.fields.add(fieldName);
                    result.total.add(fieldName);
                }

            }
        });

        return result;
    }
}
