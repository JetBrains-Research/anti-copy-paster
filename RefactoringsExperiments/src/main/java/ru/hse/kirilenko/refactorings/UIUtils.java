package ru.hse.kirilenko.refactorings;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

public class UIUtils {
    public static GridPane makeGridPane() {
        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setHgap(8);
        gridpane.setVgap(8);
        return gridpane;
    }

    public static void addDatasetInput(GridPane gridPane, Label label, TextField field, Button btn) {
        GridPane.setHalignment(label, HPos.LEFT);
        gridPane.add(label, 2, 0);
        GridPane.setHalignment(field, HPos.LEFT);
        gridPane.add(field, 3, 0, 2, 1);
        GridPane.setHalignment(btn, HPos.LEFT);
        gridPane.add(btn, 6, 0, 3, 1);
    }


}