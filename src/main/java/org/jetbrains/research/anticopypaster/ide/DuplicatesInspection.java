package org.jetbrains.research.anticopypaster.ide;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.util.PsiTreeUtil;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public final class DuplicatesInspection {
    private static ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);

    private static final Logger LOG = Logger.getInstance(AntiCopyPastePreProcessor.class);

    /**
     * Searches for duplicates in methods extracted from the file.
     * First, it checks if a method contains the copy-pasted piece of code as a substring,
     * and if doesn't then it collects the bags of words of a method and a piece of code and calculates their
     * similarity.
     *
     * @param file to search duplicates in.
     * @param code the piece of code to search for.
     * @return the result of duplicates' detection.
     */
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
                            String rawCode =
                                code.replace('\n', ' ').replace('\t', ' ')
                                    .replace('\r', ' ').replaceAll("\\s+", "");
                            String rawMethodBody = psiMethod.getText().replace('\n', ' ').replace('\t', ' ')
                                .replace('\r', ' ').replaceAll("\\s+", "");
                            boolean matches = StringUtils.contains(rawMethodBody, rawCode);
                            if (matches) {
                                duplicateResult = new DuplicateResult(psiMethod, 1.0);
                            } else {
                                List<String> tokensOfMethod = getTokens(methodBody.getText());
                                double maxNumOfTokens = Math.max(tokensOfPastedCode.size(), tokensOfMethod.size());
                                // Calculates the intersection of tokens
                                tokensOfMethod.retainAll(tokensOfPastedCode);
                                double threshold = tokensOfMethod.size() / maxNumOfTokens;
                                if (threshold >= 0.8) {
                                    duplicateResult = new DuplicateResult(psiMethod, threshold);
                                }
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
