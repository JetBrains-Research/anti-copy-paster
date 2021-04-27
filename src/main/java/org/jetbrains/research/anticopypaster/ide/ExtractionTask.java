package org.jetbrains.research.anticopypaster.ide;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageRefactoringSupport;
import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.RefactoringActionHandler;

import java.util.List;
import java.util.TimerTask;

import static org.jetbrains.research.anticopypaster.utils.PsiUtil.getStartOffset;

public class ExtractionTask extends TimerTask {
    public Editor editor;
    public Project project;
    public PsiFile file;
    public String text;

    public ExtractionTask(Editor editor, PsiFile file, String text, Project project) {
        this.editor = editor;
        this.project = project;
        this.file = file;
        this.text = text;
    }

    @Override
    public void run() {
        ApplicationManager.getApplication().invokeLater(() -> {
            String fileText = file.getText();

            if (!fileText.contains(text)) {
                return;
            }

            int startOffset = getStartOffset(editor, file, text);

            if (startOffset == -1) {
                return;
            }

            editor.getSelectionModel().setSelection(startOffset, startOffset + text.length());

            Language language = file.getLanguage();
            PsiElement element = file.getOriginalElement();
            List<RefactoringSupportProvider>
                providers = LanguageRefactoringSupport.INSTANCE.allForLanguage(language);

            for (RefactoringSupportProvider provider : providers) {
                if (provider.isAvailable(element)) {
                    RefactoringActionHandler handler = provider.getExtractMethodHandler();
                    DataContext dc = SimpleDataContext.getProjectContext(project);
                    if (handler != null) {
                        handler.invoke(project, editor, file, dc);
                    }
                }
            }
        });
    }
}
