package org.jetbrains.research.anticopypaster.utils;

import com.intellij.codeInsight.CodeInsightUtil;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.introduceVariable.IntroduceVariableBase;
import com.intellij.refactoring.util.RefactoringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PsiUtil {

    private static final Logger LOG = Logger.getInstance(PsiUtil.class);

    /**
     * Check the before revision of the file (without local changes) to find the original method.
     *
     * @param fileWithLocalChanges file that contains the local changes;
     * @param method               method to search for;
     * @return original method.
     */
    public static PsiMethod getMethodStartLineInBeforeRevision(PsiFile fileWithLocalChanges, PsiMethod method) {
        ChangeListManager changeListManager = ChangeListManager.getInstance(fileWithLocalChanges.getProject());
        Change change = changeListManager.getChange(fileWithLocalChanges.getVirtualFile());
        if (change != null) {
            ContentRevision beforeRevision = change.getBeforeRevision();
            if (beforeRevision != null) {
                try {
                    String content = beforeRevision.getContent();
                    if (content != null) {
                        PsiFile psiFileBeforeRevision =
                            PsiFileFactory.getInstance(fileWithLocalChanges.getProject()).createFileFromText("tmp",
                                                                                                             JavaFileType.INSTANCE,
                                                                                                             content);
                        PsiElement[] children = psiFileBeforeRevision.getChildren();
                        for (PsiElement element : children) {
                            if (element instanceof PsiClass) {
                                PsiClass psiClass = (PsiClass) element;
                                PsiMethod[] methods = psiClass.getMethods();
                                for (PsiMethod psiMethod : methods) {
                                    if (equalSignatures(method, psiMethod)) {
                                        return psiMethod;
                                    }
                                }
                            }
                        }
                    }
                } catch (VcsException e) {
                    LOG.error("[ACP] Failed to get a file's content from the last revision.", e.getMessage());
                }
            }
        }
        return null;
    }

    public static int getNumberOfLine(PsiFile file, int offset) {
        FileViewProvider fileViewProvider = file.getViewProvider();
        Document document = fileViewProvider.getDocument();
        return document != null ? document.getLineNumber(offset) + 1 : 0;
    }

    public static boolean equalSignatures(PsiMethod method1, PsiMethod method2) {
        return Objects.equals(calculateSignature(method1), calculateSignature(method2));
    }

    public static String calculateSignature(PsiMethod method) {
        final PsiClass containingClass = method.getContainingClass();
        final String className;
        if (containingClass != null) {
            className = containingClass.getQualifiedName();
        } else {
            className = "";
        }
        final String methodName = method.getName();
        final StringBuilder out = new StringBuilder(50);
        out.append(className);
        out.append("::");
        out.append(methodName);
        out.append('(');
        final PsiParameterList parameterList = method.getParameterList();
        final PsiParameter[] parameters = parameterList.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (i != 0) {
                out.append(',');
            }
            final PsiType parameterType = parameters[i].getType();
            final String parameterTypeText = parameterType.getPresentableText();
            out.append(parameterTypeText);
        }
        out.append(')');
        return out.toString();
    }

    public static PsiMethod findMethodByOffset(PsiFile psiFile, int offset) {
        PsiElement element = psiFile.findElementAt(offset);
        return (PsiMethod) PsiTreeUtil.findFirstParent(element, p -> p instanceof PsiMethod);
    }

    public static PsiElement[] getElements(@NotNull Project project, @NotNull PsiFile file,
                                           int startOffset, int endOffset) {
        PsiElement[] elements;
        PsiExpression expr = CodeInsightUtil.findExpressionInRange(file, startOffset, endOffset);
        if (expr != null) {
            elements = new PsiElement[]{expr};
        } else {
            elements = CodeInsightUtil.findStatementsInRange(file, startOffset, endOffset);
            if (elements.length == 0) {
                final PsiExpression expression =
                    IntroduceVariableBase.getSelectedExpression(project, file, startOffset, endOffset);
                if (expression != null && IntroduceVariableBase.getErrorMessage(expression) == null) {
                    final PsiType originalType = RefactoringUtil.getTypeByExpressionWithExpectedType(expression);
                    if (originalType != null) {
                        elements = new PsiElement[]{expression};
                    }
                }
            }
        }
        return elements;
    }

    public static int getStartOffset(Editor editor, PsiFile file, String text) {
        int caretPos = editor.getCaretModel().getOffset();
        String fileText = file.getText();
        int best_dist = 1000000000;
        int startOffset = -1;

        int fromIdx = 0;
        while (true) {
            int idx = fileText.indexOf(text, fromIdx);
            fromIdx = idx + 1;

            if (idx == -1) {
                break;
            }

            int dist = Math.abs(idx - caretPos) + Math.abs(idx + text.length() - 1 - caretPos);
            if (dist < best_dist) {
                best_dist = dist;
                startOffset = idx;
            }
        }
        return startOffset;
    }
}
