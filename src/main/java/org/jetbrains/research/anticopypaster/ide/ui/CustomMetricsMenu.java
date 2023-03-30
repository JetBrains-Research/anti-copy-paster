package org.jetbrains.research.anticopypaster.ide.ui;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Scanner;

public class CustomMetricsMenu extends DialogWrapper {
    private boolean firstTime = true;

    private CustomMetricsModel customMetricsModel;

    private static final String FILE_PATH = ProjectManager.getInstance().getOpenProjects()[0]
            .getBasePath() + "/.idea/custom_metrics.txt";

    private ComboBox<String> keywordsDropdown;
    private ComboBox<String> sizeDropdown;
    private ComboBox<String> complexityDropdown;


    public CustomMetricsMenu() {
        super(true); // true means the dialog is modal
        setTitle("Custom Metrics Model");

        // initialize the dropdown components
        keywordsDropdown = new ComboBox<>();

        sizeDropdown = new ComboBox<>();

        complexityDropdown = new ComboBox<>();


        // initialize the customMetricsModel
        this.customMetricsModel = new CustomMetricsModel();


        // initialize file to read values from
        File file = new File(FILE_PATH);
        if (file.exists()) {
            firstTime = false;
            try (Scanner scanner = new Scanner(file)) {
                //throw away first line
                scanner.nextLine();
                customMetricsModel.keywordsDropdownValue = scanner.nextLine();
                customMetricsModel.sizeDropdownValue = scanner.nextLine();
                customMetricsModel.complexityDropdownValue = scanner.nextLine();
            } catch (FileNotFoundException ex) {
                // Handle file not found exception
            }
        }
        // add items to the dropdowns
        keywordsDropdown.addItem("Off");
        keywordsDropdown.addItem("Low");
        keywordsDropdown.addItem("Medium");
        keywordsDropdown.addItem("High");
        keywordsDropdown.addActionListener(e -> {
            customMetricsModel.keywordsDropdownValue = (String) keywordsDropdown.getSelectedItem();
        });

        sizeDropdown.addItem("Off");
        sizeDropdown.addItem("Low");
        sizeDropdown.addItem("Medium");
        sizeDropdown.addItem("High");
        sizeDropdown.addActionListener(e -> {
            customMetricsModel.sizeDropdownValue = (String) sizeDropdown.getSelectedItem();
        });

        complexityDropdown.addItem("Off");
        complexityDropdown.addItem("Low");
        complexityDropdown.addItem("Medium");
        complexityDropdown.addItem("High");
        complexityDropdown.addActionListener(e -> {
            customMetricsModel.complexityDropdownValue = (String) complexityDropdown.getSelectedItem();
        });

        // create the panel to hold the dropdowns
        createCenterPanel();

        // set the panel as the content of the dialog
        setOKButtonText("Submit");
        setCancelButtonText("Cancel");

        if (!firstTime) {
            if (customMetricsModel.keywordsDropdownValue != null) {
                keywordsDropdown.setSelectedItem(customMetricsModel.keywordsDropdownValue);
            }
            if (customMetricsModel.sizeDropdownValue != null) {
                sizeDropdown.setSelectedItem(customMetricsModel.sizeDropdownValue);
            }
            if (customMetricsModel.complexityDropdownValue != null) {
                complexityDropdown.setSelectedItem(customMetricsModel.complexityDropdownValue);
            }
        }

        firstTime = false;
        init();
    }

    public String getKeywordsDropdownValue() {
        return keywordsDropdown.getSelectedItem().toString();
    }

    public String getSizeDropdownValue() {
        return sizeDropdown.getSelectedItem().toString();
    }

    public String getComplexityDropdownValue() {
        return complexityDropdown.getSelectedItem().toString();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 3));
        //Keywords Inputs
        JLabel keywordsDropdownLabel = new JLabel("Keywords Sensitivity:");
        keywordsDropdownLabel.setToolTipText("Select the sensitivity of how many keywords should be found before refactoring is suggested");
        panel.add(keywordsDropdownLabel);
        panel.add(keywordsDropdown);
        //Size Inputs
        panel.add(new JLabel("Size Sensitivity:"));
        panel.add(sizeDropdown);
        //Complexity Inputs
        panel.add(new JLabel("Complexity Sensitivity:"));
        panel.add(complexityDropdown);
        return panel;
    }
}
