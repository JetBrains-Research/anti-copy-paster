package org.jetbrains.research.anticopypaster.models;

import org.jetbrains.research.anticopypaster.models.features.features_vector.IFeaturesVector;

import java.util.List;

public interface IPredictionModel {
    List<Integer> predict(List<IFeaturesVector> batch);
}
