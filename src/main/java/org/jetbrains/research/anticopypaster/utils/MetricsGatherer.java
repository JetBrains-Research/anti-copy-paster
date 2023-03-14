package org.jetbrains.research.anticopypaster.utils;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;

import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.research.extractMethod.metrics.MetricCalculator;
import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This class is used to gather metrics from every method within the currently
 * open IntelliJ Project. If multiple IntellIJ Projects are currently in use,
 * only the first project will be scoured.
 */
public class MetricsGatherer {
    /**
     * A list of all the FeaturesVectors for all methods within
     * the IntelliJ Project.
     */
    private List<FeaturesVector> methodsMetrics;

    /**
     * Builds an instance of the MetricsGatherer and
     * does the initial (and only) metrics gathering.
     */
    public MetricsGatherer(){
        this.methodsMetrics = new ArrayList<>();
        gatherMetrics();
    }

    /**
     * Getter for the methodsMetrics.
     * @return the list of featuresVectors made by the gatherer.
     */
    public List<FeaturesVector> getMethodsMetrics(){
        return this.methodsMetrics;
    }
    /**
     * Gathers all the metrics from every method within the IntelliJ Project.
     */
    private void gatherMetrics(){
        // Gets the first currently opened project
        Project project = ProjectManager.getInstance().getOpenProjects()[0];

        // Gets all Java files from the Project
        Collection<VirtualFile> vfCollection = FileTypeIndex.getFiles(
                JavaFileType.INSTANCE,
                GlobalSearchScope.projectScope(project));

        // Gets a PsiFile for every Java file in the project
        List<PsiFile> pfList = new ArrayList<>();
        for (VirtualFile file : vfCollection) {
            // Makes everything lowercase for consistency, and gets rid of file extension
            String filename = file.getName().toLowerCase().split("[.]")[0];
            if (!filename.startsWith("test") && !filename.endsWith("test")) {
                pfList.add(PsiManager.getInstance(project).findFile(file));
            }
        }

        // Gets all the PsiMethods, as well as their start and end lines.
        for(PsiFile psiFile: pfList){
            // wrappers are used to get information out of runReadActions.
            // PsiTree's can't be accessed outside a read action, or it
            // can cause race conditions.
            var psiMethodWrapper = new Object(){ Collection<PsiMethod> psiMethods = null; };
            ApplicationManager.getApplication().runReadAction(() -> {
                psiMethodWrapper.psiMethods = PsiTreeUtil.findChildrenOfType(psiFile, PsiMethod.class);
            });

            Collection<PsiMethod> psiMethods = psiMethodWrapper.psiMethods;
            for(PsiMethod method: psiMethods) {
                int startLine = PsiUtil.getNumberOfLine(psiFile, method.getTextRange().getStartOffset());
                int endLine = PsiUtil.getNumberOfLine(psiFile, method.getTextRange().getEndOffset());

                var fvWrapper = new Object(){ FeaturesVector features = null; };
                ApplicationManager.getApplication().runReadAction(() -> {
                    fvWrapper.features = new
                            MetricCalculator(method, method.getText(), startLine, endLine).getFeaturesVector();
                });
                FeaturesVector features = fvWrapper.features;
                this.methodsMetrics.add(features);
            }

        }
    }
}
