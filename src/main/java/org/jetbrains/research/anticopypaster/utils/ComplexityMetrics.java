package org.jetbrains.research.anticopypaster.utils;

import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComplexityMetrics implements Flag{

    private int sensitivity;

    private List<FeaturesVector> featuresVectorList;
    
    private float complexityMetricQ1;
    private float complexityMetricQ2;
    private float complexityMetricQ3;


    public ComplexityMetrics(List<FeaturesVector> featuresVectorList){
        this.featuresVectorList = featuresVectorList;
    }

    private float getComplexityMetricFromFV(FeaturesVector fv){
        return fv.buildArray()[3];
    }

    private void calculateAverageComplexityMetrics(){

        ArrayList<Float> complexityMetricsValues = new ArrayList<Float>();

        for(FeaturesVector f : featuresVectorList){
            complexityMetricsValues.add(getComplexityMetricFromFV(f));
        }

        Collections.sort(complexityMetricsValues);
        boxPlotCalculations(complexityMetricsValues);
    }

    private void boxPlotCalculations(ArrayList<Float> data){
        float q1;
        if (data.size() % 2 == 0) {
            q1 = (data.get(data.size()/4 - 1) + data.get(data.size()/4)) / 2;
        } else {
            q1 = data.get(data.size()/4);
        }
        complexityMetricQ1 = q1;
        
        float q2;
        if (data.size() % 2 == 0) {
            q2 = (data.get(data.size()/2 - 1) + data.get(data.size()/2)) / 2;
        } else {
            q2 = data.get(data.size()/2);
        }
        complexityMetricQ2 = q2;
        
        float q3;
        if (data.size() % 2 == 0) {
            q3 = (data.get(data.size()*3/4 - 1) + data.get(data.size()*3/4)) / 2;
        } else {
            q3 = data.get(data.size()*3/4);
        }
        complexityMetricQ3 = q3;
    }

    @Override
    public boolean isFlagTriggered(FeaturesVector featuresVector){
        float fvComplexityValue = getComplexityMetricFromFV(featuresVector);
        switch(sensitivity) {
            case 0:
                return false;
            case 1:
                return fvComplexityValue >= complexityMetricQ1; 
            case 2:
                return fvComplexityValue >= complexityMetricQ2; 
            case 3:
                return fvComplexityValue >= complexityMetricQ3; 
            default:
                return false;
        }
    }

    @Override
    public int changeSensitivity(int sensitivity){
        if(sensitivity > 3 || sensitivity < 0){
            this.sensitivity = 0;
        } else {
            this.sensitivity = sensitivity;
        }
        return this.sensitivity;
    }


}