package org.jetbrains.research.anticopypaster.utils;

import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.changes.Change;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vcs.changes.ContentRevision;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.Objects;

public class PsiUtil {

    private static final Logger LOG = Logger.getInstance(PsiUtil.class);

    /**
     * Check the before revision of the file (without local changes) to find the method's start line.
     *
     * @param fileWithLocalChanges file that contains the local changes;
     * @param method               method to search for;
     * @return number of the method's start line in the file from the last revision.
     */
    public static int getMethodStartLineInBeforeRevision(PsiFile fileWithLocalChanges, PsiMethod method) {
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
                                        return getNumberOfMethodStartLine(psiFileBeforeRevision,
                                                                          psiMethod.getTextOffset());
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
        return 0;
    }

    public static int getNumberOfMethodStartLine(PsiFile file, int offset) {
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
}
