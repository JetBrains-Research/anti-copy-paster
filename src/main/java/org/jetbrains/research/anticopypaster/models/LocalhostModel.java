package org.jetbrains.research.anticopypaster.models;


import java.io.IOException;
import java.util.Arrays;
import java.net.http.*;
import java.net.URI;

/**
 * Attributes:
 * port - Localhost port running development inference server
 * client - HTTP client used to send requests to inference server
 */
public class LocalhostModel extends PredictionModel {
    
    private final Integer port = 4500;
    private HttpClient client;

    public LocalhostModel() {
        this.client = HttpClient.newHttpClient();
    }
    
    @Override
    public float predict(FeaturesVector featuresVector) throws IOException, InterruptedException {
        // Serialize feature vector
        String body = Arrays.toString(featuresVector.buildArray());
        // Build uri object
        URI uri = URI.create(String.format("http://0.0.0.0:%d/", port));
        // Build request object
        var req= HttpRequest.newBuilder(uri)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .version(HttpClient.Version.HTTP_1_1)
                .header("accept", "application/json")
                .build();
        // Send request via client
        var rsp = client.send(req, HttpResponse.BodyHandlers.ofString());
        return Float.parseFloat(rsp.body());
    }

}