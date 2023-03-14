package org.jetbrains.research.anticopypaster.utils;

import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;

public interface Flag{

    public int changeSensitivity(int sensitivity);
    public boolean isFlagTriggered(FeaturesVector featuresVector);

}