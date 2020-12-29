package builders;

import models.features.feature.Feature;
import models.features.features_vector.IFeaturesVector;

import java.util.ArrayList;
import java.util.List;

public class DecisionPathBuilder {
    private String treeRepresentation;

    public DecisionPathBuilder(String treeRepresentation) {
        this.treeRepresentation = treeRepresentation;

    }

    public String collect(List<PathItem> path) {

        StringBuilder resBuilder = new StringBuilder();
        for (PathItem pi: path) {
            if (pi.rawAttribute.isEmpty()) {
                continue;
            }
            resBuilder.append(pi.rawAttribute);
             if (pi.value < pi.split) {
                 resBuilder.append(" is less than ");
                 resBuilder.append(pi.split);
             } else {
                 resBuilder.append(" is greater than or equal ");
                 resBuilder.append(pi.split);
             }

            resBuilder.append(", ");
        }

        String res = resBuilder.toString();

        return res.substring(0, res.length() - 2);
    }

    public List<PathItem> buildPath(IFeaturesVector vec) {
        List<PathItem> res = new ArrayList<>();
        String[] represent = treeRepresentation.split("\n");
        traverse(res, vec, represent);

        return res;
    }

    private void traverse(List<PathItem> res, IFeaturesVector vec, String[] nodes) {
        if (nodes.length == 0) {
            return;
        }

        List<String> rest = new ArrayList<>();

        int secondIndex = -1;
        for (int i = 1; i < nodes.length; ++i) {
            if (nodes[i].charAt(0) != '|') {
                secondIndex = i;
                break;
            }
        }

        String[] p0 = nodes[0].split(" ");

        double split = Double.parseDouble(p0[2]);
        Feature feature = Feature.valueOf(p0[0]);
        double value = vec.getFeature(feature);

        res.add(new PathItem(feature.getCyrName(), (float)split, (float)value));

        if (value < split) {
            for (int i = 1; i < secondIndex; i++) {
                rest.add(nodes[i].substring(4));
            }
        } else {
            for (int i = secondIndex + 1; i < nodes.length; i++) {
                rest.add(nodes[i].substring(4));
            }
        }

        String[] restArr = new String[rest.size()];
        for (int i = 0; i < rest.size(); ++i) {
            restArr[i] = rest.get(i);
        }

        traverse(res, vec, restArr);
    }

    public static class PathItem {
        private String rawAttribute;
        private float split;
        private float value;

        public PathItem(String rawAttribute, float split, float value) {
            this.rawAttribute = rawAttribute;
            this.split = split;
            this.value = value;
        }
    }
}
