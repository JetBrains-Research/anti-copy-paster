package org.jetbrains.research.anticopypaster.checkers;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.impl.source.tree.PsiErrorElementImpl;
import com.intellij.util.IncorrectOperationException;

import java.util.HashMap;
import java.util.HashSet;

public class FragmentCorrectnessChecker {
    private static final String wrapperFormat = "class Tmp {\n" +
        "    public static void main(String[] args) {\n" +
        "        %s\n" +
        "    }\n" +
        "}";

    public static boolean isCorrect(Project project,
                                    PsiFile file,
                                    String fragment,
                                    HashSet<String> vars_in_fragment,
                                    HashMap<String, Integer> vars_counts_in_fragment) {
        String wrappedFragment = String.format(wrapperFormat, fragment);
        PsiFile tmp;
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

        return traverse(tmp, false, vars_in_fragment, vars_counts_in_fragment);
    }

    private static boolean traverse(PsiElement node,
                                    boolean inside,
                                    HashSet<String> vars_in_fragment,
                                    HashMap<String, Integer> vars_counts_in_fragment) {
        boolean result = !(node instanceof PsiErrorElementImpl);

        String nodeText = node.toString();

        if (inside) {
            if (nodeText.contains("PsiLocalVariable")) {
                String var = nodeText.split(":")[1];
                vars_in_fragment.add(var);
            }

            if (nodeText.contains("PsiIdentifier")) {
                String var = nodeText.split(":")[1];
                vars_counts_in_fragment.put(var, vars_counts_in_fragment.getOrDefault(var, 0) + 1);
            }
        }

        PsiElement[] children = node.getChildren();

        for (PsiElement child : children) {
            result &=
                traverse(child, nodeText.contains("PsiMethod") || inside, vars_in_fragment, vars_counts_in_fragment);
        }

        return result;
    }
}
