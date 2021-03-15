package org.jetbrains.research.anticopypaster.utils;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiType;

import java.util.Objects;

public class PsiUtil {

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
        PsiMethod method = null;
        PsiElement element = psiFile.findElementAt(offset);
        if (element != null) {
            PsiElement elementContext = element.getContext();
            if (elementContext != null && elementContext.getParent() instanceof PsiMethod)
                method = (PsiMethod) element.getContext().getParent();
        }
        return method;
    }
}
