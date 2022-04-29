package org.jetbrains.research.anticopypaster.models;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.research.anticopypaster.ide.AntiCopyPastePreProcessor;
import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;


public class TensorflowModel extends PredictionModel {
    private final String modelResourcePath = "TrainedModel";
    private final SavedModelBundle modelBundle;
    static final Logger LOG = Logger.getInstance(AntiCopyPastePreProcessor.class);

    public TensorflowModel(){
        modelBundle = loadModel(modelResourcePath);
    }

    private static SavedModelBundle loadModel(String modelResourcePath) {
        try {
            copyFromJar(modelResourcePath, Paths.get(modelResourcePath));
        } catch (URISyntaxException | IOException | FileSystemAlreadyExistsException e) {
            LOG.warn("[ACP] Issues when reading the model" + e.getMessage());
        }

        try {
            return SavedModelBundle.load(modelResourcePath, "serve");
        } catch (Exception ex) {
            LOG.error("[ACP] Could not load the model" + ex.getMessage());
        }
        return null;
    }

    @Override
    public float predict(FeaturesVector featuresVector) {
        // create the session from the Bundle
        Session session = modelBundle.session();
        float[] featuresArray = featuresVector.buildArray();

        FloatBuffer floatBuffer = FloatBuffer.wrap(featuresArray);

        long[] shape = new long[]{1, 78, 1};

        // create an input Tensor
        Tensor<Float> x = Tensor.create(shape, floatBuffer);

        return runModel(session, x,
                "serving_default_batch_normalization_input:0",
                "StatefulPartitionedCall:0");
    }

    /**
     * runs the tensorflow model from the given Session, and input Tensor.
     * to get inputLayerName and outputLayerName, run
     * `saved_model_cli show --dir <model> --tag_set serve --signature_def serving_default`
     * where <model> is name of directory to where the model was saved.
     */
    private float runModel(Session session, Tensor<Float> inputTensor,
                           String inputLayerName, String outputLayerName) {

        Tensor<?> result = session.runner()
                .feed(inputLayerName, inputTensor)
                .fetch(outputLayerName)
                .run()
                .get(0);

        float[][] m = new float[1][1];
        result.copyTo(m);
        float positive_proba = m[0][0];

        return positive_proba;
    }

    /**
     * Copies resource directory 'source' from jar,
     * into 'target' in gradle cache
     */
    public static void copyFromJar(String source, final Path target) throws URISyntaxException, IOException {
        URI resource = TensorflowModel.class.getClassLoader().getResource(source).toURI();
        FileSystem fileSystem = FileSystems.newFileSystem(
                resource,
                Collections.<String, String>emptyMap()
        );

        final Path jarPath = fileSystem.getPath(source);

        Files.walkFileTree(jarPath, new SimpleFileVisitor<Path>() {
            private Path currentTarget;

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                currentTarget = target.resolve(jarPath.relativize(dir).toString());
                Files.createDirectories(currentTarget);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(jarPath.relativize(file).toString()), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
