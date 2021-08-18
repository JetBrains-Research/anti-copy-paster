package org.jetbrains.research.anticopypaster.models.features;

public class FeatureItem implements IFeatureItem {
    private final Feature type;
    private double value;

    public FeatureItem(Feature type, double value) {
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