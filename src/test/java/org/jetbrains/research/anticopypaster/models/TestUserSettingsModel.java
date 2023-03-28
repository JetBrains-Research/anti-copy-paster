package org.jetbrains.research.anticopypaster.models;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;
import org.jetbrains.research.anticopypaster.utils.MetricsGatherer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.mockito.Mock;

import static org.mockito.Mockito.*;


public class TestUserSettingsModel {

    /**
    Inner class to mock a FeaturesVector, should only need buildArray() for this
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


    /**
    Mock metrics gatherer needs to go here
    */ 
    public class MetricsGathererMock {
        @Mock
        private MetricsGatherer mockMetricsGatherer;
        
        List<FeaturesVector> fvArray;

        public MetricsGathererMock(List<FeaturesVector> fvArray) {
            mockMetricsGatherer = mock(MetricsGatherer.class);
            
            this.fvArray = fvArray;

            // mock methods for the MetricsGatherer class, should only need to mock getMethodMetrics() here
            when(mockMetricsGatherer.getMethodsMetrics())
                .thenReturn(this.fvArray);
            
        }
        
        public MetricsGatherer getMock() {
            return mockMetricsGatherer;
        }
    }    

    private float[] generateAndFillArray(int value){
        float[] array = new float[78]; // generate array for metrics
        Arrays.fill(array, value); // set every value in the array to the passed in value
        return array;
    }

    private UserSettingsModel model;

    /**
    Create a new model before each test
     */
    @BeforeEach
    public void beforeTest(){
        model = new UserSettingsModel(null);
    }

    /**
    This is a test to make sure that if the model has a null metrics
    gatherer that it will always return 0 (do not pop-up)
     */
    @Test
    public void testPredictEverythingNull(){
        assertEquals(model.predict(null), 0, 0);
    }

