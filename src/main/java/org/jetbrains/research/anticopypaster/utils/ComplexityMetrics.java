package org.jetbrains.research.anticopypaster.utils;

import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComplexityMetrics extends Flag{

    public ComplexityMetrics(List<FeaturesVector> featuresVectorList){
        super(featuresVectorList);
        calculateAverageComplexityMetrics();
    }

    private float getComplexityMetricFromFV(FeaturesVector fv){
        if(fv != null){
            return fv.buildArray()[3];
        } else {
            return 0;
        }
    }

    private void calculateAverageComplexityMetrics(){

        ArrayList<Float> complexityMetricsValues = new ArrayList<Float>();

        for(FeaturesVector f : featuresVectorList){
            complexityMetricsValues.add(getComplexityMetricFromFV(f));
        }

        Collections.sort(complexityMetricsValues);
        boxPlotCalculations(complexityMetricsValues);
    }

    @Override
    public boolean isFlagTriggered(FeaturesVector featuresVector){
        float fvComplexityValue = getComplexityMetricFromFV(featuresVector);
        switch(sensitivity) {
            case 0:
                return false;
            case 1:
                return fvComplexityValue >= metricQ1; 
            case 2:
                return fvComplexityValue >= metricQ2; 
            case 3:
                return fvComplexityValue >= metricQ3; 
        }
    }
}