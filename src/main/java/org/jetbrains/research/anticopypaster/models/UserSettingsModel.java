package org.jetbrains.research.anticopypaster.models;

import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;
import org.jetbrains.research.anticopypaster.utils.MetricsGatherer;



public class UserSettingsModel extends PredictionModel{

    private MetricsGatherer metricsGatherer;

    public UserSettingsModel(){
        //The metricsGatherer instantiation calls a function that can't be used
        //outside of the context of an installed plugin, setting it to null
        //until testing is worked out
        this.metricsGatherer = null;


        // if(metricsGatherer == null){
        //     this.metricsGatherer = new MetricsGatherer();
        // }
    }

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
