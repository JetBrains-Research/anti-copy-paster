package org.jetbrains.research.anticopypaster.ide;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.Assert.assertTrue;


public class RefactoringNotificationTaskTest {

    private RefactoringNotificationTask refactoringNotificationTask;

    @BeforeEach
    public void printMessage(){
        System.out.println("BeforeEach - complete");
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
}

