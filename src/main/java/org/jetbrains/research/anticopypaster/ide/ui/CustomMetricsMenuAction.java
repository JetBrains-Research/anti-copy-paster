package org.jetbrains.research.anticopypaster.ide.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;


public class CustomMetricsMenuAction extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        CustomMetricsMenu dialog = new CustomMetricsMenu();
        if (dialog.showAndGet()) {
            // user clicked "OK", retrieve the selected values from the dropdowns
            String keywordsDropdownValue = dialog.getKeywordsDropdownValue();
            String couplingDropdownValue = dialog.getCouplingDropdownValue();
            String sizeDropdownValue = dialog.getSizeDropdownValue();
            String complexityDropdownValue = dialog.getComplexityDropdownValue();

            // create new customMetricsModel object to return to the backend
            CustomMetricsModel customMetricsModel = new CustomMetricsModel();
            customMetricsModel.keywordsDropdownValue = keywordsDropdownValue;
            customMetricsModel.couplingDropdownValue = couplingDropdownValue;
            customMetricsModel.sizeDropdownValue = sizeDropdownValue;
            customMetricsModel.complexityDropdownValue = complexityDropdownValue;

            // Save the values to the file
            dialog.loadState(customMetricsModel);


            System.out.println("---------------------------------------------------------");
            System.out.println("NEW METRICS INPUT");
            System.out.println(customMetricsModel);
            System.out.println("Keywords Sensitivity Dropdown Value:\n\t" + customMetricsModel.keywordsDropdownValue);
            System.out.println("Coupling Sensitivity Dropdown Value:\n\t" + customMetricsModel.couplingDropdownValue);
            System.out.println("Size Sensitivity Dropdown Value:\n\t" + customMetricsModel.sizeDropdownValue);
            System.out.println("Complexity Sensitivity Dropdown Value:\n\t" + customMetricsModel.complexityDropdownValue);
            System.out.println("---------------------------------------------------------");

        }
    }
}