import com.intellij.codeInspection.*;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestFactory;
import com.intellij.diff.requests.ContentDiffRequest;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.UndoConfirmationPolicy;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.ReadonlyStatusHandler;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Example of an inspection, which finds a class with the same fully qualified name. The files can be diffed or deleted.
 *
 * @author markiewb
 */
/*public class FindDuplicatesInspectionVisitor extends PsiElementVisitor {
    @Override
    public void visitReferenceExpression(PsiReferenceExpression psiReferenceExpression) {

    }

    private final ProblemsHolder holder;

    public FindDuplicatesInspectionVisitor(ProblemsHolder holder) {

        this.holder = holder;
    }

    @Override
    public void visitClass(PsiClass aClass) {
        super.visitClass(aClass);

        String fqn = aClass.getQualifiedName();

        if (null != fqn) {

            Project[] projects = ProjectManager.getInstance().getOpenProjects();
            for (Project project : projects) {

                PsiClass[] classes = null;//JavaPsiFacade.getInstance(project).findClasses(fqn, GlobalSearchScope.allScope(project));
                if (classes.length >= 2) {
                    List<PsiClass> psiClasses = new ArrayList<>(Arrays.asList(classes));
                    //do not diff itself
                    psiClasses.remove(aClass);

                    for (PsiClass psiClass : psiClasses) {

                        VirtualFile file1 = aClass.getContainingFile().getVirtualFile();
                        VirtualFile file2 = psiClass.getContainingFile().getVirtualFile();

                        List<LocalQuickFix> fixes = new ArrayList<>();
                        fixes.add(new ShowDiffFix(file1, file2));

                        if (file1.isWritable()) {
                            fixes.add(new RemoveFix(file1));
                        }
                        holder.registerProblem(aClass, String.format("Found: %s", file2.getPresentableUrl()), fixes.toArray(new LocalQuickFix[]{}));
                    }
                }
            }

        }

    }

    @Override
    public void visitReferenceExpression(PsiReferenceExpression psiReferenceExpression) {

    }

    private static class ShowDiffFix implements LocalQuickFix {
        VirtualFile file1, file2;

        public ShowDiffFix(VirtualFile file1, VirtualFile file2) {
            this.file1 = file1;
            this.file2 = file2;
        }

        @NotNull
        public String getName() {
            // The test (see the TestThisPlugin class) uses this string to identify the quick fix action.
            return "Diff...";
        }

        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {

            ContentDiffRequest req = DiffRequestFactory.getInstance().createFromFiles(project, file1, file2);
            DiffManager.getInstance().showDiff(project, req);

        }

        @NotNull
        public String getFamilyName() {
            return getName();
        }
    }

    private static class RemoveFix implements LocalQuickFix {
        VirtualFile file;

        public RemoveFix(VirtualFile file) {
            this.file = file;
        }

        @NotNull
        public String getName() {
            return String.format("Remove local '%s'", file.getPresentableName());
        }

        public void applyFix(@NotNull final Project project, @NotNull ProblemDescriptor descriptor) {

            //taken from
            ReadonlyStatusHandler.getInstance(project).ensureFilesWritable(file);

            ApplicationManager.getApplication().runWriteAction(new Runnable() {
                @Override
                public void run() {
                    CommandProcessor.getInstance().executeCommand(project, new Runnable() {
                        @Override
                        public void run() {
                            try {
                                file.delete(this);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }, getName(), "???", UndoConfirmationPolicy.REQUEST_CONFIRMATION);
                }
            });

        }

        @NotNull
        public String getFamilyName() {
            return getName();
        }
    }

}*/