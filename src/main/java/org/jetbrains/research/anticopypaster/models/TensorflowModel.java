package org.jetbrains.research.anticopypaster.models;

import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Tensor;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;

/**
 * Attributes:
 * modelResourcePath - String-name of directory in resource folder corresponding to the TF model.
 * modelBundle - TF model loaded from resources.
 * inputLayerName - name of the model's input layer, see wiki for details.
 * outputLayerName - name of the model's output layer, see wiki for details.
 */
public class TensorflowModel extends PredictionModel {
    private final String modelResourcePath = "TrainedModel";
    private final String inputLayerName = "serving_default_batch_normalization_input:0";
    private final String outputLayerName = "StatefulPartitionedCall:0";
    private final SavedModelBundle modelBundle;

    static private final Logger LOG = Logger.getInstance(TensorflowModel.class);

    public TensorflowModel() {
        TensorflowNativeLibraryLoader.load();
        modelBundle = loadModel(modelResourcePath);
    }

    private static SavedModelBundle loadModel(String modelResourcePath) {
        Path target = Paths.get(PathManager.getPluginsPath(), "ACP", modelResourcePath).toAbsolutePath();
        if (copyFromJar(modelResourcePath, target) ) {
            try {
                return SavedModelBundle.load(target.toString(), "serve");
            } catch (Exception e) {
                LOG.error("[ACP] Could not load the model" + e.getMessage());
            }
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

        return runModel(session, x);
    }

    /**
     * runs the tensorflow model from the given Session, and input Tensor.
     * to get inputLayerName and outputLayerName, run
     * `saved_model_cli show --dir <model> --tag_set serve --signature_def serving_default`
     * where <model> is name of directory to where the model was saved.
     */
    private float runModel(Session session, Tensor<Float> inputTensor) {

        Tensor<?> result = session.runner()
                .feed(inputLayerName, inputTensor)
                .fetch(outputLayerName)
                .run()
                .get(0);

        float[][] outputMatrix = new float[1][1];
        result.copyTo(outputMatrix);

        float positiveProbability = outputMatrix[0][0];

        return positiveProbability;
    }

    /**
     * Copies resource directory 'source' from jar into 'target'
     */
    public static boolean copyFromJar(String source, final Path target) {
        try (FileSystem fileSystem = FileSystems.newFileSystem(
                TensorflowModel.class.getClassLoader().getResource(source).toURI(),
                Collections.<String, String>emptyMap()
        )) {
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
            return true;
        } catch (URISyntaxException | IOException e) {
            LOG.error("[ACP] Issues when reading the model" + e.getMessage());
        } catch (FileSystemAlreadyExistsException e) {
            LOG.warn("[ACP] Trying make a redundant copy of model-resource");
        }
        return false;
    }
}
