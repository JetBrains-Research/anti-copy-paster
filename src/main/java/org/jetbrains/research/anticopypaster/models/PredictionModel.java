package org.jetbrains.research.anticopypaster.models;

import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;
import org.pmml4s.model.Model;

import java.util.Arrays;
import java.util.List;


public interface PredictionModel {
    Model model = Model.fromInputStream(PredictionModel.class.getClassLoader()
            .getResourceAsStream("rf_eman.pmml"));

    List<Integer> leftFeatures = Arrays.asList(1, 3, 4, 5, 7, 8, 10, 11, 12,
            13, 14, 15, 19, 20, 24, 25, 51, 61);

    static double getClassificationValue(FeaturesVector featuresVector) {
        List<Float> features = featuresVector.buildCroppedVector(leftFeatures);
        Object[] result = model.predict(features.toArray());
        return (Double) result[1];
    }
}