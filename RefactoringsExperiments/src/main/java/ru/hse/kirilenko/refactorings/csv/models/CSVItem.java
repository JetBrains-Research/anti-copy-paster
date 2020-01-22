package ru.hse.kirilenko.refactorings.csv.models;

public class CSVItem implements ICSVItem {
    private Feature type;
    private double value;

    public CSVItem(Feature type, double value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public int getId() {
        return type.getId();
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public void setValue(double newValue) {
        this.value = newValue;
    }
}
