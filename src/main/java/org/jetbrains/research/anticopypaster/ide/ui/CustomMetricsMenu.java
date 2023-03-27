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
    private JCheckBox keywordsCheckbox;
    private ComboBox<String> couplingDropdown;
    private JCheckBox couplingCheckbox;
    private ComboBox<String> sizeDropdown;
    private JCheckBox sizeCheckbox;
    private ComboBox<String> complexityDropdown;
    private JCheckBox complexityCheckbox;


    public CustomMetricsMenu() {
        super(true); // true means the dialog is modal
        setTitle("Custom Metrics Model");

        // initialize the dropdown and checkbox components
        keywordsDropdown = new ComboBox<>();
        keywordsCheckbox = new JCheckBox();
        keywordsCheckbox.addActionListener(e -> {
            customMetricsModel.keywordsCheckboxValue = String.valueOf(keywordsCheckbox.isSelected());
        });
        couplingDropdown = new ComboBox<>();
        couplingCheckbox = new JCheckBox();
        couplingCheckbox.addActionListener(e -> {
            customMetricsModel.couplingCheckboxValue = String.valueOf(couplingCheckbox.isSelected());
        });
        sizeDropdown = new ComboBox<>();
        sizeCheckbox = new JCheckBox();
        sizeCheckbox.addActionListener(e -> {
            customMetricsModel.sizeCheckboxValue = String.valueOf(sizeCheckbox.isSelected());
        });
        complexityDropdown = new ComboBox<>();
        complexityCheckbox = new JCheckBox();
        complexityCheckbox.addActionListener(e -> {
            customMetricsModel.complexityCheckboxValue = String.valueOf(complexityCheckbox.isSelected());
        });

        // initialize the customMetricsModel
        this.customMetricsModel = new CustomMetricsModel();


        // initialize file to write values to
        File file = new File(FILE_PATH);
        if (file.exists()) {
            firstTime = false;
            try (Scanner scanner = new Scanner(file)) {
                //throw away first line
                scanner.nextLine();
                customMetricsModel.keywordsDropdownValue = scanner.nextLine();
                customMetricsModel.keywordsCheckboxValue = scanner.nextLine();
                customMetricsModel.couplingDropdownValue = scanner.nextLine();
                customMetricsModel.couplingCheckboxValue = scanner.nextLine();
                customMetricsModel.sizeDropdownValue = scanner.nextLine();
                customMetricsModel.sizeCheckboxValue = scanner.nextLine();
                customMetricsModel.complexityDropdownValue = scanner.nextLine();
                customMetricsModel.complexityCheckboxValue = scanner.nextLine();
            } catch (FileNotFoundException ex) {
                // Handle file not found exception
            }
        }else{
            Project p = ProjectManager.getInstance().getOpenProjects()[0];
            String basePath = p.getBasePath();
            String filepath = basePath + "/.idea/custom_metrics.txt";
            try(FileWriter fr = new FileWriter(filepath)){
                fr.write("Custom Metrics Values:");
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

    public Boolean getKeywordsCheckboxValue() {
        return keywordsCheckbox.isSelected();
    }

    public String getCouplingDropdownValue() {
        return couplingDropdown.getSelectedItem().toString();
    }

    public Boolean getCouplingCheckboxValue() {
        return couplingCheckbox.isSelected();
    }

    public String getSizeDropdownValue() {
        return sizeDropdown.getSelectedItem().toString();
    }

    public Boolean getSizeCheckboxValue() {
        return sizeCheckbox.isSelected();
    }

    public String getComplexityDropdownValue() {
        return complexityDropdown.getSelectedItem().toString();
    }

    public Boolean getComplexityCheckboxValue() {
        return complexityCheckbox.isSelected();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 3));
        //Keywords Inputs
        JLabel keywordsDropdownLabel = new JLabel("Keywords Sensitivity:");
        keywordsDropdownLabel.setToolTipText("Select the sensitivity of how many keywords should be found before refactoring is suggested");
        panel.add(keywordsDropdownLabel);
        panel.add(keywordsDropdown);
        JLabel keywordsCheckboxLabel = new JLabel("Keywords Required:");
        panel.add(keywordsCheckboxLabel);
        panel.add(keywordsCheckbox);
        //Coupling Inputs
        panel.add(new JLabel("Coupling Sensitivity:"));
        panel.add(couplingDropdown);
        panel.add(new JLabel("Coupling Required:"));
        panel.add(couplingCheckbox);
        //Size Inputs
        panel.add(new JLabel("Size Sensitivity:"));
        panel.add(sizeDropdown);
        panel.add(new JLabel("Size Required:"));
        panel.add(sizeCheckbox);
        //Complexity Inputs
        panel.add(new JLabel("Complexity Sensitivity:"));
        panel.add(complexityDropdown);
        panel.add(new JLabel("Complexity Required:"));
        panel.add(complexityCheckbox);
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
