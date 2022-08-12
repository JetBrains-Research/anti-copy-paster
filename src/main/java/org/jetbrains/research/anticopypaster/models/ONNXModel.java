package org.jetbrains.research.anticopypaster.models;

import com.intellij.openapi.diagnostic.Logger;
import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import org.jetbrains.research.extractMethod.metrics.features.FeaturesVector;

import java.io.IOException;
import java.util.Map;


/**
 * Attributes:
 * modelResourcePath - String-name of directory in resource folder corresponding to the TF model.
 * modelBundle - TF model loaded from resources.
 * inputLayerName - name of the model's input layer, see wiki for details.
 * outputLayerName - name of the model's output layer, see wiki for details.
 */
public class ONNXHandler extends PredictionModel {
    private OrtEnvironment env;
    private OrtSession session;
    private final String modelFile = "cnn_model.onnx";
    private final static String inputLabel = "features";
    private final static String outputLabel = "dense_1";
    private final float defaultOuput = 0f;
    private static final Logger logger = Logger.getInstance(ONNXHandler.class);

    public ONNXHandler() {
        env= OrtEnvironment.getEnvironment();
        try {
            var model = ONNXHandler.class.getClassLoader().getResourceAsStream(modelFile).readAllBytes();
            session = env.createSession(model);
        } catch (OrtException e) {
            logger.error("[ACP] Create OnnxSession from OnnxEnvironment" + e.getMessage());
        } catch (IOException e) {
            logger.error("[ACP] Couldn't open Onnx model from file" + e.getMessage());
        }
    }

    private float postprocess(OrtSession.Result result) {
        try {
            return ((float[][]) result.get(outputLabel).get().getValue())[0][0];
        } catch (OrtException e) {
            logger.error("[ACP] Couldn't get value from OnnxSession.Result" + e.getMessage());
            return defaultOuput;
        }
    }

    public float predict(FeaturesVector featuresVector) {
        var features = featuresVector.buildArray();

        try {
            var tensor = OnnxTensor.createTensor(env, featuresVector);
            var inputs = Map.of(inputLabel, tensor);

            try {
                var result = session.run(inputs);
                return postprocess(result);
            } catch (OrtException e) {
                logger.error("[ACP] Couldn't execute Onnx model forward pass" + e.getMessage());
                return defaultOuput;
            }
        } catch (OrtException e) {
            logger.error("[ACP] Couldn't create OnnxTensor" + e.getMessage());
            return defaultOuput;
        }
    }

}
