package loggers;

import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import credentials.Credentials;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class CloudLogger implements ILogger {
    private Storage cloudStorage;
    private ThreadPoolExecutor logPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

    public CloudLogger() throws Exception {
        cloudStorage = StorageOptions
                .newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(Credentials.toStream()))
                .build()
                .getService();
    }

    private void upload(final String text) throws UnsupportedEncodingException {
        String bucketName = Credentials.bucketNamePrefix + System.currentTimeMillis();
        Bucket bucket = cloudStorage.create(BucketInfo.newBuilder(bucketName)
                .setLocation("europe-north1")
                .build());

        BlobId blobId = BlobId.of(bucketName, Credentials.blobName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
        Blob blob = bucket.create(String.valueOf(blobInfo), text.getBytes("UTF-8"));
    }

    public void log(final String text, boolean addNewLine) {
        logPool.execute(() -> logInternal(text, addNewLine));
    }

    private void logInternal(final String text, boolean addNewLine) {
        String fullText = text;

        if (addNewLine) {
            fullText += '\n';
        }

        try {
            upload(fullText);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}