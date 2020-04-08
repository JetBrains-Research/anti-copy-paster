import com.intellij.codeInspection.InspectionToolProvider;


public class FindDuplicatesInspectionToolProvider implements InspectionToolProvider {
    public Class[] getInspectionClasses() {
        return new Class[]{FindDuplicatesInspection.class};
    }
}