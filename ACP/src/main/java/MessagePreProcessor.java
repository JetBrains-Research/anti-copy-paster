import builders.logs.LogEventBuilder;
import builders.logs.LogItem;
import com.intellij.codeInsight.editorActions.CopyPastePreProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RawText;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import credentials.Credentials;
import loggers.CloudLogger;
import loggers.LocalLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.zip.ZipUtil;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Class that extend logic on copy-paste actions
 */
public class MessagePreProcessor implements CopyPastePreProcessor {
    private static LocalLogger logger;
    private static CloudLogger cloudLogger;
    private static ThreadPoolExecutor handleQueue = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
    static {
        try {
            logger = new LocalLogger(Credentials.LOCAL_LOG_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Unable to create local log file");
            e.printStackTrace();
        }

        try {
            cloudLogger = new CloudLogger();
        } catch (Exception e) {
            System.err.println("Unable to connect google cloud");
            e.printStackTrace();
        }
    }

    /**
     * Nothing to do
     */
    @Nullable
    @Override
    public String preprocessOnCopy(PsiFile file, int[] startOffsets, int[] endOffsets, String text) {
        int startOffset = startOffsets[0];
        int endOffset = endOffsets[0];
        final VirtualFile vf = file.getVirtualFile();
        final String path;
        if (vf != null) {
            path = vf.getPath();
        } else {
            path = "";
        }
        final String fileText = file.getText();
        handleQueue.execute(() -> {
            LogEventBuilder builder = new LogEventBuilder();
            builder.addItem(LogItem.ACTION, "COPY");
            builder.addItem(LogItem.USER_NAME, System.getProperty("user.name"));
            builder.addItem(LogItem.BEGIN_OFFSET, String.valueOf(startOffset));
            builder.addItem(LogItem.END_OFFSET, String.valueOf(endOffset));
            builder.addItem(LogItem.FILE_PATH, path);

            builder.addItem(LogItem.CODE_FRAGMENT, text);

            try {
                String compressed = ZipUtil.compress(fileText);
                if (compressed.length() < fileText.length()) {
                    builder.addItem(LogItem.IS_COMPRESSED, "true");
                    builder.addItem(LogItem.FILE_CONTENT, compressed);
                } else {
                    builder.addItem(LogItem.IS_COMPRESSED, "false");
                    builder.addItem(LogItem.FILE_CONTENT, fileText);
                }
            } catch(Exception ex) {
                System.err.println("Unable to compress file with code");
                builder.addItem(LogItem.IS_COMPRESSED, "false");
                builder.addItem(LogItem.FILE_CONTENT, fileText);

            }
            handleAction(builder.build());
        });

        return null;
    }

    /**
     * Method that shows alert about copy-paste
     */
    @NotNull
    @Override
    public String preprocessOnPaste(Project project, PsiFile file, Editor editor, String text, RawText rawText) {
        final Scanner scanner;
        Scanner scanner1;
        try {
            byte[] workspace = project.getWorkspaceFile().contentsToByteArray();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(workspace);
            scanner1 = new Scanner(byteArrayInputStream);
        } catch(IOException ex) {
            scanner1 = null;
        }

        scanner = scanner1;
        int caretOffset = editor.getCaretModel().getOffset();
        final VirtualFile vf = file.getVirtualFile();
        final String path;
        if (vf != null) {
            path = vf.getPath();
        } else {
            path = "";
        }
        final String fileText = file.getText();
        handleQueue.execute(() -> {
            LogEventBuilder builder = new LogEventBuilder();
            builder.addItem(LogItem.ACTION, "PASTE");
            builder.addItem(LogItem.USER_NAME, System.getProperty("user.name"));
            if (scanner != null) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.contains("ProjectId")) {
                        builder.addItem(LogItem.PROJECT_ID, line);
                        break;
                    }
                }
            }
            builder.addItem(LogItem.BEGIN_OFFSET, String.valueOf(caretOffset));

            builder.addItem(LogItem.FILE_PATH, path);

            builder.addItem(LogItem.CODE_FRAGMENT, text);

            try {
                String compressed = ZipUtil.compress(fileText);
                if (compressed.length() < fileText.length()) {
                    builder.addItem(LogItem.IS_COMPRESSED, "true");
                    builder.addItem(LogItem.FILE_CONTENT, compressed);
                } else {
                    builder.addItem(LogItem.IS_COMPRESSED, "false");
                    builder.addItem(LogItem.FILE_CONTENT, fileText);
                }
            } catch(Exception ex) {
                System.err.println("Unable to compress file with code");
                builder.addItem(LogItem.IS_COMPRESSED, "false");
                builder.addItem(LogItem.FILE_CONTENT, fileText);

            }
            handleAction(builder.build());
        });

        return text;
    }

    private void handleAction(final String text) {
        logger.log(text, false);
        cloudLogger.log(text, false);
    }
}