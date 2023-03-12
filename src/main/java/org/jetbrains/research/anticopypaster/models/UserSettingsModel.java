package org.jetbrains.research.anticopypaster.models;

import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;

public class UserSettingsModel extends PredictionModel{

    /**
    Returns a value higher than 0.5 if the task satisfied the requirements
    to be extracted, lower than 0.5 means the notification will not appear.
    This is currently hardcoded to return 1 until the metrics category logic
    has been implemented.
     */
    @Override
    public float predict(FeaturesVector featuresVector){
        return 1;
    }

}
