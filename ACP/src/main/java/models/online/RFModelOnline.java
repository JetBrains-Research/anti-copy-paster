package models.online;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.*;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.discovery.Discovery;
import com.google.api.services.discovery.model.JsonSchema;
import com.google.api.services.discovery.model.RestDescription;
import com.google.api.services.discovery.model.RestMethod;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import credentials.Credentials;
import models.IPredictionModel;
import models.features.features_vector.IFeaturesVector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RFModelOnline implements IPredictionModel {
    private RestMethod method;
    private GenericUrl url;
    private JsonFactory jsonFactory;
    private HttpRequestFactory requestFactory;

    public RFModelOnline(final String projectId, final String modelId, final String versionId) throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        Discovery discovery = new Discovery.Builder(httpTransport, jsonFactory, null).build();
        RestDescription api = discovery.apis().getRest("ml", "v1").execute();
        RestMethod method = api.getResources().get("projects").getMethods().get("predict");
        JsonSchema param = new JsonSchema();

        param.set("name", String.format("projects/%s/models/%s/versions/%s", projectId, modelId, versionId));
        GenericUrl url = new GenericUrl(UriTemplate.expand(api.getBaseUrl() + method.getPath(), param, true));

        List<String> scopes = Collections.singletonList("https://www.googleapis.com/auth/cloud-platform");

        GoogleCredentials credential = ServiceAccountCredentials.fromStream(Credentials.toStream()).createScoped(scopes);

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);
        HttpRequestFactory requestFactory = httpTransport.createRequestFactory(requestInitializer);

        this.jsonFactory = jsonFactory;
        this.method = method;
        this.url = url;
        this.requestFactory = requestFactory;
    }

    @Override
    public List<Integer> predict(List<IFeaturesVector> batch) throws IOException {
        Map<String, Object> data = new HashMap<>();
        List<List<Float>> dataList = batch.stream().map(IFeaturesVector::buildVector).collect(Collectors.toList());

        data.put("instances", dataList);
        HttpRequest request = requestFactory.buildRequest(method.getHttpMethod(), url, new JsonHttpContent(jsonFactory, data));
        request.setParser(new JsonObjectParser(jsonFactory));
        HttpResponse response = request.execute();

        try {
            GenericJson json = response.parseAs(GenericJson.class);
            int[] result = ((List<BigDecimal>) json.get("predictions"))
                    .stream()
                    .mapToInt(BigDecimal::intValue)
                    .toArray();

            return Arrays.stream(result).boxed().collect(Collectors.toList());
            //return result;
            //System.out.println(ss);
        } finally {
            response.disconnect();
        }
    }
}
