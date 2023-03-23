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

@State(name = "AntiCopyPasterUsageStatistics", storages = {@Storage("custom-anticopypaster-plugin.xml")})
public class CustomMetricsMenu extends DialogWrapper implements PersistentStateComponent<CustomMetricsModel> {
    private boolean firstTime = true;

    private CustomMetricsModel customMetricsModel;

    private static final String FILE_PATH = "./custom_metrics.txt";

    private ComboBox<String> keywordsDropdown;
    private ComboBox<String> couplingDropdown;
    private ComboBox<String> sizeDropdown;
    private ComboBox<String> complexityDropdown;

    public CustomMetricsMenu() {
        super(true); // true means the dialog is modal
        setTitle("Custom Metrics Model");

        // initialize the dropdown components
        keywordsDropdown = new ComboBox<>();
        couplingDropdown = new ComboBox<>();
        sizeDropdown = new ComboBox<>();
        complexityDropdown = new ComboBox<>();

        // initialize the customMetricsModel
        this.customMetricsModel = new CustomMetricsModel();


        // initialize file to write values to
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (Scanner scanner = new Scanner(file)) {
                customMetricsModel.keywordsDropdownValue = scanner.nextLine();
                customMetricsModel.couplingDropdownValue = scanner.nextLine();
                customMetricsModel.sizeDropdownValue = scanner.nextLine();
                customMetricsModel.complexityDropdownValue = scanner.nextLine();
            } catch (FileNotFoundException ex) {
                // Handle file not found exception
            }
        }else{
            Project p = ProjectManager.getInstance().getOpenProjects()[0];
            String basePath = p.getBasePath();
            String filepath = basePath + "/.idea/custom_metrics.txt";
            try(FileWriter fr = new FileWriter(filepath)){
                fr.write("This is a test");
            }catch(IOException ioe){

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

        couplingDropdown.addItem("Off");
        couplingDropdown.addItem("Low");
        couplingDropdown.addItem("Medium");
        couplingDropdown.addItem("High");
        couplingDropdown.addActionListener(e -> {
            customMetricsModel.couplingDropdownValue = (String) couplingDropdown.getSelectedItem();
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
            if (customMetricsModel.couplingDropdownValue != null) {
                couplingDropdown.setSelectedItem(customMetricsModel.couplingDropdownValue);
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

    public String getCouplingDropdownValue() {
        return couplingDropdown.getSelectedItem().toString();
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
        JLabel keywordsLabel = new JLabel("Keywords Sensitivity:");
        keywordsLabel.setToolTipText("Select the sensitivity of how many keywords should be found before refactoring is suggested");
        panel.add(keywordsLabel);
        panel.add(keywordsDropdown);
        panel.add(new JLabel("Coupling Sensitivity:"));
        panel.add(couplingDropdown);
        panel.add(new JLabel("Size Sensitivity:"));
        panel.add(sizeDropdown);
        panel.add(new JLabel("Complexity Sensitivity:"));
        panel.add(complexityDropdown);
        return panel;
    }

    @Override
    public @Nullable CustomMetricsModel getState() {
        return customMetricsModel;
    }

    @Override
    public void loadState(@NotNull CustomMetricsModel state) {
        customMetricsModel = state;
    }

    public static CustomMetricsModel getInstance(Project project) {
        return project.getService(CustomMetricsModel.class);
    }

}
