package org.jetbrains.research.anticopypaster.models;

import org.jetbrains.research.anticopypaster.models.features.FeaturesVector;
import org.pmml4s.model.Model;


public interface PredictionModel {
    Model model = Model.fromInputStream(PredictionModel.class.getClassLoader()
            .getResourceAsStream("mlp_pipeline.pmml"));
    ;

    static double getClassificationValue(FeaturesVector featuresVector) {
        Object[] result = model.predict(featuresVector.getFeatures());
        return (Double) result[0];
    }
}