    @Test
    public void testPredictEverythingOff(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        float[] fvArrayValue1 = new float[78];
        float[] fvArrayValue2 = new float[78];
        float[] fvArrayValue3 = new float[78];
        float[] fvArrayValue4 = new float[78];
        float[] fvArrayValue5 = new float[78];
        
        //Adding these values gives:
        // Q1 = 0
        // Q2 = 0
        // Q3 = 0
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        // turn everything off
        this.model.setComplexitySensitivity(0);
        this.model.setKeywordsSensitivity(0);
        this.model.setSizeSensitivity(0);

        // Make a FeaturesVector that would trip the flag, but should not
        float[] passedInArray = generateAndFillArray(1);
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);

    }

    /**
    
    These tests will retest each of the cases from the flag tests
    to show that they are still valid when the flags are created
    via the metrics gatherer. If any of these tests fail, but the 
    flag tests are passing, it is an issue within the model.
    
     */
    @Test
    public void testPredictOnlySizeOnSensOneTrue(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

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

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(0);
        this.model.setKeywordsSensitivity(0);
        this.model.setSizeSensitivity(1);

        // Make a FeaturesVector that will trip the flag
        float[] passedInArray = new float[78];
        passedInArray[0] = 1;
        passedInArray[11] = 2;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 1, 0);
    }

    @Test
    public void testPredictOnlySizeOnSensOneFalse(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

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

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(0);
        this.model.setKeywordsSensitivity(0);
        this.model.setSizeSensitivity(1);

        // Make a FeaturesVector that will NOT trip the flag
        float[] passedInArray = new float[78];
        passedInArray[0] = 1;
        passedInArray[11] = 10;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);
    }

    @Test
    public void testPredictOnlySizeOnSensTwoTrue(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

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

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(0);
        this.model.setKeywordsSensitivity(0);
        this.model.setSizeSensitivity(2);

        // Make a FeaturesVector that will trip the flag
        float[] passedInArray = new float[78];
        passedInArray[0] = 3;
        passedInArray[11] = 4;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 1, 0);
    }

    @Test
    public void testPredictOnlySizeOnSensTwoFalse(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

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

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(0);
        this.model.setKeywordsSensitivity(0);
        this.model.setSizeSensitivity(2);

        // Make a FeaturesVector that will NOT trip the flag
        float[] passedInArray = new float[78];
        passedInArray[0] = 1;
        passedInArray[11] = 4;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);
    }

    @Test
    public void testPredictOnlySizeOnSensThreeTrue(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

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

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(0);
        this.model.setKeywordsSensitivity(0);
        this.model.setSizeSensitivity(3);

        // Make a FeaturesVector that will trip the flag
        float[] passedInArray = new float[78];
        passedInArray[0] = 1;
        passedInArray[11] = 1;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 1, 0);
    }

    @Test
    public void testPredictOnlySizeOnSensThreeFalse(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

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

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(0);
        this.model.setKeywordsSensitivity(0);
        this.model.setSizeSensitivity(3);

        // Make a FeaturesVector that will NOT trip the flag
        float[] passedInArray = new float[78];
        passedInArray[0] = 1;
        passedInArray[11] = 2;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);
    }

    @Test
    public void testPredictOnlyComplexityOnSensOneTrue(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

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

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(1);
        this.model.setKeywordsSensitivity(0);
        this.model.setSizeSensitivity(0);

        // Make a FeaturesVector that will trip the flag
        float[] passedInArray = new float[78];
        passedInArray[3] = (float)3;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 1, 0);
    }

    @Test
    public void testPredictOnlyComplexityOnSensOneFalse(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

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

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(1);
        this.model.setKeywordsSensitivity(0);
        this.model.setSizeSensitivity(0);

        // Make a FeaturesVector that will NOT trip the flag
        float[] passedInArray = new float[78];
        passedInArray[3] = (float)1;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);
    }

    @Test
    public void testPredictOnlyComplexityOnSensTwoTrue(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

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

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(2);
        this.model.setKeywordsSensitivity(0);
        this.model.setSizeSensitivity(0);

        // Make a FeaturesVector that will trip the flag
        float[] passedInArray = new float[78];
        passedInArray[3] = (float)4;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 1, 0);
    }

    @Test
    public void testPredictOnlyComplexityOnSensTwoFalse(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

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

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(2);
        this.model.setKeywordsSensitivity(0);
        this.model.setSizeSensitivity(0);

        // Make a FeaturesVector that will NOT trip the flag
        float[] passedInArray = new float[78];
        passedInArray[3] = (float)2;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);
    }

    @Test
    public void testPredictOnlyComplexityOnSensThreeTrue(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

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

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(3);
        this.model.setKeywordsSensitivity(0);
        this.model.setSizeSensitivity(0);

        // Make a FeaturesVector that will trip the flag
        float[] passedInArray = new float[78];
        passedInArray[3] = (float)5;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 1, 0);
    }

    @Test
    public void testPredictOnlyComplexityOnSensThreeFalse(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

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

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(3);
        this.model.setKeywordsSensitivity(0);
        this.model.setSizeSensitivity(0);

        // Make a FeaturesVector that will NOT trip the flag
        float[] passedInArray = new float[78];
        passedInArray[3] = (float)3;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);
    }

    @Test
    public void testPredictOnlyKeywordsOnSensOneTrue(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // This category uses the odd metrics between 17-77, filled by helper method
        float[] fvArrayValue1 = generateAndFillArray(1);
        float[] fvArrayValue2 = generateAndFillArray(2);
        float[] fvArrayValue3 = generateAndFillArray(3);
        float[] fvArrayValue4 = generateAndFillArray(4);
        float[] fvArrayValue5 = generateAndFillArray(5);
        
        //Adding these values gives:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(0);
        this.model.setKeywordsSensitivity(1);
        this.model.setSizeSensitivity(0);

        // Make a FeaturesVector that will trip the flag
        float[] passedInArray = generateAndFillArray(3);
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 1, 0);
    }

    @Test
    public void testPredictOnlyKeywordsOnSensOneFalse(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // This category uses the odd metrics between 17-77, filled by helper method
        float[] fvArrayValue1 = generateAndFillArray(1);
        float[] fvArrayValue2 = generateAndFillArray(2);
        float[] fvArrayValue3 = generateAndFillArray(3);
        float[] fvArrayValue4 = generateAndFillArray(4);
        float[] fvArrayValue5 = generateAndFillArray(5);
        
        //Adding these values gives:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(0);
        this.model.setKeywordsSensitivity(1);
        this.model.setSizeSensitivity(0);

        // Make a FeaturesVector that will NOT trip the flag
        float[] passedInArray = generateAndFillArray(1);
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);
    }

    @Test
    public void testPredictOnlyKeywordsOnSensTwoTrue(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // This category uses the odd metrics between 17-77, filled by helper method
        float[] fvArrayValue1 = generateAndFillArray(1);
        float[] fvArrayValue2 = generateAndFillArray(2);
        float[] fvArrayValue3 = generateAndFillArray(3);
        float[] fvArrayValue4 = generateAndFillArray(4);
        float[] fvArrayValue5 = generateAndFillArray(5);
        
        //Adding these values gives:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(0);
        this.model.setKeywordsSensitivity(2);
        this.model.setSizeSensitivity(0);

        // Make a FeaturesVector that will trip the flag
        float[] passedInArray = generateAndFillArray(4);
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 1, 0);
    }

    @Test
    public void testPredictOnlyKeywordsOnSensTwoFalse(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // This category uses the odd metrics between 17-77, filled by helper method
        float[] fvArrayValue1 = generateAndFillArray(1);
        float[] fvArrayValue2 = generateAndFillArray(2);
        float[] fvArrayValue3 = generateAndFillArray(3);
        float[] fvArrayValue4 = generateAndFillArray(4);
        float[] fvArrayValue5 = generateAndFillArray(5);
        
        //Adding these values gives:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(0);
        this.model.setKeywordsSensitivity(2);
        this.model.setSizeSensitivity(0);

        // Make a FeaturesVector that will NOT trip the flag
        float[] passedInArray = generateAndFillArray(2);
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);
    }

    @Test
    public void testPredictOnlyKeywordsOnSensThreeTrue(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // This category uses the odd metrics between 17-77, filled by helper method
        float[] fvArrayValue1 = generateAndFillArray(1);
        float[] fvArrayValue2 = generateAndFillArray(2);
        float[] fvArrayValue3 = generateAndFillArray(3);
        float[] fvArrayValue4 = generateAndFillArray(4);
        float[] fvArrayValue5 = generateAndFillArray(5);
        
        //Adding these values gives:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(0);
        this.model.setKeywordsSensitivity(3);
        this.model.setSizeSensitivity(0);

        // Make a FeaturesVector that will trip the flag
        float[] passedInArray = generateAndFillArray(5);
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 1, 0);
    }

    @Test
    public void testPredictOnlyKeywordsOnSensThreeFalse(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // This category uses the odd metrics between 17-77, filled by helper method
        float[] fvArrayValue1 = generateAndFillArray(1);
        float[] fvArrayValue2 = generateAndFillArray(2);
        float[] fvArrayValue3 = generateAndFillArray(3);
        float[] fvArrayValue4 = generateAndFillArray(4);
        float[] fvArrayValue5 = generateAndFillArray(5);
        
        //Adding these values gives:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120
        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());


        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(0);
        this.model.setKeywordsSensitivity(3);
        this.model.setSizeSensitivity(0);

        // Make a FeaturesVector that will NOT trip the flag
        float[] passedInArray = generateAndFillArray(3);
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);
    }

    @Test
    public void testPredictSizeComplexityTrue(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // The size category uses metrics 1 and 12, so we set those
        // Complexity uses metric 4, so that will also be set
        float[] fvArrayValue1 = new float[78];
        fvArrayValue1[0] = 0;
        fvArrayValue1[11] = 1;
        fvArrayValue1[3] = 1;

        float[] fvArrayValue2 = new float[78];
        fvArrayValue2[0] = 1;
        fvArrayValue2[11] = 4;
        fvArrayValue2[3] = 2;

        float[] fvArrayValue3 = new float[78];
        fvArrayValue3[0] = 1;
        fvArrayValue3[11] = 2;
        fvArrayValue3[3] = 3;

        float[] fvArrayValue4 = new float[78];
        fvArrayValue4[0] = 3;
        fvArrayValue4[11] = 4;
        fvArrayValue4[3] = 4;

        float[] fvArrayValue5 = new float[78];
        fvArrayValue5[0] = 1;
        fvArrayValue5[11] = 1;
        fvArrayValue5[3] = 5;
        
        //Adding these values gives size metrics of:
        // Q1 = 0.25
        // Q2 = 0.5
        // Q3 = 0.75
        //And complexity metrics of:
        // Q1 = 2
        // Q2 = 3
        // Q3 = 4

        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(1);
        this.model.setKeywordsSensitivity(0);
        this.model.setSizeSensitivity(1);

        // Make a FeaturesVector that will trip the flag
        float[] passedInArray = new float[78];
        passedInArray[0] = 1;
        passedInArray[11] = 2;
        passedInArray[3] = 3;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 1, 0);
    }

    @Test
    public void testPredictSizeComplexityFalseOneValue(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // The size category uses metrics 1 and 12, so we set those
        // Complexity uses metric 4, so that will also be set
        float[] fvArrayValue1 = new float[78];
        fvArrayValue1[0] = 0;
        fvArrayValue1[11] = 1;
        fvArrayValue1[3] = 1;

        float[] fvArrayValue2 = new float[78];
        fvArrayValue2[0] = 1;
        fvArrayValue2[11] = 4;
        fvArrayValue2[3] = 2;

        float[] fvArrayValue3 = new float[78];
        fvArrayValue3[0] = 1;
        fvArrayValue3[11] = 2;
        fvArrayValue3[3] = 3;

        float[] fvArrayValue4 = new float[78];
        fvArrayValue4[0] = 3;
        fvArrayValue4[11] = 4;
        fvArrayValue4[3] = 4;

        float[] fvArrayValue5 = new float[78];
        fvArrayValue5[0] = 1;
        fvArrayValue5[11] = 1;
        fvArrayValue5[3] = 5;
        
        //Adding these values gives size metrics of:
        // Q1 = 0.25
        // Q2 = 0.5
        // Q3 = 0.75
        //And complexity metrics of:
        // Q1 = 2
        // Q2 = 3
        // Q3 = 4

        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(1);
        this.model.setKeywordsSensitivity(0);
        this.model.setSizeSensitivity(1);

        // Make a FeaturesVector that will NOT trip the flag
        float[] passedInArray = new float[78];
        passedInArray[0] = 1;
        passedInArray[11] = 2;
        passedInArray[3] = 1;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);
    }

    @Test
    public void testPredictSizeComplexityFalseBothValues(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // The size category uses metrics 1 and 12, so we set those
        // Complexity uses metric 4, so that will also be set
        float[] fvArrayValue1 = new float[78];
        fvArrayValue1[0] = 0;
        fvArrayValue1[11] = 1;
        fvArrayValue1[3] = 1;

        float[] fvArrayValue2 = new float[78];
        fvArrayValue2[0] = 1;
        fvArrayValue2[11] = 4;
        fvArrayValue2[3] = 2;

        float[] fvArrayValue3 = new float[78];
        fvArrayValue3[0] = 1;
        fvArrayValue3[11] = 2;
        fvArrayValue3[3] = 3;

        float[] fvArrayValue4 = new float[78];
        fvArrayValue4[0] = 3;
        fvArrayValue4[11] = 4;
        fvArrayValue4[3] = 4;

        float[] fvArrayValue5 = new float[78];
        fvArrayValue5[0] = 1;
        fvArrayValue5[11] = 1;
        fvArrayValue5[3] = 5;
        
        //Adding these values gives size metrics of:
        // Q1 = 0.25
        // Q2 = 0.5
        // Q3 = 0.75
        //And complexity metrics of:
        // Q1 = 2
        // Q2 = 3
        // Q3 = 4

        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(1);
        this.model.setKeywordsSensitivity(0);
        this.model.setSizeSensitivity(1);

        // Make a FeaturesVector that will NOT trip the flag
        float[] passedInArray = new float[78];
        passedInArray[0] = 1;
        passedInArray[11] = 10;
        passedInArray[3] = 1;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);
    }

    @Test
    public void testPredictSizeKeywordsTrue(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // Keywords uses every odd metric from 17-77, so we set those with the helper method
        // The size category uses metrics 1 and 12, so we set those after the array is filled
        float[] fvArrayValue1 = generateAndFillArray(1);
        fvArrayValue1[0] = 0;
        fvArrayValue1[11] = 1;
        

        float[] fvArrayValue2 = generateAndFillArray(2);
        fvArrayValue2[0] = 1;
        fvArrayValue2[11] = 4;
        

        float[] fvArrayValue3 = generateAndFillArray(3);
        fvArrayValue3[0] = 1;
        fvArrayValue3[11] = 2;
        

        float[] fvArrayValue4 = generateAndFillArray(4);
        fvArrayValue4[0] = 3;
        fvArrayValue4[11] = 4;
        

        float[] fvArrayValue5 = generateAndFillArray(5);
        fvArrayValue5[0] = 1;
        fvArrayValue5[11] = 1;
        
        
        //Adding these values gives size metrics of:
        // Q1 = 0.25
        // Q2 = 0.5
        // Q3 = 0.75
        //And keywords metrics of:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120

        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(0);
        this.model.setKeywordsSensitivity(1);
        this.model.setSizeSensitivity(1);

        // Make a FeaturesVector that will trip the flag
        float[] passedInArray = generateAndFillArray(3);
        passedInArray[0] = 1;
        passedInArray[11] = 2;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 1, 0);
    }

    @Test
    public void testPredictSizeKeywordsFalseOneValue(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // Keywords uses every odd metric from 17-77, so we set those with the helper method
        // The size category uses metrics 1 and 12, so we set those after the array is filled
        float[] fvArrayValue1 = generateAndFillArray(1);
        fvArrayValue1[0] = 0;
        fvArrayValue1[11] = 1;
        

        float[] fvArrayValue2 = generateAndFillArray(2);
        fvArrayValue2[0] = 1;
        fvArrayValue2[11] = 4;
        

        float[] fvArrayValue3 = generateAndFillArray(3);
        fvArrayValue3[0] = 1;
        fvArrayValue3[11] = 2;
        

        float[] fvArrayValue4 = generateAndFillArray(4);
        fvArrayValue4[0] = 3;
        fvArrayValue4[11] = 4;
        

        float[] fvArrayValue5 = generateAndFillArray(5);
        fvArrayValue5[0] = 1;
        fvArrayValue5[11] = 1;
        
        
        //Adding these values gives size metrics of:
        // Q1 = 0.25
        // Q2 = 0.5
        // Q3 = 0.75
        //And keywords metrics of:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120

        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(0);
        this.model.setKeywordsSensitivity(1);
        this.model.setSizeSensitivity(1);

        // Make a FeaturesVector that will NOT trip the flag
        float[] passedInArray = generateAndFillArray(1);
        passedInArray[0] = 1;
        passedInArray[11] = 2;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);
    }

    @Test
    public void testPredictSizeKeywordsFalseBothValues(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // Keywords uses every odd metric from 17-77, so we set those with the helper method
        // The size category uses metrics 1 and 12, so we set those after the array is filled
        float[] fvArrayValue1 = generateAndFillArray(1);
        fvArrayValue1[0] = 0;
        fvArrayValue1[11] = 1;
        

        float[] fvArrayValue2 = generateAndFillArray(2);
        fvArrayValue2[0] = 1;
        fvArrayValue2[11] = 4;
        

        float[] fvArrayValue3 = generateAndFillArray(3);
        fvArrayValue3[0] = 1;
        fvArrayValue3[11] = 2;
        

        float[] fvArrayValue4 = generateAndFillArray(4);
        fvArrayValue4[0] = 3;
        fvArrayValue4[11] = 4;
        

        float[] fvArrayValue5 = generateAndFillArray(5);
        fvArrayValue5[0] = 1;
        fvArrayValue5[11] = 1;
        
        
        //Adding these values gives size metrics of:
        // Q1 = 0.25
        // Q2 = 0.5
        // Q3 = 0.75
        //And keywords metrics of:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120

        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(0);
        this.model.setKeywordsSensitivity(1);
        this.model.setSizeSensitivity(1);

        // Make a FeaturesVector that will NOT trip the flag
        float[] passedInArray = generateAndFillArray(1);
        passedInArray[0] = 1;
        passedInArray[11] = 10;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);
    }

    @Test
    public void testPredictComplexityKeywordsTrue(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // Keywords uses every odd metric from 17-77, so we set those with the helper method
        // The complexity category uses metric 4 so we set those after the array is filled
        float[] fvArrayValue1 = generateAndFillArray(1);
        fvArrayValue1[3] = 1;

        float[] fvArrayValue2 = generateAndFillArray(2);
        fvArrayValue2[3] = 2;

        float[] fvArrayValue3 = generateAndFillArray(3);
        fvArrayValue3[3] = 3;
    
        float[] fvArrayValue4 = generateAndFillArray(4);
        fvArrayValue4[3] = 4;
        
        float[] fvArrayValue5 = generateAndFillArray(5);
        fvArrayValue5[3] = 5;
        
        //Adding these values gives complexity metrics of:
        // Q1 = 2
        // Q2 = 3
        // Q3 = 4
        //And keywords metrics of:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120

        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(1);
        this.model.setKeywordsSensitivity(1);
        this.model.setSizeSensitivity(0);

        // Make a FeaturesVector that will trip the flag
        float[] passedInArray = generateAndFillArray(3);
        passedInArray[3] = 3;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 1, 0);
    }

    @Test
    public void testPredictComplexityKeywordsFalseOneValue(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // Keywords uses every odd metric from 17-77, so we set those with the helper method
        // The complexity category uses metric 4 so we set those after the array is filled
        float[] fvArrayValue1 = generateAndFillArray(1);
        fvArrayValue1[3] = 1;

        float[] fvArrayValue2 = generateAndFillArray(2);
        fvArrayValue2[3] = 2;

        float[] fvArrayValue3 = generateAndFillArray(3);
        fvArrayValue3[3] = 3;
    
        float[] fvArrayValue4 = generateAndFillArray(4);
        fvArrayValue4[3] = 4;
        
        float[] fvArrayValue5 = generateAndFillArray(5);
        fvArrayValue5[3] = 5;
        
        //Adding these values gives complexity metrics of:
        // Q1 = 2
        // Q2 = 3
        // Q3 = 4
        //And keywords metrics of:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120

        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(1);
        this.model.setKeywordsSensitivity(1);
        this.model.setSizeSensitivity(0);

        // Make a FeaturesVector that will NOT trip the flag
        float[] passedInArray = generateAndFillArray(1);
        passedInArray[3] = 3;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);
    }

    @Test
    public void testPredictComplexityKeywordsFalseBothValues(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // Keywords uses every odd metric from 17-77, so we set those with the helper method
        // The complexity category uses metric 4 so we set those after the array is filled
        float[] fvArrayValue1 = generateAndFillArray(1);
        fvArrayValue1[3] = 1;

        float[] fvArrayValue2 = generateAndFillArray(2);
        fvArrayValue2[3] = 2;

        float[] fvArrayValue3 = generateAndFillArray(3);
        fvArrayValue3[3] = 3;
    
        float[] fvArrayValue4 = generateAndFillArray(4);
        fvArrayValue4[3] = 4;
        
        float[] fvArrayValue5 = generateAndFillArray(5);
        fvArrayValue5[3] = 5;
        
        //Adding these values gives complexity metrics of:
        // Q1 = 2
        // Q2 = 3
        // Q3 = 4
        //And keywords metrics of:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120

        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(1);
        this.model.setKeywordsSensitivity(1);
        this.model.setSizeSensitivity(0);

        // Make a FeaturesVector that will NOT trip the flag
        float[] passedInArray = generateAndFillArray(1);
        passedInArray[3] = 1;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);
    }

    @Test
    public void testPredictAllFlagsTrue(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // Keywords uses every odd metric from 17-77, so we set those with the helper method
        // The size category uses metrics 1 and 12, so we set those after the array is filled
        // The complexity category uses metric 4, so we set that afterwards as well
        float[] fvArrayValue1 = generateAndFillArray(1);
        fvArrayValue1[0] = 0;
        fvArrayValue1[11] = 1;
        fvArrayValue1[3] = 1;
        

        float[] fvArrayValue2 = generateAndFillArray(2);
        fvArrayValue2[0] = 1;
        fvArrayValue2[11] = 4;
        fvArrayValue2[3] = 2;

        float[] fvArrayValue3 = generateAndFillArray(3);
        fvArrayValue3[0] = 1;
        fvArrayValue3[11] = 2;
        fvArrayValue3[3] = 3;

        float[] fvArrayValue4 = generateAndFillArray(4);
        fvArrayValue4[0] = 3;
        fvArrayValue4[11] = 4;
        fvArrayValue4[3] = 4;

        float[] fvArrayValue5 = generateAndFillArray(5);
        fvArrayValue5[0] = 1;
        fvArrayValue5[11] = 1;
        fvArrayValue5[3] = 5;
        
        //Adding these values gives size metrics of:
        // Q1 = 0.25
        // Q2 = 0.5
        // Q3 = 0.75
        //And keywords metrics of:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120
        //And complexity metrics of:
        // Q1 = 2
        // Q2 = 3
        // Q3 = 4

        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(1);
        this.model.setKeywordsSensitivity(1);
        this.model.setSizeSensitivity(1);

        // Make a FeaturesVector that will trip the flag
        float[] passedInArray = generateAndFillArray(3);
        passedInArray[0] = 1;
        passedInArray[11] = 2;
        passedInArray[3] = 3;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 1, 0);
    }

    @Test
    public void testPredictAllFlagsFalseOneValue(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // Keywords uses every odd metric from 17-77, so we set those with the helper method
        // The size category uses metrics 1 and 12, so we set those after the array is filled
        // The complexity category uses metric 4, so we set that afterwards as well
        float[] fvArrayValue1 = generateAndFillArray(1);
        fvArrayValue1[0] = 0;
        fvArrayValue1[11] = 1;
        fvArrayValue1[3] = 1;
        

        float[] fvArrayValue2 = generateAndFillArray(2);
        fvArrayValue2[0] = 1;
        fvArrayValue2[11] = 4;
        fvArrayValue2[3] = 2;

        float[] fvArrayValue3 = generateAndFillArray(3);
        fvArrayValue3[0] = 1;
        fvArrayValue3[11] = 2;
        fvArrayValue3[3] = 3;

        float[] fvArrayValue4 = generateAndFillArray(4);
        fvArrayValue4[0] = 3;
        fvArrayValue4[11] = 4;
        fvArrayValue4[3] = 4;

        float[] fvArrayValue5 = generateAndFillArray(5);
        fvArrayValue5[0] = 1;
        fvArrayValue5[11] = 1;
        fvArrayValue5[3] = 5;
        
        //Adding these values gives size metrics of:
        // Q1 = 0.25
        // Q2 = 0.5
        // Q3 = 0.75
        //And keywords metrics of:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120
        //And complexity metrics of:
        // Q1 = 2
        // Q2 = 3
        // Q3 = 4

        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(1);
        this.model.setKeywordsSensitivity(1);
        this.model.setSizeSensitivity(1);

        // Make a FeaturesVector that will NOT trip the flag
        float[] passedInArray = generateAndFillArray(3);
        passedInArray[0] = 1;
        passedInArray[11] = 2;
        passedInArray[3] = 1;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);
    }

    @Test
    public void testPredictAllFlagsFalseTwoValues(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // Keywords uses every odd metric from 17-77, so we set those with the helper method
        // The size category uses metrics 1 and 12, so we set those after the array is filled
        // The complexity category uses metric 4, so we set that afterwards as well
        float[] fvArrayValue1 = generateAndFillArray(1);
        fvArrayValue1[0] = 0;
        fvArrayValue1[11] = 1;
        fvArrayValue1[3] = 1;
        

        float[] fvArrayValue2 = generateAndFillArray(2);
        fvArrayValue2[0] = 1;
        fvArrayValue2[11] = 4;
        fvArrayValue2[3] = 2;

        float[] fvArrayValue3 = generateAndFillArray(3);
        fvArrayValue3[0] = 1;
        fvArrayValue3[11] = 2;
        fvArrayValue3[3] = 3;

        float[] fvArrayValue4 = generateAndFillArray(4);
        fvArrayValue4[0] = 3;
        fvArrayValue4[11] = 4;
        fvArrayValue4[3] = 4;

        float[] fvArrayValue5 = generateAndFillArray(5);
        fvArrayValue5[0] = 1;
        fvArrayValue5[11] = 1;
        fvArrayValue5[3] = 5;
        
        //Adding these values gives size metrics of:
        // Q1 = 0.25
        // Q2 = 0.5
        // Q3 = 0.75
        //And keywords metrics of:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120
        //And complexity metrics of:
        // Q1 = 2
        // Q2 = 3
        // Q3 = 4

        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(1);
        this.model.setKeywordsSensitivity(1);
        this.model.setSizeSensitivity(1);

        // Make a FeaturesVector that will NOT trip the flag
        float[] passedInArray = generateAndFillArray(3);
        passedInArray[0] = 1;
        passedInArray[11] = 10;
        passedInArray[3] = 1;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);
    }

    @Test
    public void testPredictAllFlagsFalseThreeValues(){
        List<FeaturesVector> fvList = new ArrayList<FeaturesVector>();

        // Keywords uses every odd metric from 17-77, so we set those with the helper method
        // The size category uses metrics 1 and 12, so we set those after the array is filled
        // The complexity category uses metric 4, so we set that afterwards as well
        float[] fvArrayValue1 = generateAndFillArray(1);
        fvArrayValue1[0] = 0;
        fvArrayValue1[11] = 1;
        fvArrayValue1[3] = 1;
        

        float[] fvArrayValue2 = generateAndFillArray(2);
        fvArrayValue2[0] = 1;
        fvArrayValue2[11] = 4;
        fvArrayValue2[3] = 2;

        float[] fvArrayValue3 = generateAndFillArray(3);
        fvArrayValue3[0] = 1;
        fvArrayValue3[11] = 2;
        fvArrayValue3[3] = 3;

        float[] fvArrayValue4 = generateAndFillArray(4);
        fvArrayValue4[0] = 3;
        fvArrayValue4[11] = 4;
        fvArrayValue4[3] = 4;

        float[] fvArrayValue5 = generateAndFillArray(5);
        fvArrayValue5[0] = 1;
        fvArrayValue5[11] = 1;
        fvArrayValue5[3] = 5;
        
        //Adding these values gives size metrics of:
        // Q1 = 0.25
        // Q2 = 0.5
        // Q3 = 0.75
        //And keywords metrics of:
        // Q1 = 60
        // Q2 = 90
        // Q3 = 120
        //And complexity metrics of:
        // Q1 = 2
        // Q2 = 3
        // Q3 = 4

        fvList.add(new FeaturesVectorMock(fvArrayValue1).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue2).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue3).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue4).getMock());
        fvList.add(new FeaturesVectorMock(fvArrayValue5).getMock());

        MetricsGathererMock mockMg = new MetricsGathererMock(fvList);
        this.model.initMetricsGathererAndMetricsFlags(mockMg.getMock());
        this.model.setComplexitySensitivity(1);
        this.model.setKeywordsSensitivity(1);
        this.model.setSizeSensitivity(1);

        // Make a FeaturesVector that will NOT trip the flag
        float[] passedInArray = generateAndFillArray(1);
        passedInArray[0] = 1;
        passedInArray[11] = 10;
        passedInArray[3] = 1;
        FeaturesVectorMock passedInFv = new FeaturesVectorMock(passedInArray);

        assertEquals(model.predict(passedInFv.getMock()), 0, 0);
    }
}