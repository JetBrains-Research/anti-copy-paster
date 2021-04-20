package org.jetbrains.research.anticopypaster.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public final class DuplicatesInspection {
    private static ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);

    public InspectionResult resolve(PsiFile file, final String code) {
        final ArrayList<Future<DuplicateResult>> tasks = new ArrayList<>();
        final List<String> tokensOfPastedCode = getTokens(code);
        List<DuplicateResult> results = new ArrayList<>();
        @NotNull Collection<PsiMethod> methods = PsiTreeUtil.findChildrenOfType(file, PsiMethod.class);
        for (PsiMethod psiMethod : methods) {
            tasks.add(
                pool.submit(() -> ApplicationManager.getApplication().runReadAction(new Computable<DuplicateResult>() {
                    @Override
                    public DuplicateResult compute() {
                        PsiCodeBlock methodBody = psiMethod.getBody();
                        if (methodBody != null) {
                            // Calculate the Jaccard similarity
                            List<String> tokensOfMethod = getTokens(methodBody.getText());
                            double sumOfTokens = tokensOfPastedCode.size() + tokensOfMethod.size();
                            tokensOfMethod.retainAll(tokensOfPastedCode);
                            double threshold = tokensOfMethod.size() / sumOfTokens;
                            if (threshold >= 0.7) {
                                DuplicateResult duplicateResult = new DuplicateResult(psiMethod, threshold);
                                results.add(duplicateResult);
                                return duplicateResult;
                            }
                        }
                        return null;
                    }
                })));
        }

        return new InspectionResult(results);
    }

    public static class DuplicateResult {
        public PsiMethod method;
        public double threshold;

        public DuplicateResult(PsiMethod method, double threshold) {
            this.method = method;
            this.threshold = threshold;
        }
    }

    public static class InspectionResult {
        public int count;
        public List<DuplicateResult> results;

        public InspectionResult(List<DuplicateResult> results) {
            this.count = results.size();
            this.results = results;
        }
    }

    private List<String> getTokens(String text) {
        return StringUtil.getWordsIn(text);
    }
}
