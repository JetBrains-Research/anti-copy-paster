package org.jetbrains.research.anticopypaster.utils;

import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Flag{

    protected int sensitivity;

    protected List<FeaturesVector> featuresVectorList;

    protected float metricQ1;
    protected float metricQ2;
    protected float metricQ3;

    public abstract boolean isFlagTriggered(FeaturesVector featuresVector);

    public Flag(List<FeaturesVector> featuresVectorList){
        this.featuresVectorList = featuresVectorList;
        this.metricQ1=0;
        this.metricQ2=0;
        this.metricQ3=0;
    }

    /**
    Takes a SORTED list and generates/sets the Q1-3 values based on a box plot
    of those metric values
     */
    protected void boxPlotCalculations(ArrayList<Float> data){

        if(data == null || data.size() == 0){
            metricQ1=0;
            metricQ2=0;
            metricQ3=0;
            return;
        }

        float q1;
        if (data.size() % 2 == 0) {
            q1 = (data.get(data.size()/4 - 1) + data.get(data.size()/4)) / 2;
        } else {
            q1 = data.get(data.size()/4);
        }
        metricQ1 = q1;
        
        float q2;
        if (data.size() % 2 == 0) {
            q2 = (data.get(data.size()/2 - 1) + data.get(data.size()/2)) / 2;
        } else {
            q2 = data.get(data.size()/2);
        }
        metricQ2 = q2;
        
        float q3;
        if (data.size() % 2 == 0) {
            q3 = (data.get(data.size()*3/4 - 1) + data.get(data.size()*3/4)) / 2;
        } else {
            q3 = data.get(data.size()*3/4);
        }
        metricQ3 = q3;
    }

    public float getMetricQ1(){
        return this.metricQ1;
    }

    public float getMetricQ2(){
        return this.metricQ2;
    }

    public float getMetricQ3(){
        return this.metricQ3;
    }

    public int changeSensitivity(int sensitivity){
        if(sensitivity > 3 || sensitivity < 0){
            this.sensitivity = 0;
        } else {
            this.sensitivity = sensitivity;
        }
        return this.sensitivity;
    }

}