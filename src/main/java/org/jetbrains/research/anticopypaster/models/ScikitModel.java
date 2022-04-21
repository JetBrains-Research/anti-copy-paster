package org.jetbrains.research.anticopypaster.models;

import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;
import org.pmml4s.model.Model;

import java.util.Arrays;
import java.util.List;


public class ScikitModel extends PredictionModel {
    private Model model;
    private List<Integer> leftFeatures;

    public ScikitModel() {
        model = Model.fromInputStream(ScikitModel.class.getClassLoader()
                .getResourceAsStream("rf_imbalanced_pruned.pmml"));

        leftFeatures = Arrays.asList(1, 3, 4, 5, 7, 8, 10, 11, 12,
                13, 14, 15, 19, 20, 24, 25, 51, 61);
    }

    @Override
    public float predict(FeaturesVector featuresVector) {
        List<Float> features = featuresVector.buildCroppedVector(leftFeatures);
        Object[] result = model.predict(features.toArray());

        float positive_proba = (float) result[1];
        return positive_proba;
    }
}