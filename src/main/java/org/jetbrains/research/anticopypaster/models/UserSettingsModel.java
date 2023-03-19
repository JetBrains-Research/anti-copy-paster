package org.jetbrains.research.anticopypaster.models;

import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;
import org.jetbrains.research.anticopypaster.utils.MetricsGatherer;
import org.jetbrains.research.anticopypaster.utils.KeywordsMetrics;
import org.jetbrains.research.anticopypaster.utils.SizeMetrics;
import org.jetbrains.research.anticopypaster.utils.ComplexityMetrics;
import org.jetbrains.research.anticopypaster.utils.Flag;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class UserSettingsModel extends PredictionModel{

    private MetricsGatherer metricsGatherer;

    private Flag keywordsMetrics;
    private Flag sizeMetrics;
    private Flag complexityMetrics;
    
    private int sizeSensitivity = 0;
    private int complexitySensitivity = 0;
    private int keywordsSensitivity = 0;

    public UserSettingsModel(MetricsGatherer mg){
        //The metricsGatherer instantiation calls a function that can't be used
        //outside of the context of an installed plugin, so in order to unit test
        //our model, the metrics gatherer is passed in from the constructor
        if(mg != null){
            initMetricsGathererAndMetricsFlags(mg);
        }
    }

    /**
    Helper initializaton method for the metrics gatherer.
    This is a separate method so that if we ever wanted to have the metrics 
    gatherer regather metrics and update the values in the sensitivity 
    thresholds
     */
    public void initMetricsGathererAndMetricsFlags(MetricsGatherer mg){
        this.metricsGatherer = mg;

        List<FeaturesVector> methodMetrics = mg.getMethodsMetrics();
        this.keywordsMetrics = new KeywordsMetrics(methodMetrics);
        this.complexityMetrics = new ComplexityMetrics(methodMetrics);
        this.sizeMetrics = new SizeMetrics(methodMetrics);

        readSensitivitiesFromFrontend();
    }

    public void setKeywordsSensitivity(int sensitivity){
        this.keywordsSensitivity = sensitivity;
        this.keywordsMetrics.changeSensitivity(sensitivity);
    }

    public void setComplexitySensitivity(int sensitivity){
        this.complexitySensitivity = sensitivity;
        this.complexityMetrics.changeSensitivity(sensitivity);
    }

    public void setSizeSensitivity(int sensitivity){
        this.sizeSensitivity = sensitivity;
        this.sizeMetrics.changeSensitivity(sensitivity);
    }    

    /**
    Currently stubbed out to default to medium sens
    This should ideally read in whatever the sensitivities are from the frontend,
    the frontend 
    */
    private void readSensitivitiesFromFrontend(){
        int sizeSensFromFrontend = 2;
        int complexitySensFromFrontend = 2;
        int keywordsSensFromFrontend = 2;

        setComplexitySensitivity(complexitySensFromFrontend);
        setSizeSensitivity(sizeSensFromFrontend);
        setKeywordsSensitivity(keywordsSensFromFrontend);
    }

    /**
    This just gets the count of how many flags are not turned off
     */
    private int countOnFlags() {
        int count = 0;
        if (sizeSensitivity != 0) {
            count++;
        }
        if (complexitySensitivity != 0) {
            count++;
        }
        if (keywordsSensitivity != 0) {
            count++;
        }
        return count;
    }


    /**
    Returns a value higher than 0.5 if the task satisfied the requirements
    to be extracted, lower than 0.5 means the notification will not appear.
    This is currently hardcoded to return 1 until the metrics category logic
    has been implemented.
     */
    @Override
    public float predict(FeaturesVector featuresVector){

        if(sizeMetrics == null || complexityMetrics == null || keywordsMetrics == null){
            return 0;
        }

        boolean sizeTriggered = this.sizeMetrics.isFlagTriggered(featuresVector);
        boolean complexityTriggered = this.complexityMetrics.isFlagTriggered(featuresVector);
        boolean keywordsTriggered = this.keywordsMetrics.isFlagTriggered(featuresVector);


        int count = countOnFlags();
        boolean shouldNotify;

        switch (count) {
            case 0:
                shouldNotify = false;
                break;
            case 1:
                // if ANY flags are flipped, this is true
                // 1 category being set to on would be: false || false || {category}
                shouldNotify = sizeTriggered || complexityTriggered || keywordsTriggered;
                break;
            case 2:
                // if 2 flags are flipped, this is true
                // 2 categories being set to on would be: false || ({category1} && {category2})
                shouldNotify = (sizeTriggered && complexityTriggered) || (sizeTriggered && keywordsTriggered) || (complexityTriggered && keywordsTriggered);
                break;
            case 3:
                // if all 3 flags are flipped, this is true
                shouldNotify = sizeTriggered && complexityTriggered && keywordsTriggered;
                break;
            default:
                shouldNotify = false;
                break;
        }

        return shouldNotify ? 1 : 0;
    }

}
