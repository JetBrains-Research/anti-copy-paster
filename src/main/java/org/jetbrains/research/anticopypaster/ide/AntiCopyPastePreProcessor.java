package org.jetbrains.research.anticopypaster.ide;

import com.intellij.codeInsight.editorActions.CopyPastePreProcessor;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RawText;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.research.anticopypaster.checkers.FragmentCorrectnessChecker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;

import static org.jetbrains.research.anticopypaster.utils.PsiUtil.findMethodByOffset;
import static org.jetbrains.research.anticopypaster.utils.PsiUtil.getCountOfCodeLines;

/**
 * Handles any copy-paste action and checks if the pasted code fragment could be extracted into a separate method.
 */
public class AntiCopyPastePreProcessor implements CopyPastePreProcessor {
    private final DuplicatesInspection inspection = new DuplicatesInspection();
    private final RefactoringNotificationTask refactoringNotificationTask = new RefactoringNotificationTask();

    private static final Logger LOG = Logger.getInstance(AntiCopyPastePreProcessor.class);

    public AntiCopyPastePreProcessor() {
        setCheckingForRefactoringOpportunities();
    }

    /**
     * Triggers on each copy action.
     */
    @Nullable
    @Override
    public String preprocessOnCopy(PsiFile file, int[] startOffsets, int[] endOffsets, String text) {
        return null;
    }

    /**
     * Triggers on each paste action to search for duplicates and check the Extract Method refactoring opportunities
     * for a copied-pasted code fragment.
     */
    @NotNull
    @Override
    public String preprocessOnPaste(Project project, PsiFile file, Editor editor, String text, RawText rawText) {
        HashSet<String> variablesInCodeFragment = new HashSet<>();
        HashMap<String, Integer> variablesCountsInCodeFragment = new HashMap<>();

        if (project == null || editor == null || file == null ||
                !FragmentCorrectnessChecker.isCorrect(project, file,
                        text,
                        variablesInCodeFragment,
                        variablesCountsInCodeFragment)) {
            return text;
        }

        @Nullable Caret caret = CommonDataKeys.CARET.getData(DataManager.getInstance().getDataContext());
        int offset = caret == null ? 0 : caret.getOffset();
        PsiMethod destinationMethod = findMethodByOffset(file, offset);

        // find number of code fragments considered as duplicated
        DuplicatesInspection.InspectionResult result = inspection.resolve(file, text);
        if (result.getDuplicatesCount() == 0) {
            return text;
        }

        //number of lines in fragment
        int linesOfCode = getCountOfCodeLines(text);

        refactoringNotificationTask.addEvent(
                new RefactoringEvent(file, destinationMethod, text, result.getDuplicatesCount(),
                        project, editor, linesOfCode));

        return text;
    }

    /**
     * Sets the regular checking for Extract Method refactoring opportunities.
     */
    private void setCheckingForRefactoringOpportunities() {
        try {
            Timer timer = new Timer();
            timer.schedule(refactoringNotificationTask, 15000, 15000);
        } catch (Exception ex) {
            LOG.error("[ACP] Failed to schedule the checking for refactorings.", ex.getMessage());
        }
    }

}