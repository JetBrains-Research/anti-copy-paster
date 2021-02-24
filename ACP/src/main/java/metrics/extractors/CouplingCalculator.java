package metrics.extractors;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import metrics.models.MemberSets;
import models.features.feature.Feature;
import models.features.feature.FeatureItem;
import models.features.features_vector.IFeaturesVector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Calculates the coupling of the copy-pasted code fragment with the enclosing class.
 */
public class CouplingCalculator {

    public static void calculate(PsiFile file,
                                 String codeFragment,
                                 int linesCount,
                                 IFeaturesVector featuresVector) {
        MemberSets memberSets = extractAllMethodsAndFields(file);

        int fieldMatches = 0;
        int methodMatches = 0;
        int totalMatches;

        PsiFileFactory factory = PsiFileFactory.getInstance(file.getProject());
        @Nullable PsiFile psiFromText = factory.createFileFromText(codeFragment, file);

        // search for all identifiers (methods and variables) in the code fragment
        @NotNull Collection<PsiIdentifier> identifiers = PsiTreeUtil.collectElementsOfType(psiFromText,
                                                                                          PsiIdentifier.class);
        ArrayList<String> identifiersNames = new ArrayList<>();
        identifiers.forEach(i -> identifiersNames.add(i.getText()));

        for (String fieldName: memberSets.fields) {
            if (identifiersNames.contains(fieldName)) {
                fieldMatches += 1;
            }
        }

        for (String methodName: memberSets.methods) {
            if (identifiersNames.contains(methodName)) {
                methodMatches += 1;
            }
        }

        totalMatches = methodMatches + fieldMatches;

        featuresVector.addFeature(new FeatureItem(Feature.TotalConnectivity, totalMatches));
        featuresVector.addFeature(new FeatureItem(Feature.TotalConnectivityPerLine,
                                                  (double) totalMatches / linesCount));
        featuresVector.addFeature(new FeatureItem(Feature.FieldConnectivity, fieldMatches));
        featuresVector.addFeature(new FeatureItem(Feature.FieldConnectivityPerLine,
                                                  (double) fieldMatches / linesCount));
        featuresVector.addFeature(new FeatureItem(Feature.MethodConnectivity, methodMatches));
        featuresVector.addFeature(new FeatureItem(Feature.MethodConnectivityPerLine,
                                                  (double) methodMatches / linesCount));
    }

    private static MemberSets extractAllMethodsAndFields(PsiElement root) {
        final MemberSets result = new MemberSets();
        root.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
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
