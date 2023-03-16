package org.jetbrains.research.anticopypaster.utils;

import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SizeMetrics extends Flag{

    public SizeMetrics(List<FeaturesVector> featuresVectorList){
        super(featuresVectorList);
        calculateAverageSizeMetrics();
    }

    private void calculateAverageSizeMetrics(){
        ArrayList<Float> sizeMetricsValues = new ArrayList<Float>();

        for(FeaturesVector f : featuresVectorList){
            sizeMetricsValues.add(getSizeMetricFromFV(f));
        }

        Collections.sort(sizeMetricsValues);
        boxPlotCalculations(sizeMetricsValues);
    }

    /**
    This takes metrics 1 and 12 from the array and gets the ratio
    of size of fragment to size of enclosing method
     */
    private float getSizeMetricFromFV(FeaturesVector fv){
        if(fv != null){
            float[] fvArr = fv.buildArray();
            //If the size of the enclosing method is 0 that would cause a
            //divide by zero error, this will just skip making that calculation
            //and return zero regardless of what size of the snippet is
            if(fvArr[11] != (float)0){
                return fvArr[0]/fvArr[11];
            }
        }
        return 0; 
    }
    
    /**
    Required override function from Flag. This just compares the size (M1/M12)
    of the passed in FeaturesVector against the correct quartile value 
    based on the box plot depending on whatever the sensitivity is.
     */
    @Override
    public boolean isFlagTriggered(FeaturesVector featuresVector){
        float fvSizeValue = getSizeMetricFromFV(featuresVector);
        switch(sensitivity) {
            case 0:
                return false;
            case 1:
                return fvSizeValue >= metricQ1; 
            case 2:
                return fvSizeValue >= metricQ2; 
            case 3:
                return fvSizeValue >= metricQ3; 
            default:
                return false;
        }
    }

}