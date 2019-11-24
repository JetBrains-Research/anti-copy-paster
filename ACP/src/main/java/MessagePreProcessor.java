import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.storage.*;
import com.intellij.codeInsight.editorActions.CopyPastePreProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RawText;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import java.io.*;

import com.google.cloud.ServiceOptions;
/**
 * Class that extend logic on copy-paste actions
 */
public class MessagePreProcessor implements CopyPastePreProcessor {
    private static Logger logger;
    private static CloudLogger cloudLogger;
    private static Storage cloudStorage;
    private static String SERVICE_ACCOUNT_JSON_PATH = System.getProperty("user.home") + "/Anti-Copy-Paster-1c5681e84926.json";
    private static String LOCAL_LOG_FILE_PATH = System.getProperty("user.home") + "/copy_log.txt";

    static {
        try {
            logger = new Logger(LOCAL_LOG_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Unable to create log file");
            e.printStackTrace();
        }

        try {

            cloudStorage = StorageOptions
                    .newBuilder()
                    .setCredentials(
                            ServiceAccountCredentials
                            .fromStream(new FileInputStream(SERVICE_ACCOUNT_JSON_PATH))
                    )
                    .build()
                    .getService();
        } catch (Exception e) {
            e.printStackTrace();
        }

        cloudLogger = new CloudLogger();
    }


    /**
     * Nothing to do
     */
    @Nullable
    @Override
    public String preprocessOnCopy(PsiFile file, int[] startOffsets, int[] endOffsets, String text) {
        int locs = StringUtils.countMatches(text, '\n');
        boolean lastCharIsNewLine = text.charAt(text.length() - 1) == '\n';

        if (!lastCharIsNewLine) {
            ++locs;
        }

        logger.log("C " + locs, true);
        logger.log(text, !lastCharIsNewLine);

        String fullLog = "C " + locs + '\n' + text;
        //cloudLogger.log(fullLog, !lastCharIsNewLine);
        return null;
    }

    /**
     * Method that shows alert about copy-paste
     */
    @NotNull
    @Override
    public String preprocessOnPaste(Project project, PsiFile file, Editor editor, String text, RawText rawText) {
        int locs = StringUtils.countMatches(text, '\n');
        boolean lastCharIsNewLine = text.charAt(text.length() - 1) == '\n';

        if (!lastCharIsNewLine) {
            ++locs;
        }

        logger.log("P " + locs, true);
        logger.log(text, !lastCharIsNewLine);

        String fullLog = "P " + locs + '\n' + text;
        new Thread(() -> cloudLogger.log(fullLog, !lastCharIsNewLine)).start();

        return text;
    }

    private static class Logger {
        private PrintWriter pw;

        public Logger(String fileName) throws IOException {
            FileWriter fileWriter = new FileWriter(fileName, true);
            final PrintWriter printWriter = new PrintWriter(fileWriter, true);
            this.pw = printWriter;
        }

        private void log(final String text, boolean addNewLine) {
            if (addNewLine) {
                pw.println(text);
            } else {
                pw.print(text);
            }

            pw.flush();
        }
    }

    private static class CloudLogger {
        public CloudLogger() {}

        private static void upload(final String text) throws UnsupportedEncodingException {
            String bucketName = "acp-bucket-" + System.currentTimeMillis();
            Bucket bucket = cloudStorage.create(BucketInfo.of(bucketName));

            String blobName = "-copy-log.txt";
            BlobId blobId = BlobId.of(bucketName, blobName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();
            Blob blob = bucket.create(String.valueOf(blobInfo), text.getBytes("UTF-8"));
        }

        private void log(final String text, boolean addNewLine) {
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
}