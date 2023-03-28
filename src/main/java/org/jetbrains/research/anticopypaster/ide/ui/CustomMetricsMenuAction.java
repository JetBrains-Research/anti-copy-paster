package org.jetbrains.research.anticopypaster.ide.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

import java.io.FileWriter;
import java.io.IOException;


public class CustomMetricsMenuAction extends AnAction {


    public void actionPerformed(AnActionEvent e) {
        CustomMetricsMenu dialog = new CustomMetricsMenu();
        if (dialog.showAndGet()) {
            // user clicked "OK", retrieve the selected values from the dropdowns
            String keywordsDropdownValue = dialog.getKeywordsDropdownValue();
            Boolean keywordsCheckboxValue = dialog.getKeywordsCheckboxValue();
            String sizeDropdownValue = dialog.getSizeDropdownValue();
            Boolean sizeCheckboxValue = dialog.getSizeCheckboxValue();
            String complexityDropdownValue = dialog.getComplexityDropdownValue();
            Boolean complexityCheckboxValue = dialog.getComplexityCheckboxValue();

            // create new customMetricsModel object to return to the backend
            CustomMetricsModel customMetricsModel = new CustomMetricsModel();
            customMetricsModel.keywordsDropdownValue = keywordsDropdownValue;
            customMetricsModel.keywordsCheckboxValue = String.valueOf(keywordsCheckboxValue);
            customMetricsModel.sizeDropdownValue = sizeDropdownValue;
            customMetricsModel.sizeCheckboxValue = String.valueOf(sizeCheckboxValue);
            customMetricsModel.complexityDropdownValue = complexityDropdownValue;
            customMetricsModel.complexityCheckboxValue = String.valueOf(complexityCheckboxValue);

            // initialize file to write values to
            Project p = ProjectManager.getInstance().getOpenProjects()[0];
            String basePath = p.getBasePath();
            String filepath = basePath + "/.idea/custom_metrics.txt";
            try(FileWriter fr = new FileWriter(filepath)){
                fr.write("Custom Metrics Values:\n");

                //write keywords values
                fr.write(customMetricsModel.keywordsDropdownValue + "\n");
                fr.write(customMetricsModel.keywordsCheckboxValue + "\n");

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