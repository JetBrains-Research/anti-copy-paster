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
    Local testing implementation for this class to test the utilities
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


}