package org.jetbrains.research.anticopypaster.models;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class MemberSets {
    public Set<String> methods = new HashSet<>();
    public Set<String> fields = new HashSet<>();
    public Set<String> total = new HashSet<>();

    public static MemberSets extractAllMethodsAndFields(PsiElement root) {
        final MemberSets result = new MemberSets();
        root.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(@NotNull PsiElement element) {
                super.visitElement(element);

                String[] elementSplit = element.toString().split(":");
                if (elementSplit[0].equals("PsiMethod")) {
                    String methodName = elementSplit[1];
                    result.methods.add(methodName);
                    result.total.add(methodName);
                }

                if (elementSplit[0].equals("PsiField")) {
                    String fieldName = elementSplit[1];
                    result.fields.add(fieldName);
                    result.total.add(fieldName);
                }

            }
        });
        return result;
    }
}
