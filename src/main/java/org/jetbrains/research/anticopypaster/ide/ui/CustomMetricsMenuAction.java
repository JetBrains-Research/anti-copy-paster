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
            String sizeDropdownValue = dialog.getSizeDropdownValue();
            String complexityDropdownValue = dialog.getComplexityDropdownValue();

            // create new customMetricsModel object to return to the backend
            CustomMetricsModel customMetricsModel = new CustomMetricsModel();
            customMetricsModel.keywordsDropdownValue = keywordsDropdownValue;
            customMetricsModel.sizeDropdownValue = sizeDropdownValue;
            customMetricsModel.complexityDropdownValue = complexityDropdownValue;

            // initialize file to write values to
            Project p = ProjectManager.getInstance().getOpenProjects()[0];
            String basePath = p.getBasePath();
            String filepath = basePath + "/.idea/custom_metrics.txt";
            try(FileWriter fr = new FileWriter(filepath)){
                fr.write("Custom Metrics Values:\n");

                //write keywords values
                fr.write(customMetricsModel.keywordsDropdownValue + "\n");

                //write size values
                fr.write(customMetricsModel.sizeDropdownValue + "\n");

                //write complexity values
                fr.write(customMetricsModel.complexityDropdownValue + "\n");

            }catch(IOException ioe){

            }
        }
    }
}