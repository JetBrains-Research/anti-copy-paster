package org.jetbrains.research.anticopypaster.models;

import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;

public class UserSettingsModel extends PredictionModel{

    @Override
    public float predict(FeaturesVector featuresVector){
        return 1;
    }

}
