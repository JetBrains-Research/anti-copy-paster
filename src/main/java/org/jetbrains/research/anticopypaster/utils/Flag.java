package org.jetbrains.research.anticopypaster.utils;

import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;

public interface Flag{

    public void changeSensitivity(int sensitivity);
    public boolean isFlagTriggered(FeaturesVector featuresVector);

}