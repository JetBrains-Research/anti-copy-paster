package models.features.features_vector;

import models.features.feature.IFeatureItem;

import java.util.List;

public interface IFeaturesVector {
    void addFeature(final IFeatureItem item);
    List<Float> buildVector();
}
