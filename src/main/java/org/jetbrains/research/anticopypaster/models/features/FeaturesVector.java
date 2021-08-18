package org.jetbrains.research.anticopypaster.models.features;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class FeaturesVector implements IFeaturesVector {
    private final List<IFeatureItem> features = new ArrayList<>();
    private final int dimension;

    public FeaturesVector(int dimension) {
        this.dimension = dimension;
    }

    public void addFeature(final IFeatureItem item) {
        this.features.add(item);
    }

    public int getDimension() {
        return dimension;
    }

    public List<Integer> getMissingFeaturesId() {
        List<Integer> range = IntStream.range(0, dimension)
                .boxed().collect(Collectors.toList());
        features.forEach(c -> range.remove(c.getId()));

        return range;
    }

    @Override
    public double getFeature(Feature toSearch) {
        for (IFeatureItem item : features) {
            if (item.getId() == toSearch.getId()) {
                return item.getValue();
            }
        }

        return 0.0;
    }

    public Object[] getFeatures() {
        return features.stream().map(IFeatureItem::getValue).toArray();
    }

    public List<Float> buildVector() {
        features.sort(Comparator.comparingInt(IFeatureItem::getId));
        int itemsPtr = 0;
        List<Float> result = new ArrayList<>();
        for (int i = 0; i < dimension; ++i) {
            if (itemsPtr != features.size() && features.get(itemsPtr).getId() == i) {
                result.add((float) features.get(itemsPtr++).getValue());
            } else {
                result.add(0f);
            }
        }

        return result;
    }
}
