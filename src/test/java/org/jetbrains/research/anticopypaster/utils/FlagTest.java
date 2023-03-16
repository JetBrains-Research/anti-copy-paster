package org.jetbrains.research.anticopypaster.utils;

import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.mockito.Mock;

import static org.mockito.Mockito.*;

public class FlagTest {

    /**
    Inner, nonspecific testing implementation for this class to test the shared utilities
     */
    class TestingFlag extends Flag{

        public TestingFlag(List<FeaturesVector> featuresVectorList){
            super(featuresVectorList);
        }

        @Override 
        public boolean isFlagTriggered(FeaturesVector featuresVector){
            return false;
        }
    }

    private TestingFlag testFlag;

    @BeforeEach
    public void beforeTest(){
        this.testFlag = new TestingFlag(null);
    }

    @Test
    public void testSensitivityZero(){
        int newSens = testFlag.changeSensitivity(0);
        assertEquals(newSens, 0);
    }

    @Test
    public void testSensitivityOne(){
        int newSens = testFlag.changeSensitivity(1);
        assertEquals(newSens, 1);
    }

    @Test
    public void testSensitivityTwo(){
        int newSens = testFlag.changeSensitivity(2);
        assertEquals(newSens, 2);
    }

    @Test
    public void testSensitivityThree(){
        int newSens = testFlag.changeSensitivity(3);
        assertEquals(newSens, 3);
    }

    @Test
    public void testSensitivityOutOfRangePositive(){
        int newSens = testFlag.changeSensitivity(4);
        assertEquals(newSens, 0);
    }

    @Test
    public void testSensitivityOutOfRangeNegative(){
        int newSens = testFlag.changeSensitivity(-1);
        assertEquals(newSens, 0);
    }

    @Test 
    public void testBoxPlotNullList(){
        testFlag.boxPlotCalculations(null);
        assertEquals(0, testFlag.getMetricQ1(), 0);
        assertEquals(0, testFlag.getMetricQ2(), 0);
        assertEquals(0, testFlag.getMetricQ3(), 0);
    }

    @Test 
    public void testBoxPlotEmptyList(){
        ArrayList emptyList = new ArrayList<Float>();
        testFlag.boxPlotCalculations(emptyList);
        assertEquals(0, testFlag.getMetricQ1(), 0);
        assertEquals(0, testFlag.getMetricQ2(), 0);
        assertEquals(0, testFlag.getMetricQ3(), 0);
    }

    @Test 
    public void testBoxPlotSameNumberFourValuesList(){
        ArrayList fourValueSameNumberFloatList = new ArrayList<Float>();
        
        fourValueSameNumberFloatList.add((float)1.0);
        fourValueSameNumberFloatList.add((float)1.0);
        fourValueSameNumberFloatList.add((float)1.0);
        fourValueSameNumberFloatList.add((float)1.0);

        Collections.sort(fourValueSameNumberFloatList);

        testFlag.boxPlotCalculations(fourValueSameNumberFloatList);
        assertEquals(1, testFlag.getMetricQ1(), 0);
        assertEquals(1, testFlag.getMetricQ2(), 0);
        assertEquals(1, testFlag.getMetricQ3(), 0);
    }

    @Test 
    public void testBoxPlotSameNumberFiveValuesList(){
        ArrayList fiveValueSameNumberFloatList = new ArrayList<Float>();
        
        fiveValueSameNumberFloatList.add((float)1.0);
        fiveValueSameNumberFloatList.add((float)1.0);
        fiveValueSameNumberFloatList.add((float)1.0);
        fiveValueSameNumberFloatList.add((float)1.0);
        fiveValueSameNumberFloatList.add((float)1.0);

        Collections.sort(fiveValueSameNumberFloatList);

        testFlag.boxPlotCalculations(fiveValueSameNumberFloatList);
        assertEquals(1, testFlag.getMetricQ1(), 0);
        assertEquals(1, testFlag.getMetricQ2(), 0);
        assertEquals(1, testFlag.getMetricQ3(), 0);
    }

    @Test 
    public void testBoxPlotDifferentNumberFourValuesList(){
        ArrayList fourValueDifferentNumberFloatList = new ArrayList<Float>();
        
        fourValueDifferentNumberFloatList.add((float)1.0);
        fourValueDifferentNumberFloatList.add((float)2.0);
        fourValueDifferentNumberFloatList.add((float)3.0);
        fourValueDifferentNumberFloatList.add((float)4.0);

        Collections.sort(fourValueDifferentNumberFloatList);

        testFlag.boxPlotCalculations(fourValueDifferentNumberFloatList);
        assertEquals(1.5, testFlag.getMetricQ1(), 0);
        assertEquals(2.5, testFlag.getMetricQ2(), 0);
        assertEquals(3.5, testFlag.getMetricQ3(), 0);
    }

    @Test 
    public void testBoxPlotDifferentNumberFiveValuesList(){
        ArrayList fiveValueDifferentNumberFloatList = new ArrayList<Float>();
        
        fiveValueDifferentNumberFloatList.add((float)1.0);
        fiveValueDifferentNumberFloatList.add((float)2.0);
        fiveValueDifferentNumberFloatList.add((float)3.0);
        fiveValueDifferentNumberFloatList.add((float)4.0);
        fiveValueDifferentNumberFloatList.add((float)5.0);

        Collections.sort(fiveValueDifferentNumberFloatList);

        testFlag.boxPlotCalculations(fiveValueDifferentNumberFloatList);
        assertEquals(2, testFlag.getMetricQ1(), 0);
        assertEquals(3, testFlag.getMetricQ2(), 0);
        assertEquals(4, testFlag.getMetricQ3(), 0);
    }

}