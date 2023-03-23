package org.jetbrains.research.anticopypaster.ide.ui;

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
import org.jetbrains.research.anticopypaster.ide.DuplicatesInspection;
import org.jetbrains.research.anticopypaster.ide.RefactoringEvent;
import org.jetbrains.research.anticopypaster.ide.RefactoringNotificationTask;
import org.jetbrains.research.anticopypaster.statistics.AntiCopyPasterUsageStatistics;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Timer;

import static org.jetbrains.research.anticopypaster.utils.PsiUtil.findMethodByOffset;
import static org.jetbrains.research.anticopypaster.utils.PsiUtil.getCountOfCodeLines;

/**
 * Handles any copy-paste action and checks if the pasted code fragment could be extracted into a separate method.
 */
public class CustomMetricsMenuPreProcessor {
    private final DuplicatesInspection inspection = new DuplicatesInspection();
    private final Timer timer = new Timer(true);
    private final RefactoringNotificationTask refactoringNotificationTask = new RefactoringNotificationTask(inspection, timer);

    private static final Logger LOG = Logger.getInstance(CustomMetricsMenuPreProcessor.class);

    public CustomMetricsMenuPreProcessor() {

    }

    /**
     * Triggers on each popup.
     */
    @Nullable
    public String preprocessInputs(Project project, PsiFile file, Editor editor, String text, RawText rawText) {
        HashSet<String> variablesInCodeFragment = new HashSet<>();
        HashMap<String, Integer> variablesCountsInCodeFragment = new HashMap<>();

        CustomMetricsMenu.getInstance(project);

        if (editor == null || file == null || !FragmentCorrectnessChecker.isCorrect(project, file,
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
}