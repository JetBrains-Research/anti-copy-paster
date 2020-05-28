import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.options.newEditor.SettingsDialog;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;

/**
 * Action of printing AST tree of selected text
 */
public class UploadLogAction extends AnAction {
    private static String SERVICE_ACCOUNT_JSON_PATH = System.getProperty("user.home") + "/Anti-Copy-Paster-1c5681e84926.json";
    private static String LOCAL_LOG_FILE_PATH = System.getProperty("user.home") + "/copy_log.txt";

    public UploadLogAction() {
        super();
    }

    public void actionPerformed(@NotNull AnActionEvent e) {
        //final Editor editor = e.getData(CommonDataKeys.PROJECT);

        int code = Messages.showOkCancelDialog("Send log file to server?", "Upload Logs", Messages.getInformationIcon());

        if (code == 0) {
            uploadLog();
        }

    }

    private void uploadLog() {
        try {

            Storage cloudStorage = StorageOptions
                    .newBuilder()
                    .setCredentials(
                            ServiceAccountCredentials
                                    .fromStream(new FileInputStream(SERVICE_ACCOUNT_JSON_PATH))
                    )
                    .build()
                    .getService();

            long curTime = System.currentTimeMillis();
            String bucketName = "acp-bucket-" + curTime;
            Bucket bucket = cloudStorage.create(BucketInfo.of(bucketName));

            String blobName = "-copy-log.txt";
            BlobId blobId = BlobId.of(bucketName, blobName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("text/plain").build();

            String logText = "";

            try (InputStream is = new FileInputStream(LOCAL_LOG_FILE_PATH);
                 BufferedReader buf = new BufferedReader(new InputStreamReader(is))) {
                String line = buf.readLine();
                StringBuilder sb = new StringBuilder();
                while(line != null){
                    sb.append(line).append("\n");
                    line = buf.readLine();
                }

                logText = sb.toString();

            } catch (IOException ex) {
                Messages.showErrorDialog(ex.getMessage(), "Upload Error");
            }


            if (!logText.isEmpty()) {
                Blob blob = bucket.create(String.valueOf(blobInfo), logText.getBytes("UTF-8"));
            }
        } catch (Exception e) {
            Messages.showErrorDialog(e.getMessage(), "Upload Error");
        }

    }
}