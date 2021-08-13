package org.jetbrains.research.anticopypaster.models.features;

public interface IFeatureItem {
    int getId();

    double getValue();

    void setValue(double newValue);
}