package org.jetbrains.research.anticopypaster.models;

import org.jetbrains.research.anticopypaster.models.features.feature.Feature;
import org.jetbrains.research.anticopypaster.models.features.features_vector.IFeaturesVector;

public class VectorValidator {
    public static boolean isValid(IFeaturesVector featuresVector) {
        boolean hasVisibilityModifier = featuresVector.getFeature(Feature.KeywordPrivateTotalCount) > 0.0 ||
                featuresVector.getFeature(Feature.KeywordPublicTotalCount) > 0.0 ||
                featuresVector.getFeature(Feature.KeywordProtectedTotalCount) > 0.0;

        boolean hasClassModifiers = featuresVector.getFeature(Feature.KeywordClassTotalCount) > 0.0 ||
                featuresVector.getFeature(Feature.KeywordPackageTotalCount) > 0.0 ||
                featuresVector.getFeature(Feature.KeywordImportCountPerLine) > 0.0 ||
                featuresVector.getFeature(Feature.KeywordStaticTotalCount) > 0.0 ||
                featuresVector.getFeature(Feature.KeywordAbstractTotalCount) > 0.0 ||
                featuresVector.getFeature(Feature.KeywordImplementsTotalCount) > 0.0 ||
                featuresVector.getFeature(Feature.KeywordThrowsTotalCount) > 0.0 ||
                featuresVector.getFeature(Feature.KeywordEnumTotalCount) > 0.0 ||
                featuresVector.getFeature(Feature.KeywordDefaultTotalCount) > 0.0 ||
                featuresVector.getFeature(Feature.KeywordNativeTotalCount) > 0.0 ||
                featuresVector.getFeature(Feature.KeywordExtendsTotalCount) > 0.0 ||
                featuresVector.getFeature(Feature.KeywordInterfaceTotalCount) > 0.0;

        if (hasVisibilityModifier || hasClassModifiers) {
            return false;
        }



        return true;
    }
}
