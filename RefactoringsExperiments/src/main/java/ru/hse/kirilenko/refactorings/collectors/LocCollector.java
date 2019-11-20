package ru.hse.kirilenko.refactorings.collectors;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;

import java.util.HashMap;

public class LocCollector {
    public static HashMap<Integer, Integer> counts;

    public static void accept(int value) {
        //counts.put(value, counts.getOrDefault(value, 0) + 1);
    }
}
