package org.jetbrains.research.anticopypaster.ide;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;

/**
 * Contains information about the code fragment that is recommended for extraction into a separate method.
 */
public class RefactoringEvent {
    private final PsiFile file;
    private final PsiMethod destinationMethod;
    private final String text;
    private final int matches;
    private final Project project;
    private final Editor editor;
    private final int linesOfCode;
    private boolean forceExtraction = false;
    private String reasonToExtract;

    public RefactoringEvent(PsiFile file, PsiMethod destinationMethod, String text, int matches,
                            Project project,
                            Editor editor,
                            int linesOfCode
    ) {
        this.file = file;
        this.destinationMethod = destinationMethod;
        this.text = text;
        this.matches = matches;
        this.project = project;
        this.editor = editor;
        this.linesOfCode = linesOfCode;
    }

    public void setForceExtraction(boolean forceExtraction) {
        this.forceExtraction = forceExtraction;
    }

    public void setReasonToExtract(String message) {
        this.reasonToExtract = message;
    }

    public boolean isForceExtraction() {
        return forceExtraction;
    }

    public String getReasonToExtract() {
        return reasonToExtract;
    }

    public PsiFile getFile() {
        return file;
    }

    public PsiMethod getDestinationMethod() {
        return destinationMethod;
    }

    public String getText() {
        return text;
    }

    public int getMatches() {
        return matches;
    }

    public Project getProject() {
        return project;
    }

    public Editor getEditor() {
        return editor;
    }

    public int getLinesOfCode() {
        return linesOfCode;
    }
}