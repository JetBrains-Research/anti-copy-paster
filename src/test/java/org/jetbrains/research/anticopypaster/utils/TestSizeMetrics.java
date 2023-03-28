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

public class TestSizeMetrics {
    
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

    private SizeMetrics sizeMetrics;
    private List<FeaturesVector> fvList;

    @BeforeEach
    public void beforeTest(){
        //Zero out everything
        this.sizeMetrics = null;
        this.fvList = new ArrayList<FeaturesVector>();
    }

    @Test
    public void testIsTriggeredSensitivityZero(){
        this.sizeMetrics = new SizeMetrics(fvList);
        sizeMetrics.changeSensitivity(0);
        assertFalse(sizeMetrics.isFlagTriggered(null));
    }

    @Test
    public void testIsTriggeredSensitivityOneTrue(){

        // This category uses metrics 1 and 12, so we set just those
        float[] fvArrayValue1 = new float[78];
        fvArrayValue1[0] = 0;
        fvArrayValue1[11] = 1;

        float[] fvArrayValue2 = new float[78];
        fvArrayValue2[0] = 1;
        fvArrayValue2[11] = 4;

        float[] fvArrayValue3 = new float[78];
        fvArrayValue3[0] = 1;
        fvArrayValue3[11] = 2;

        float[] fvArrayValue4 = new float[78];
        fvArrayValue4[0] = 3;
        fvArrayValue4[11] = 4;

        float[] fvArrayValue5 = new float[78];
        fvArrayValue5[0] = 1;
        fvArrayValue5[11] = 1;
        
        //Adding these values gives:
        // Q1 = 0.25
        // Q2 = 0.5
        // Q3 = 0.75
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        this.sizeMetrics = new SizeMetrics(fvList);
        sizeMetrics.changeSensitivity(1);

        float[] passedInArray = new float[78];
        passedInArray[0] = 1;
        passedInArray[11] = 2;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertTrue(sizeMetrics.isFlagTriggered(passedInFv.getMock()));
    }

    @Test
    public void testIsTriggeredSensitivityOneFalse(){

        // This category uses metrics 1 and 12, so we set just those
        float[] fvArrayValue1 = new float[78];
        fvArrayValue1[0] = 0;
        fvArrayValue1[11] = 1;

        float[] fvArrayValue2 = new float[78];
        fvArrayValue2[0] = 1;
        fvArrayValue2[11] = 4;

        float[] fvArrayValue3 = new float[78];
        fvArrayValue3[0] = 1;
        fvArrayValue3[11] = 2;

        float[] fvArrayValue4 = new float[78];
        fvArrayValue4[0] = 3;
        fvArrayValue4[11] = 4;

        float[] fvArrayValue5 = new float[78];
        fvArrayValue5[0] = 1;
        fvArrayValue5[11] = 1;
        
        //Adding these values gives:
        // Q1 = 0.25
        // Q2 = 0.5
        // Q3 = 0.75
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        this.sizeMetrics = new SizeMetrics(fvList);
        sizeMetrics.changeSensitivity(1);

        float[] passedInArray = new float[78];
        passedInArray[0] = 1;
        passedInArray[11] = 10;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertFalse(sizeMetrics.isFlagTriggered(passedInFv.getMock()));
    }

    @Test
    public void testIsTriggeredSensitivityTwoTrue(){

        // This category uses metrics 1 and 12, so we set just those
        float[] fvArrayValue1 = new float[78];
        fvArrayValue1[0] = 0;
        fvArrayValue1[11] = 1;

        float[] fvArrayValue2 = new float[78];
        fvArrayValue2[0] = 1;
        fvArrayValue2[11] = 4;

        float[] fvArrayValue3 = new float[78];
        fvArrayValue3[0] = 1;
        fvArrayValue3[11] = 2;

        float[] fvArrayValue4 = new float[78];
        fvArrayValue4[0] = 3;
        fvArrayValue4[11] = 4;

        float[] fvArrayValue5 = new float[78];
        fvArrayValue5[0] = 1;
        fvArrayValue5[11] = 1;
        
        //Adding these values gives:
        // Q1 = 0.25
        // Q2 = 0.5
        // Q3 = 0.75
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        this.sizeMetrics = new SizeMetrics(fvList);
        sizeMetrics.changeSensitivity(2);

        float[] passedInArray = new float[78];
        passedInArray[0] = 3;
        passedInArray[11] = 4;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertTrue(sizeMetrics.isFlagTriggered(passedInFv.getMock()));
    }

