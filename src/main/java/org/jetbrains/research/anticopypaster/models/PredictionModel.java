package org.jetbrains.research.anticopypaster.models;

import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;
import org.pmml4s.model.Model;


public interface PredictionModel {
    Model model = Model.fromInputStream(PredictionModel.class.getClassLoader()
            .getResourceAsStream("gradboost.pmml"));

    static double getClassificationValue(FeaturesVector featuresVector) {
        model.probabilitiesSupported();
        Object[] result = model.predict(featuresVector.buildVector().toArray());
        return (Double) result[0];
    }
}