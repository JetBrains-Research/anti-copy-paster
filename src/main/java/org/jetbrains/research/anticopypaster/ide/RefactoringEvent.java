package org.jetbrains.research.anticopypaster.ide;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.research.anticopypaster.models.features.features_vector.IFeaturesVector;

/**
 * Contains information about the code fragment that is recommended for extraction into a separate method.
 */
public class RefactoringEvent {
    public PsiFile file;
    public String text;
    public int matches;
    public IFeaturesVector vec;
    public Project project;
    public Editor editor;
    public double predBoost;
    public int linesOfCode;
    public boolean forceExtraction;
    public String reasonToExtract;

    public RefactoringEvent(PsiFile file, String text, int matches, IFeaturesVector vec, Project project, Editor editor,
                             double predBoost, int linesOfCode, boolean forceExtraction, String reasonToExtract) {
        this.file = file;
        this.text = text;
        this.matches = matches;
        this.vec = vec;
        this.project = project;
        this.editor = editor;
        this.predBoost = predBoost;
        this.linesOfCode = linesOfCode;
        this.forceExtraction = forceExtraction;
        this.reasonToExtract = reasonToExtract;
    }
}