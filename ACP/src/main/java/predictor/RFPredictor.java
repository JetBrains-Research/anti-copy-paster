package predictor;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.*;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.discovery.Discovery;
import com.google.api.services.discovery.model.JsonSchema;
import com.google.api.services.discovery.model.RestDescription;
import com.google.api.services.discovery.model.RestMethod;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.StorageOptions;
import credentials.Credentials;
import models.IPredictionModel;
import models.features.feature.Feature;
import models.features.feature.FeatureItem;
import models.features.features_vector.FeaturesVector;
import models.features.features_vector.IFeaturesVector;
import models.online.RFModelOnline;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

public class RFPredictor {
    public static void main(String[] args) throws GeneralSecurityException, IOException {
        IPredictionModel model = new RFModelOnline("anti-copy-paster", "RFBaseline1", "RFBase1");
        List<IFeaturesVector> req = new ArrayList<>();
        Random r = new Random();

        for (int k = 0; k < 64; ++k) {
            IFeaturesVector fv = new FeaturesVector(112);

            for (int i = 0; i < 112; ++i) {
                fv.addFeature(new FeatureItem(Feature.fromId(i), 1));
            }

            req.add(fv);
        }

        List<Integer> res = model.predict(req);


        System.out.println(res);
    }

}
