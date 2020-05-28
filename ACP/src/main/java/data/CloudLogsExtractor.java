package data;

import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import credentials.Credentials;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CloudLogsExtractor {
    private Storage cloudStorage;

    public CloudLogsExtractor() throws Exception {
        cloudStorage = StorageOptions
                .newBuilder()
                .setCredentials(ServiceAccountCredentials.fromStream(Credentials.toStream()))
                .build()
                .getService();
    }

    public void dumpBucketsCount() {
        Page<Bucket> buckets = cloudStorage.list();
        int count = 0;

        for (Bucket bucket : buckets.iterateAll()) {
            ++count;
        }

        System.out.println("Total buckets count: " + count);
    }

    public void listBucketsNames() {
        Page<Bucket> buckets = cloudStorage.list();

        for (Bucket bucket : buckets.iterateAll()) {
            System.out.println(bucket.getName());
        }
    }

    public void downloadAll() {
        Page<Bucket> buckets = cloudStorage.list();

        for (Bucket bucket : buckets.iterateAll()) {
            String bucketName = bucket.getName();
            if (!bucketName.startsWith(Credentials.bucketNamePrefix)) {
                return;
            }

            Page<Blob> blobs = bucket.list();
            for (Blob blob: blobs.iterateAll()) {
                Path destPath = Paths.get("logs/" + bucketName + ".txt");
                blob.downloadTo(destPath);
            }
        }
    }
}
