package org.jetbrains.research.anticopypaster.models;

import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;
import org.pmml4s.model.Model;

import java.util.Arrays;
import java.util.List;

/**
 * Attributes:
 * modelResourcePath - String-name of file in resource folder corresponding to the PMML model.
 * model - PMML model loaded from resources.
 * leftFeatures - List of indices (zero-based) of features for the model to use.
 */
public class ScikitModel extends PredictionModel {
    private final String modelResourcePath = "rf_imbalanced_pruned.pmml";
    private final Model model = Model.fromInputStream(ScikitModel.class.getClassLoader()
            .getResourceAsStream("rf_imbalanced_pruned.pmml"));
    private final List<Integer> leftFeatures = Arrays.asList(1, 3, 4, 5, 7, 8, 10, 11, 12,
            13, 14, 15, 19, 20, 24, 25, 51, 61);

    @Override
    public float predict(FeaturesVector featuresVector) {
        List<Float> features = featuresVector.buildCroppedVector(leftFeatures);
        Object[] result = model.predict(features.toArray());

        Double positive_proba = (Double) result[1];
        return positive_proba.floatValue();
    }
}