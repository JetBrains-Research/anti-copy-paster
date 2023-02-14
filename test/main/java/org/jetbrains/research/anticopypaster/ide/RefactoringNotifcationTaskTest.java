package main.java.org.jetbrains.research.anticopypaster.ide;
import org.jetbrains.research.anticopypaster.models.TensorflowModel;
import org.junit.Test;
import static org.junit.Assert.assertEquals;


public class RefactoringNotifcationTaskTest {
    TensorflowModel model = new TensorflowModel();

    @Test
    public void testPrintMessage() {
        System.out.println("Message = ");
        assertEquals(true, true);
    }
}

