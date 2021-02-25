package org.jetbrains.research.anticopypaster.models.features.feature;

public interface IFeatureItem {
    int getId();
    double getValue();
    void setValue(double newValue);
}