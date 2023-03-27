package org.jetbrains.research.anticopypaster.ide.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

import java.io.FileWriter;
import java.io.IOException;


public class CustomMetricsMenuAction extends AnAction {

    private static final String FILE_PATH = "./custom_metrics.txt";

    public void actionPerformed(AnActionEvent e) {
        CustomMetricsMenu dialog = new CustomMetricsMenu();
        if (dialog.showAndGet()) {
            // user clicked "OK", retrieve the selected values from the dropdowns
            String keywordsDropdownValue = dialog.getKeywordsDropdownValue();
            Boolean keywordsCheckboxValue = dialog.getKeywordsCheckboxValue();
            String couplingDropdownValue = dialog.getCouplingDropdownValue();
            Boolean couplingCheckboxValue = dialog.getCouplingCheckboxValue();
            String sizeDropdownValue = dialog.getSizeDropdownValue();
            Boolean sizeCheckboxValue = dialog.getSizeCheckboxValue();
            String complexityDropdownValue = dialog.getComplexityDropdownValue();
            Boolean complexityCheckboxValue = dialog.getComplexityCheckboxValue();

            // create new customMetricsModel object to return to the backend
            CustomMetricsModel customMetricsModel = new CustomMetricsModel();
            customMetricsModel.keywordsDropdownValue = keywordsDropdownValue;
            customMetricsModel.keywordsCheckboxValue = String.valueOf(keywordsCheckboxValue);
            customMetricsModel.couplingDropdownValue = couplingDropdownValue;
            customMetricsModel.couplingCheckboxValue = String.valueOf(couplingCheckboxValue);
            customMetricsModel.sizeDropdownValue = sizeDropdownValue;
            customMetricsModel.sizeCheckboxValue = String.valueOf(sizeCheckboxValue);
            customMetricsModel.complexityDropdownValue = complexityDropdownValue;
            customMetricsModel.complexityCheckboxValue = String.valueOf(complexityCheckboxValue);

            // Save the values to the file
            dialog.loadState(customMetricsModel);


            System.out.println("---------------------------------------------------------");
            System.out.println("NEW METRICS INPUT");
            System.out.println(customMetricsModel);
            System.out.println("Keywords Sensitivity Dropdown Value:\n\t" + customMetricsModel.keywordsDropdownValue);
            System.out.println("Keywords Sensitivity Checkbox Value:\n\t" + customMetricsModel.keywordsCheckboxValue);

            System.out.println("Coupling Sensitivity Dropdown Value:\n\t" + customMetricsModel.couplingDropdownValue);
            System.out.println("Coupling Sensitivity Checkbox Value:\n\t" + customMetricsModel.couplingCheckboxValue);

            System.out.println("Size Sensitivity Dropdown Value:\n\t" + customMetricsModel.sizeDropdownValue);
            System.out.println("Size Sensitivity Checkbox Value:\n\t" + customMetricsModel.sizeCheckboxValue);

            System.out.println("Complexity Sensitivity Dropdown Value:\n\t" + customMetricsModel.complexityDropdownValue);
            System.out.println("Complexity Sensitivity Checkbox Value:\n\t" + customMetricsModel.complexityCheckboxValue);

            System.out.println("---------------------------------------------------------");

            // initialize file to write values to
            Project p = ProjectManager.getInstance().getOpenProjects()[0];
            String basePath = p.getBasePath();
            String filepath = basePath + "/.idea/custom_metrics.txt";
            try(FileWriter fr = new FileWriter(filepath)){
                fr.write("Custom Metrics Values:\n");

                //write keywords values
                fr.write(customMetricsModel.keywordsDropdownValue + "\n");
                fr.write(customMetricsModel.keywordsCheckboxValue + "\n");

                //write coupling values
                fr.write(customMetricsModel.couplingDropdownValue + "\n");
                fr.write(customMetricsModel.couplingCheckboxValue + "\n");

                //write size values
                fr.write(customMetricsModel.sizeDropdownValue + "\n");
                fr.write(customMetricsModel.sizeCheckboxValue + "\n");

                //write complexity values
                fr.write(customMetricsModel.complexityDropdownValue + "\n");
                fr.write(customMetricsModel.complexityCheckboxValue + "\n");

            }catch(IOException ioe){

            }
        }
    }
}