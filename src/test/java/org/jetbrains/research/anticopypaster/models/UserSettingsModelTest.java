package org.jetbrains.research.anticopypaster.models;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserSettingsModelTest {

    private UserSettingsModel model;

    /**
    Create a new model before each test
     */
    @BeforeEach
    public void beforeTest(){
        model = new UserSettingsModel();
    }

    /**
    This is a test of the skeleton version of the model. This just tests that the predict
    method returns 1 as expected. This will be changed out in the future as we pass in a feature
    vector once the flag logic has been added. The third parameter is the delta, which is needed
    to assert floating point values, 0 just means they need to be exactly the same.
     */
    @Test
    public void testPredictSkeleton(){
        assertEquals(model.predict(null), 1, 0);
    }

}