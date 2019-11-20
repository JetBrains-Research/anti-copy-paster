package ru.hse.kirilenko.refactorings;

import javafx.collections.FXCollections;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

import java.util.ArrayList;
import java.util.Arrays;

public class UIUtils {
    public static GridPane makeGridPane() {
        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(5));
        gridpane.setHgap(8);
        gridpane.setVgap(8);

        /*RowConstraints row7 = new RowConstraints(300);
        RowConstraints row9 = new RowConstraints(300);
        ArrayList<ColumnConstraints> al = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            al.add(new ColumnConstraints(100));
        }
        gridpane.getColumnConstraints().addAll(al);
        gridpane.getRowConstraints().addAll(new RowConstraints(), new RowConstraints(), new RowConstraints(), new RowConstraints(), new RowConstraints(),
                new RowConstraints(), new RowConstraints(), row7, new RowConstraints(), row9, new RowConstraints());
        */
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

    public static void addFeaturesInput(GridPane gridPane, Label label, TextField field, Button btn) {
        GridPane.setHalignment(label, HPos.LEFT);
        gridPane.add(label, 6, 1);
        GridPane.setHalignment(field, HPos.LEFT);
        gridPane.add(field, 7, 1, 2, 1);
        GridPane.setHalignment(btn, HPos.LEFT);
        gridPane.add(btn, 9, 1, 3, 1);
    }

    public static void addTargetInput(GridPane gridPane, Label label, TextField field, Button btn) {
        GridPane.setHalignment(label, HPos.LEFT);
        gridPane.add(label, 6, 2);
        GridPane.setHalignment(field, HPos.LEFT);
        gridPane.add(field, 7, 2, 2, 1);
        GridPane.setHalignment(btn, HPos.LEFT);
        gridPane.add(btn, 9, 2, 3, 1);
    }

    public static LineChart<Number,Number> addAlgorithmChart(GridPane gridPane, XYChart.Series series, XYChart.Series seriesT, int index) {
        final NumberAxis xAxis = new NumberAxis("Iteration", 0, 2000, 250);
        final NumberAxis yAxis = new NumberAxis();
        final LineChart<Number,Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Score chart");
        lineChart.setCreateSymbols(false);
        lineChart.setStyle(".chart-series-line { -fx-stroke-width: 1px; }");
        lineChart.getData().add(series);
        lineChart.getData().add(seriesT);
        gridPane.add(lineChart, index * 4, 7, 4, 1);

        return lineChart;
    }

    public static ProgressBar addProgressBar(GridPane gridpane, int index) {
        Label binLabel = new Label("Binarize progress:");
        GridPane.setHalignment(binLabel, HPos.CENTER);
        gridpane.add(binLabel, index * 4, 5);
        ProgressBar algorithmBar = new ProgressBar();
        GridPane.setHalignment(algorithmBar, HPos.LEFT);
        gridpane.add(algorithmBar, index * 4 + 1, 5);
        return algorithmBar;
    }

    public static CheckBox addAlgorithmEnabled(GridPane gridpane, int index) {
        CheckBox box = new CheckBox("enabled");
        GridPane.setHalignment(box, HPos.CENTER);
        gridpane.add(box, index * 4, 6);

        return box;
    }

    public static Label addBinarizeTime(GridPane gridpane, int index) {
        Label binLabel = new Label("Time:");
        binLabel.setStyle
                (
                        "-fx-font-size: 13px;"
                                + "-fx-font-weight:500;"
                );
        GridPane.setHalignment(binLabel, HPos.CENTER);
        gridpane.add(binLabel, index * 4 + 2, 5);

        Label timeLabel = new Label();
        timeLabel.setStyle
                (
                        "-fx-font-size: 13px;"
                                + "-fx-font-weight: bold;"
                );
        GridPane.setHalignment(timeLabel, HPos.LEFT);
        gridpane.add(timeLabel, index * 4 + 3, 5, 2, 1);

        return timeLabel;
    }

    public static BarChart addBinsChartUsage(GridPane gridPane, XYChart.Series bars, int index) {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String,Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Bins usage");
        barChart.setStyle(".chart-series-line { -fx-stroke-width: 1px; }");
        barChart.getData().add(bars);
        gridPane.add(barChart, index * 4, 9, 4, 1);

        return barChart;
    }

    public static Label addScore(GridPane gridpane, int index) {
        Label binLabel = new Label("Best score:");
        binLabel.setStyle
                (
                        "-fx-font-size: 13px;"
                                + "-fx-font-weight: 500;"
                );
        GridPane.setHalignment(binLabel, HPos.CENTER);
        gridpane.add(binLabel, index * 4 + 1, 8);

        Label timeLabel = new Label();
        timeLabel.setStyle
                (
                        "-fx-font-size: 13px;"
                                + "-fx-font-weight: bold;"
                );
        GridPane.setHalignment(timeLabel, HPos.LEFT);
        gridpane.add(timeLabel, index * 4 + 2, 8, 2, 1);

        return timeLabel;
    }

    public static Label addBinsCount(GridPane gridpane, int index) {
        Label binLabel = new Label("Total bins:");
        binLabel.setStyle
                (
                        "-fx-font-size: 13px;"
                                + "-fx-font-weight: 500;"
                );
        GridPane.setHalignment(binLabel, HPos.CENTER);
        gridpane.add(binLabel, index * 4 + 1, 10);

        Label timeLabel = new Label();
        timeLabel.setStyle
                (
                        "-fx-font-size: 13px;"
                                + "-fx-font-weight: bold;"
                );
        GridPane.setHalignment(timeLabel, HPos.LEFT);
        gridpane.add(timeLabel, index * 4 + 2, 10, 2, 1);

        return timeLabel;
    }

    public static Button addSettingsInput(GridPane gridPane) {
        Button btn = new Button("Settings");
        btn.setStyle
                (
                        "-fx-font-size: 19px;"
                                + "-fx-font-weight: bold;"
                                + "-fx-background-color: lightgreen;"
                                + "-fx-border-style: solid inside;"
                                + "-fx-border-width: 0pt;"
                                + "-fx-background-radius: 19pt; "
                );
        GridPane.setHalignment(btn, HPos.LEFT);
        gridPane.add(btn, 14, 1);

        return btn;
    }
}