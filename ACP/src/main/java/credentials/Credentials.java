package credentials;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public final class Credentials {
    public static final String LOCAL_LOG_FILE_PATH = System.getProperty("user.home") + "/copy_log.txt";
    private static final String GOOGLE_CLOUD_CREDENTIALS = "{\n" +
            "  \"type\": \"service_account\",\n" +
            "  \"project_id\": \"anti-copy-paster\",\n" +
            "  \"private_key_id\": \"1c5681e8492606d9363e6aa5143937241e437f16\",\n" +
            "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCQetQDLD5vqksw\\nBQYzuOhUOPHVeDHDCHBL3HM6hhTgIke3vsjxhUF3G2V6OAj5OG1kTqkMilSW/3uK\\nEVvMPU9bGPWqqnsgdEgk0Y8U1orVFjBZQOOf2j1UqeWblo27DKDmzU9BWLyWzCiJ\\nFJLZh+NzeoTz0Rsw+1zLYkbmyAzaxhUuHOmnzgjnqLRVGqjfN+gap9DeVldwTKxI\\n7hfg5Dm+6FJlvn5ffthWlsdJkEHqW2mFQZXsugDW1EbW6ou70DjV4Msp/eRit8zO\\nTG9Ewd4N+x6BLwX2I394pKa80lz3ikapfRF05yeN+vnRDIHkjuNZpCk8kSH3BJzL\\nUUFwuY0lAgMBAAECggEABxw4W1id0P6DOT+CDOEUc/KieiQyZLwBVMZqWE65b8dY\\nGrOVrYcmj99GCPQBgwxTG9zgJmkXXSABGywcBardwSizDF1b9l6qnz896KNfTTnM\\nktPl79u8aXJ8BYJtAq/OKlWfySI+E7jD5O0hhfmRSMFWki4wBaLVMAAr55cgRNLo\\nS1mEIUOhsy6YPtxIJBpfBplT9iPKEukTuIe6N80WJONzlH6sTAzsPldqadQ3BE/k\\n27TXjl8W1Tchv7z0qC4PrSxVBBtPBae42Di/IecWbGDsDFgLmv17jrVT4oQ5luGQ\\nGFKiBG9S68KsC8p5o2gfhgm/Ijhf2EDpnhZUEEUDwQKBgQDKQrOMLmOi/8nAlQKI\\nIhpyypz4p+sfDJeiMoywEUwQlagFkKxaiZOiUM3NYA+PE0spxTFfpTY/xpFhpJbZ\\n48S3xuo2LlmJoz52nJeByIcUuT+hZmcmfP2h0GfzCgryn/7xCqQOOACgH6+0MiS7\\ngHKyHi8DQJgcyedT0COdChMDZQKBgQC23gMxZk40EuhiLr0vc6xwkoydoyklAaYb\\nUqYpwh3Kbj06FaQf7bakbiYZ3EbeBGmreOOQNnAStwImJ4kkMNjGoXduHVSItUt1\\nbqN/NBnmXVY7x18VduD3OF530sICdAcAJORL4Rj7ghcjiFyPIvGDv429lzUKmmlQ\\nwoKAI6cmwQKBgQCGYSaVXkTIiRVsJBRYif/0gHUmJ7ppGJ6uHtOmLvfQlP3bJLG+\\nPWWfE/yIFOiBcJohmGgjWcrfOHhi3U10WoXabAg0Eztd3N2lRIjMq4RD2ohSHY0u\\n3yBMIADQUr3PUUm6sA0rfT29JRBBsocBJGgPu0oU6RenGOXeo4zly0mymQKBgEmi\\nwLplrdv+OSFAHOI8vBVgjcr0Zp8GOPMLpultxz06guEe0cYhnMg9qo4DS24fbMWl\\nmkz5/ssqcm0WwnyWRtRFNKhcmyuphUc2VBPm0P8YWp8bu0gTZKdoRXIOph0Cx8bh\\nFCUR8VNloRen0024DnS078iGWM96OipRCOe8c5SBAoGBAJOjfHBVYlJQiA1ZWJIs\\njKFeN0ltcX0ZS7RNhmxSEM7L/BK9dhcNpPHiilwAhs6MDDgUBOG10+iMY/4dHUxe\\nXrx4N9h/u0B1Wy011H6IVd47zGK8uVCqGw9b7x8PzyjxM/xF7OskNopSZKXoXiw8\\nBlhaMX4s4Ek5aClN2Yog0TaW\\n-----END PRIVATE KEY-----\\n\",\n" +
            "  \"client_email\": \"acp-dev@anti-copy-paster.iam.gserviceaccount.com\",\n" +
            "  \"client_id\": \"113880194234547897379\",\n" +
            "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
            "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
            "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
            "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/acp-dev%40anti-copy-paster.iam.gserviceaccount.com\"\n" +
            "}\n";

    public static final String blobName = "-copy-log.txt";
    public static final String bucketNamePrefix = "acp-bucket-";

    public static InputStream toStream() {
        return new ByteArrayInputStream(Credentials.GOOGLE_CLOUD_CREDENTIALS.getBytes());
    }
}
