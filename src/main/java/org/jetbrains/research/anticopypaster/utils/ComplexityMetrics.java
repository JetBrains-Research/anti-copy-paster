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

    /**
    This is a function that will get the complexity metric out of 
    the FeaturesVector that is passed in
    Complexity only uses Metric #4, so getting the value at index 3
    from the fv array gives us the right value
     */
    private float getComplexityMetricFromFV(FeaturesVector fv){
        if(fv != null){
            return fv.buildArray()[3];
        } else {
            return 0;
        }
    }

    /**
    This will iterate over all of the FeaturesVectors passed in to the
    class, and then export only the relevant metric values to an array.
    That array will then be sorted and run through the Flag boxplot 
    method to get Q1, Q2, and Q3 for the sensitivities
     */
    private void calculateAverageComplexityMetrics(){
        ArrayList<Float> complexityMetricsValues = new ArrayList<Float>();

        for(FeaturesVector f : featuresVectorList){
            complexityMetricsValues.add(getComplexityMetricFromFV(f));
        }

        Collections.sort(complexityMetricsValues);
        boxPlotCalculations(complexityMetricsValues);
    }

    /**
    Required override function from Flag. This just compares the complexity
    of the passed in FeaturesVector against the correct quartile value 
    based on the box plot depending on whatever the sensitivity is.
     */
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
            default:
                return false;
        }
        
    }
}