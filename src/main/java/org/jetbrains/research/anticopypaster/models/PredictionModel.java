package org.jetbrains.research.anticopypaster.models;

import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;

public abstract class PredictionModel {
    public abstract float predict(FeaturesVector featuresVector);
}
