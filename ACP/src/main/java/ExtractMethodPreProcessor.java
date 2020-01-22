import builders.LogEventBuilder;
import builders.LogItem;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.*;
import com.intellij.codeInsight.editorActions.CopyPastePreProcessor;
import com.intellij.lang.ASTNode;
import com.intellij.lang.FileASTNode;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RawText;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import credentials.Credentials;
import loggers.CloudLogger;
import loggers.LocalLogger;
import metrics.extractors.ConnectivityExtractor;
import metrics.extractors.KeywordMetricsExtractor;
import metrics.models.MemberSets;
import models.IPredictionModel;
import models.features.feature.Feature;
import models.features.feature.FeatureItem;
import models.features.features_vector.FeaturesVector;
import models.features.features_vector.IFeaturesVector;
import models.online.RFModelOnline;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import utils.ZipUtil;
import java.io.*;
import java.lang.annotation.ElementType;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
//import com.intellij.psi.JavaRecursiveElementVisitor;
/**
 * Class that extend logic on copy-paste actions
 */
public class ExtractMethodPreProcessor implements CopyPastePreProcessor {
    private static IPredictionModel model;

    static {
        try {
            model = new RFModelOnline("anti-copy-paster", "RFBaseline1", "RFBase1");
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public String preprocessOnCopy(PsiFile file, int[] startOffsets, int[] endOffsets, String text) {
        return null;
    }

    @NotNull
    @Override
    public String preprocessOnPaste(Project project, PsiFile file, Editor editor, String text, RawText rawText) {

        if (project == null || editor == null || file == null) {
            return text;
        }

        IFeaturesVector featuresVector = new FeaturesVector(112);

        int linesCount = StringUtils.countMatches(text, "\n") + 1;

        KeywordMetricsExtractor.calculate(text, linesCount, featuresVector);

        FileASTNode root =  file.getNode();
        ConnectivityExtractor.calculate(root, text, linesCount, "", featuresVector);

        featuresVector.addFeature(new FeatureItem(Feature.TotalSymbolsInCodeFragment, text.length()));
        featuresVector.addFeature(new FeatureItem(Feature.AverageSymbolsInCodeLine, (double)text.length() / linesCount));


        //TODO: recalc
        featuresVector.addFeature(new FeatureItem(Feature.TotalCommitsInFragment, 1));
        featuresVector.addFeature(new FeatureItem(Feature.TotalAuthorsInFragment, 1));
        //
        featuresVector.addFeature(new FeatureItem(Feature.LiveTimeOfFragment, 1e6));
        featuresVector.addFeature(new FeatureItem(Feature.AverageLiveTimeOfLine, 1e6));
        //

        featuresVector.addFeature(new FeatureItem(Feature.TotalLinesOfCode, linesCount));

        //TODO: method decl metrics

        try {
            List<Integer> prediction = model.predict(Collections.singletonList(featuresVector));
            //BigDecimal bd = BigDecimal.valueOf(prediction.get(0));
            if (prediction.get(0) == 1) {
                Messages.showMessageDialog(editor.getProject(), "Code is recommended to extract", "Anti CP Recommendation", Messages.getWarningIcon());
            } else {
                //nothing to do
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return text;
    }


}