    @Test
    public void testIsTriggeredSensitivityTwoFalse(){

        // This category uses metrics 1 and 12, so we set just those
        float[] fvArrayValue1 = new float[78];
        fvArrayValue1[0] = 0;
        fvArrayValue1[11] = 1;

        float[] fvArrayValue2 = new float[78];
        fvArrayValue2[0] = 1;
        fvArrayValue2[11] = 4;

        float[] fvArrayValue3 = new float[78];
        fvArrayValue3[0] = 1;
        fvArrayValue3[11] = 2;

        float[] fvArrayValue4 = new float[78];
        fvArrayValue4[0] = 3;
        fvArrayValue4[11] = 4;

        float[] fvArrayValue5 = new float[78];
        fvArrayValue5[0] = 1;
        fvArrayValue5[11] = 1;
        
        //Adding these values gives:
        // Q1 = 0.25
        // Q2 = 0.5
        // Q3 = 0.75
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        this.sizeMetrics = new SizeMetrics(fvList);
        sizeMetrics.changeSensitivity(2);

        float[] passedInArray = new float[78];
        passedInArray[0] = 1;
        passedInArray[11] = 4;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertFalse(sizeMetrics.isFlagTriggered(passedInFv.getMock()));
    }

    @Test
    public void testIsTriggeredSensitivityThreeTrue(){

        // This category uses metrics 1 and 12, so we set just those
        float[] fvArrayValue1 = new float[78];
        fvArrayValue1[0] = 0;
        fvArrayValue1[11] = 1;

        float[] fvArrayValue2 = new float[78];
        fvArrayValue2[0] = 1;
        fvArrayValue2[11] = 4;

        float[] fvArrayValue3 = new float[78];
        fvArrayValue3[0] = 1;
        fvArrayValue3[11] = 2;

        float[] fvArrayValue4 = new float[78];
        fvArrayValue4[0] = 3;
        fvArrayValue4[11] = 4;

        float[] fvArrayValue5 = new float[78];
        fvArrayValue5[0] = 1;
        fvArrayValue5[11] = 1;
        
        //Adding these values gives:
        // Q1 = 0.25
        // Q2 = 0.5
        // Q3 = 0.75
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        this.sizeMetrics = new SizeMetrics(fvList);
        sizeMetrics.changeSensitivity(3);

        float[] passedInArray = new float[78];
        passedInArray[0] = 1;
        passedInArray[11] = 1;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertTrue(sizeMetrics.isFlagTriggered(passedInFv.getMock()));
    }

    @Test
    public void testIsTriggeredSensitivityThreeFalse(){

        // This category uses metrics 1 and 12, so we set just those
        float[] fvArrayValue1 = new float[78];
        fvArrayValue1[0] = 0;
        fvArrayValue1[11] = 1;

        float[] fvArrayValue2 = new float[78];
        fvArrayValue2[0] = 1;
        fvArrayValue2[11] = 4;

        float[] fvArrayValue3 = new float[78];
        fvArrayValue3[0] = 1;
        fvArrayValue3[11] = 2;

        float[] fvArrayValue4 = new float[78];
        fvArrayValue4[0] = 3;
        fvArrayValue4[11] = 4;

        float[] fvArrayValue5 = new float[78];
        fvArrayValue5[0] = 1;
        fvArrayValue5[11] = 1;
        
        //Adding these values gives:
        // Q1 = 0.25
        // Q2 = 0.5
        // Q3 = 0.75
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        this.sizeMetrics = new SizeMetrics(fvList);
        sizeMetrics.changeSensitivity(3);

        float[] passedInArray = new float[78];
        passedInArray[0] = 1;
        passedInArray[11] = 2;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertFalse(sizeMetrics.isFlagTriggered(passedInFv.getMock()));
    }

}