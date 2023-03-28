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

public class TestComplexityMetrics {
    
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

    private ComplexityMetrics complexityMetrics;
    private List<FeaturesVector> fvList;

    @BeforeEach
    public void beforeTest(){
        //Zero out everything
        this.complexityMetrics = null;
        this.fvList = new ArrayList<FeaturesVector>();
    }
    
    @Test
    public void testIsTriggeredSensitivityZero(){
        this.complexityMetrics = new ComplexityMetrics(fvList);
        complexityMetrics.changeSensitivity(0);
        assertFalse(complexityMetrics.isFlagTriggered(null));
    }

    @Test
    public void testIsTriggeredSensitivityOneTrue(){

        // This category only uses metric 4, which would be index 3 here
        float[] fvArrayValue1 = new float[78];
        fvArrayValue1[3] = 1;

        float[] fvArrayValue2 = new float[78];
        fvArrayValue2[3] = 2;

        float[] fvArrayValue3 = new float[78];
        fvArrayValue3[3] = 3;

        float[] fvArrayValue4 = new float[78];
        fvArrayValue4[3] = 4;

        float[] fvArrayValue5 = new float[78];
        fvArrayValue5[3] = 5;
        
        //Adding these values gives:
        // Q1 = 2
        // Q2 = 3
        // Q3 = 4
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        this.complexityMetrics = new ComplexityMetrics(fvList);
        complexityMetrics.changeSensitivity(1);

        float[] passedInArray = new float[78];
        passedInArray[3] = (float)3;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertTrue(complexityMetrics.isFlagTriggered(passedInFv.getMock()));
    }

    @Test
    public void testIsTriggeredSensitivityOneFalse(){

        // This category only uses metric 4, which would be index 3 here
        float[] fvArrayValue1 = new float[78];
        fvArrayValue1[3] = 1;

        float[] fvArrayValue2 = new float[78];
        fvArrayValue2[3] = 2;

        float[] fvArrayValue3 = new float[78];
        fvArrayValue3[3] = 3;

        float[] fvArrayValue4 = new float[78];
        fvArrayValue4[3] = 4;

        float[] fvArrayValue5 = new float[78];
        fvArrayValue5[3] = 5;
        
        //Adding these values gives:
        // Q1 = 2
        // Q2 = 3
        // Q3 = 4
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        this.complexityMetrics = new ComplexityMetrics(fvList);
        this.complexityMetrics.changeSensitivity(1);

        float[] passedInArray = new float[78];
        passedInArray[3] = (float)1;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertFalse(complexityMetrics.isFlagTriggered(passedInFv.getMock()));
    }

    @Test
    public void testIsTriggeredSensitivityTwoTrue(){

        // This category only uses metric 4, which would be index 3 here
        float[] fvArrayValue1 = new float[78];
        fvArrayValue1[3] = 1;

        float[] fvArrayValue2 = new float[78];
        fvArrayValue2[3] = 2;

        float[] fvArrayValue3 = new float[78];
        fvArrayValue3[3] = 3;

        float[] fvArrayValue4 = new float[78];
        fvArrayValue4[3] = 4;

        float[] fvArrayValue5 = new float[78];
        fvArrayValue5[3] = 5;
        
        //Adding these values gives:
        // Q1 = 2
        // Q2 = 3
        // Q3 = 4
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        this.complexityMetrics = new ComplexityMetrics(fvList);
        complexityMetrics.changeSensitivity(2);

        float[] passedInArray = new float[78];
        passedInArray[3] = (float)4;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertTrue(complexityMetrics.isFlagTriggered(passedInFv.getMock()));
    }

    @Test
    public void testIsTriggeredSensitivityTwoFalse(){

        // This category only uses metric 4, which would be index 3 here
        float[] fvArrayValue1 = new float[78];
        fvArrayValue1[3] = 1;

        float[] fvArrayValue2 = new float[78];
        fvArrayValue2[3] = 2;

        float[] fvArrayValue3 = new float[78];
        fvArrayValue3[3] = 3;

        float[] fvArrayValue4 = new float[78];
        fvArrayValue4[3] = 4;

        float[] fvArrayValue5 = new float[78];
        fvArrayValue5[3] = 5;
        
        //Adding these values gives:
        // Q1 = 2
        // Q2 = 3
        // Q3 = 4
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        this.complexityMetrics = new ComplexityMetrics(fvList);
        this.complexityMetrics.changeSensitivity(2);

        float[] passedInArray = new float[78];
        passedInArray[3] = (float)2;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertFalse(complexityMetrics.isFlagTriggered(passedInFv.getMock()));
    }

    @Test
    public void testIsTriggeredSensitivityThreeTrue(){

        // This category only uses metric 4, which would be index 3 here
        float[] fvArrayValue1 = new float[78];
        fvArrayValue1[3] = 1;

        float[] fvArrayValue2 = new float[78];
        fvArrayValue2[3] = 2;

        float[] fvArrayValue3 = new float[78];
        fvArrayValue3[3] = 3;

        float[] fvArrayValue4 = new float[78];
        fvArrayValue4[3] = 4;

        float[] fvArrayValue5 = new float[78];
        fvArrayValue5[3] = 5;
        
        //Adding these values gives:
        // Q1 = 2
        // Q2 = 3
        // Q3 = 4
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        this.complexityMetrics = new ComplexityMetrics(fvList);
        complexityMetrics.changeSensitivity(3);

        float[] passedInArray = new float[78];
        passedInArray[3] = (float)5;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertTrue(complexityMetrics.isFlagTriggered(passedInFv.getMock()));
    }

    @Test
    public void testIsTriggeredSensitivityThreeFalse(){

        // This category only uses metric 4, which would be index 3 here
        float[] fvArrayValue1 = new float[78];
        fvArrayValue1[3] = 1;

        float[] fvArrayValue2 = new float[78];
        fvArrayValue2[3] = 2;

        float[] fvArrayValue3 = new float[78];
        fvArrayValue3[3] = 3;

        float[] fvArrayValue4 = new float[78];
        fvArrayValue4[3] = 4;

        float[] fvArrayValue5 = new float[78];
        fvArrayValue5[3] = 5;
        
        //Adding these values gives:
        // Q1 = 2
        // Q2 = 3
        // Q3 = 4
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        this.complexityMetrics = new ComplexityMetrics(fvList);
        this.complexityMetrics.changeSensitivity(3);

        float[] passedInArray = new float[78];
        passedInArray[3] = (float)3;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertFalse(complexityMetrics.isFlagTriggered(passedInFv.getMock()));
    }

    
}