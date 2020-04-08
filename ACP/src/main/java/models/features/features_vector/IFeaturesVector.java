package models.features.features_vector;

import models.features.feature.Feature;
import models.features.feature.IFeatureItem;

import java.util.List;

public interface IFeaturesVector {
    void addFeature(final IFeatureItem item);
    double getFeature(final Feature item);
    List<Float> buildVector();
}
