package org.jetbrains.research.anticopypaster.models.features.features_vector;

import org.jetbrains.research.anticopypaster.models.features.feature.Feature;
import org.jetbrains.research.anticopypaster.models.features.feature.IFeatureItem;

import java.util.List;

public interface IFeaturesVector {
    void addFeature(final IFeatureItem item);
    double getFeature(final Feature item);
    List<Float> buildVector();
}
