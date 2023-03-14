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

public class ComplexityMetricsTest {
    
    /**
    Inner class to mock a FeaturesVector
     */
    public class FeaturesVectorMock {
        @Mock
        private FeaturesVector mockFeaturesVector;
        
        private float[] metricsArray;

        public FeaturesVectorMock(float[] metricsArray) {
            mockFeaturesVector = mock(FeaturesVector.class);
            this.metricsArray = metricsArray;
            
            // mock methods for the FeaturesVector class
            when(mockFeaturesVector.buildArray())
                .thenReturn(this.metricsArray);
            
        }
        
        public FeaturesVector getMock() {
            return mockFeaturesVector;
        }
    }

    private ComplexityMetrics complexityMetric;
    private List<FeaturesVector> fvList;

    @BeforeEach
    public void beforeTest(){
        this.complexityMetric = null;
        this.fvList = new ArrayList<FeaturesVector>();
    }

    @Test
    public void testSensitivityZero(){
        this.complexityMetric = new ComplexityMetrics(this.fvList);
        int newSens = complexityMetric.changeSensitivity(0);
        assertEquals(newSens, 0);
    }

    @Test
    public void testSensitivityOne(){
        this.complexityMetric = new ComplexityMetrics(this.fvList);
        int newSens = complexityMetric.changeSensitivity(1);
        assertEquals(newSens, 1);
    }

    @Test
    public void testSensitivityTwo(){
        this.complexityMetric = new ComplexityMetrics(this.fvList);
        int newSens = complexityMetric.changeSensitivity(2);
        assertEquals(newSens, 2);
    }

    @Test
    public void testSensitivityThree(){
        this.complexityMetric = new ComplexityMetrics(this.fvList);
        int newSens = complexityMetric.changeSensitivity(3);
        assertEquals(newSens, 3);
    }

    @Test
    public void testSensitivityOutOfRangePositive(){
        this.complexityMetric = new ComplexityMetrics(this.fvList);
        int newSens = complexityMetric.changeSensitivity(4);
        assertEquals(newSens, 0);
    }

    @Test
    public void testSensitivityOutOfRangeNegative(){
        this.complexityMetric = new ComplexityMetrics(this.fvList);
        int newSens = complexityMetric.changeSensitivity(-1);
        assertEquals(newSens, 0);
    }



}