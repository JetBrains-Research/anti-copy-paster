package org.jetbrains.research.anticopypaster.models;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.research.anticopypaster.ide.AntiCopyPastePreProcessor;
import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.List;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;
import org.tensorflow.TensorFlow;


public interface TensorflowModel {
    URL modelUrl = TensorflowModel.class.getClassLoader().getResource("tf_model");
    static final Logger LOG = Logger.getInstance(AntiCopyPastePreProcessor.class);

    static float getClassificationValue(FeaturesVector featuresVector) {
        // load the model Bundle
        File file = null;
        try {
            assert modelUrl != null;
            file = new File(modelUrl.getPath());
        } catch (Exception ex) {
            LOG.error("[ACP] Improper path to prediction model");
        }

        SavedModelBundle b = SavedModelBundle.load(file.toString(), "serve");

        // create the session from the Bundle
        Session sess = b.session();
        float[] featuresArray = featuresVector.buildArray();

        FloatBuffer floatBuffer  =  FloatBuffer.wrap(featuresArray);

        long[] shape = new long[]{1, 78, 1};

        // create an input Tensor, value = 2.0f
        Tensor<?> x = Tensor.create(shape, floatBuffer);

        // run the model and get the result, 4.0f.
        Tensor<?> result = sess.runner()
                .feed("serving_default_input_1:0", x)
                .fetch("StatefulPartitionedCall:0")
                .run()
                .get(0);
        // print out the result.

        float[][] m = new float[1][1];
        result.copyTo(m);
        float positive_proba = m[0][0];

        return positive_proba;
    }

    static File getFileFromPath(URL url) {
        File file = null;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            file = new File(url.getPath());
        }
        return file;
    }

}
