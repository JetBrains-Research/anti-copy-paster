package org.jetbrains.research.anticopypaster.ide;

import com.intellij.lang.FileASTNode;
import com.intellij.lang.Language;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.search.SearchScope;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RefactoringEventTest {
//    private PsiFile file;
//    private PsiMethod destinationMethod;
//    private String text;
//    private int matches;
//    private Project project;
//    private Editor editor;
//    private int linesOfCode;
    private RefactoringEvent refactoringEvent;

    @BeforeEach
    public void beforeTest(){
        refactoringEvent = new RefactoringEvent(null, null, null, 0, null, null, 0);
    }

    @Test
    public void testPrintMessage() {
        System.out.println("Message = Test 1");
        assertTrue(true);
    }

    @Test
    public void testPrintMessage2() {
        System.out.println("Message2 = Test 2");
        assertTrue(true);
    }

    @Test
    public void testNullRefactorEvent(){
        //Baseline "null" test to see that our testing folder can access src files
        assertEquals(refactoringEvent.getFile(), null);
    }
}
