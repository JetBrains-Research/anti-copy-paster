package org.jetbrains.research.anticopypaster.utils;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.research.anticopypaster.ide.AntiCopyPastePreProcessor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public final class DuplicatesInspection {
    private static ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);

    private static final Logger LOG = Logger.getInstance(AntiCopyPastePreProcessor.class);

    public InspectionResult resolve(PsiFile file, final String code) {
        final ArrayList<Future<DuplicateResult>> tasks = new ArrayList<>();
        final List<String> tokensOfPastedCode = getTokens(code);
        @NotNull Collection<PsiMethod> methods = PsiTreeUtil.findChildrenOfType(file, PsiMethod.class);
        for (PsiMethod psiMethod : methods) {
            tasks.add(
                pool.submit(() -> ApplicationManager.getApplication().runReadAction(new Computable<DuplicateResult>() {
                    @Override
                    public DuplicateResult compute() {
                        DuplicateResult duplicateResult = null;
                        PsiCodeBlock methodBody = psiMethod.getBody();
                        if (methodBody != null) {
                            // Calculate the Jaccard similarity
                            List<String> tokensOfMethod = getTokens(methodBody.getText());
                            double maxNumOfTokens = Math.max(tokensOfPastedCode.size(), tokensOfMethod.size());
                            // Calculates the intersection of tokens
                            tokensOfMethod.retainAll(tokensOfPastedCode);
                            double threshold = tokensOfMethod.size() / maxNumOfTokens;
                            if (threshold > 0.6) {
                                duplicateResult = new DuplicateResult(psiMethod, threshold);
                            }
                        }
                        return duplicateResult;
                    }
                })));
        }

        ArrayList<DuplicateResult> results = new ArrayList<>();
        for (Future<DuplicateResult> future : tasks) {
            try {
                DuplicateResult result = future.get();
                if (result != null)
                    results.add(result);
            } catch (Exception e) {
                LOG.warn("[ACP] Failed while searching for code duplicates.", e);
            }
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
        private final ArrayList<DuplicateResult> results;

        public InspectionResult(ArrayList<DuplicateResult> results) {
            this.results = results;
        }

        public int getDuplicatesCount() {
            return this.results.size();
        }
    }

    private List<String> getTokens(String text) {
        return StringUtil.getWordsIn(text);
    }
}
