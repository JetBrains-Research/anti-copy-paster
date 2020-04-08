package checkers;

import com.intellij.lang.FileASTNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.source.tree.PsiErrorElementImpl;
import com.intellij.util.IncorrectOperationException;

public class FragmentCorrectnessChecker {
    private static String wrapperFormat = "class Tmp {\n" +
            "    public static void main(String[] args) {\n" +
            "        %s\n" +
            "    }\n" +
            "}";

    public static boolean isCorrect(Project project, PsiFile file, String fragment) {
        String wrappedFragment = String.format(wrapperFormat, fragment);
        PsiFile tmp = null;
        try {
            tmp = PsiFileFactory.getInstance(project)
                    .createFileFromText(file.getFileType(),
                            "tmp.txt",
                            wrappedFragment,
                            0,
                            wrappedFragment.length());
        } catch (IncorrectOperationException e) {
            return false;
        }
        return traverse(tmp);
    }

    private static boolean traverse(PsiElement node) {
        if (node instanceof PsiErrorElementImpl) {
            return false;
        }

        PsiElement[] children = node.getChildren();

        for (PsiElement child: children) {
            if (!traverse(child)) {
                return false;
            }
        }

        return true;
    }
}
