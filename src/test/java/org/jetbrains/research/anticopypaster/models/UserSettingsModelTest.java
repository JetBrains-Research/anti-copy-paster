package org.jetbrains.research.anticopypaster.models;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserSettingsModelTest {


    /**
   
    Mock metrics gatherer needs to go here

    */     


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

}