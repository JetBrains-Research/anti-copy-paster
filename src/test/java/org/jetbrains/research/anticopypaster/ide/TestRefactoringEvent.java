package org.jetbrains.research.anticopypaster.ide;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestRefactoringEvent {
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
