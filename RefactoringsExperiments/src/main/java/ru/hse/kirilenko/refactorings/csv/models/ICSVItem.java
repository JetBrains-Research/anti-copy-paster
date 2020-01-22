package ru.hse.kirilenko.refactorings.csv.models;

public interface ICSVItem {
    int getId();
    double getValue();
    void setValue(double newValue);
}
