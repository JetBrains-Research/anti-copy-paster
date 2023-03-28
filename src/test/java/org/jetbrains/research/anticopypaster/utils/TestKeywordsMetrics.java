package org.jetbrains.research.anticopypaster.utils;

import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.mockito.Mock;

import static org.mockito.Mockito.*;

public class TestKeywordsMetrics {
    
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

    private float[] generateArrayForKeywordsPopulatedByValue(float value){
        float[] floatArr = new float[78];
        for (int i = 16; i <= 77; i += 2) {
            floatArr[i] = value;
        }
        return floatArr;
    }

    private KeywordsMetrics keywordsMetrics;
    private List<FeaturesVector> fvList;

    @BeforeEach
    public void beforeTest(){
        //Zero out everything
        this.keywordsMetrics = null;
        this.fvList = new ArrayList<FeaturesVector>();
    }
    
    @Test
    public void testIsTriggeredSensitivityZero(){
        this.keywordsMetrics = new KeywordsMetrics(fvList);
        keywordsMetrics.changeSensitivity(0);
        assertFalse(keywordsMetrics.isFlagTriggered(null));
    }


    @Test
    public void testIsTriggeredSensitivityOneTrue(){

        // This category uses the odd metrics between 17-77 so there's a helper method for it
        float[] fvArrayValue1 = generateArrayForKeywordsPopulatedByValue(1);
        float[] fvArrayValue2 = generateArrayForKeywordsPopulatedByValue(2);
        float[] fvArrayValue3 = generateArrayForKeywordsPopulatedByValue(3);
        float[] fvArrayValue4 = generateArrayForKeywordsPopulatedByValue(4);
        float[] fvArrayValue5 = generateArrayForKeywordsPopulatedByValue(5);
        
        //Adding these values gives:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        this.keywordsMetrics = new KeywordsMetrics(fvList);
        keywordsMetrics.changeSensitivity(1);

        float[] passedInArray = generateArrayForKeywordsPopulatedByValue(3);
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertTrue(keywordsMetrics.isFlagTriggered(passedInFv.getMock()));
    }

    @Test
    public void testIsTriggeredSensitivityOneFalse(){

        // This category uses the odd metrics between 17-77 so there's a helper method for it
        float[] fvArrayValue1 = generateArrayForKeywordsPopulatedByValue(1);
        float[] fvArrayValue2 = generateArrayForKeywordsPopulatedByValue(2);
        float[] fvArrayValue3 = generateArrayForKeywordsPopulatedByValue(3);
        float[] fvArrayValue4 = generateArrayForKeywordsPopulatedByValue(4);
        float[] fvArrayValue5 = generateArrayForKeywordsPopulatedByValue(5);
        
        //Adding these values gives:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        this.keywordsMetrics = new KeywordsMetrics(fvList);
        keywordsMetrics.changeSensitivity(1);

        float[] passedInArray = generateArrayForKeywordsPopulatedByValue(1);
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertFalse(keywordsMetrics.isFlagTriggered(passedInFv.getMock()));
    }

    @Test
    public void testIsTriggeredSensitivityTwoTrue(){

        // This category uses the odd metrics between 17-77 so there's a helper method for it
        float[] fvArrayValue1 = generateArrayForKeywordsPopulatedByValue(1);
        float[] fvArrayValue2 = generateArrayForKeywordsPopulatedByValue(2);
        float[] fvArrayValue3 = generateArrayForKeywordsPopulatedByValue(3);
        float[] fvArrayValue4 = generateArrayForKeywordsPopulatedByValue(4);
        float[] fvArrayValue5 = generateArrayForKeywordsPopulatedByValue(5);
        
        //Adding these values gives:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        this.keywordsMetrics = new KeywordsMetrics(fvList);
        keywordsMetrics.changeSensitivity(2);

        float[] passedInArray = generateArrayForKeywordsPopulatedByValue(4);
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertTrue(keywordsMetrics.isFlagTriggered(passedInFv.getMock()));
    }

    @Test
    public void testIsTriggeredSensitivityTwoFalse(){

        // This category uses the odd metrics between 17-77 so there's a helper method for it
        float[] fvArrayValue1 = generateArrayForKeywordsPopulatedByValue(1);
        float[] fvArrayValue2 = generateArrayForKeywordsPopulatedByValue(2);
        float[] fvArrayValue3 = generateArrayForKeywordsPopulatedByValue(3);
        float[] fvArrayValue4 = generateArrayForKeywordsPopulatedByValue(4);
        float[] fvArrayValue5 = generateArrayForKeywordsPopulatedByValue(5);
        
        //Adding these values gives:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        this.keywordsMetrics = new KeywordsMetrics(fvList);
        keywordsMetrics.changeSensitivity(2);

        float[] passedInArray = generateArrayForKeywordsPopulatedByValue(2);
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertFalse(keywordsMetrics.isFlagTriggered(passedInFv.getMock()));
    }

    @Test
    public void testIsTriggeredSensitivityThreeTrue(){

        // This category uses the odd metrics between 17-77 so there's a helper method for it
        float[] fvArrayValue1 = generateArrayForKeywordsPopulatedByValue(1);
        float[] fvArrayValue2 = generateArrayForKeywordsPopulatedByValue(2);
        float[] fvArrayValue3 = generateArrayForKeywordsPopulatedByValue(3);
        float[] fvArrayValue4 = generateArrayForKeywordsPopulatedByValue(4);
        float[] fvArrayValue5 = generateArrayForKeywordsPopulatedByValue(5);
        
        //Adding these values gives:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        this.keywordsMetrics = new KeywordsMetrics(fvList);
        keywordsMetrics.changeSensitivity(3);

        float[] passedInArray = generateArrayForKeywordsPopulatedByValue(5);
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertTrue(keywordsMetrics.isFlagTriggered(passedInFv.getMock()));
    }

    @Test
    public void testIsTriggeredSensitivityThreeFalse(){

        // This category uses the odd metrics between 17-77 so there's a helper method for it
        float[] fvArrayValue1 = generateArrayForKeywordsPopulatedByValue(1);
        float[] fvArrayValue2 = generateArrayForKeywordsPopulatedByValue(2);
        float[] fvArrayValue3 = generateArrayForKeywordsPopulatedByValue(3);
        float[] fvArrayValue4 = generateArrayForKeywordsPopulatedByValue(4);
        float[] fvArrayValue5 = generateArrayForKeywordsPopulatedByValue(5);
        
        //Adding these values gives:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        this.keywordsMetrics = new KeywordsMetrics(fvList);
        keywordsMetrics.changeSensitivity(3);

        float[] passedInArray = generateArrayForKeywordsPopulatedByValue(3);
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertFalse(keywordsMetrics.isFlagTriggered(passedInFv.getMock()));
    }

}