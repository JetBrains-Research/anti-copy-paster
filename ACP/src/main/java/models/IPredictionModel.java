package models;

import models.features.features_vector.IFeaturesVector;

import java.io.IOException;
import java.util.List;

public interface IPredictionModel {
    List<Integer> predict(List<IFeaturesVector> batch) throws IOException;
}
