import builders.DecisionPathBuilder;
import checkers.FragmentCorrectnessChecker;
import com.intellij.codeInsight.editorActions.CopyPastePreProcessor;
import com.intellij.lang.FileASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.LanguageRefactoringSupport;
import com.intellij.lang.refactoring.RefactoringSupportProvider;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.RawText;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.actions.ExtractMethodAction;
import metrics.extractors.ConnectivityExtractor;
import metrics.extractors.KeywordMetricsExtractor;
import metrics.extractors.MethodDeclarationMetricsExtractor;
import models.IPredictionModel;
import models.features.feature.Feature;
import models.features.feature.FeatureItem;
import models.features.features_vector.FeaturesVector;
import models.features.features_vector.IFeaturesVector;
import models.offline.WekaBasedModel;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import weka.classifiers.trees.RandomTree;
import weka.core.SerializationHelper;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;
//import com.intellij.psi.JavaRecursiveElementVisitor;
/**
 * Class that extend logic on copy-paste actions
 */


public class ExtractMethodPreProcessor implements CopyPastePreProcessor {
    private static IPredictionModel model;
    private static RandomTree tree;
    private static String treeString;
    private static ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(8);
    static {
        try {
            //model = new RFModelOnline("anti-copy-paster", "RFBaseline1", "RFBase1");
            model = new WekaBasedModel();
            tree = (RandomTree) SerializationHelper.read(System.getProperty("user.home") + "/RTree-ACP-SH.model");
            treeString = tree.toString();
            String[] split = treeString.split("\n");
            StringBuilder resBuilder = new StringBuilder();
            for (int i = 4; i < split.length - 2; ++i) {
                resBuilder.append(split[i]);
                resBuilder.append("\n");
            }
            treeString = resBuilder.toString();
            treeString = treeString.substring(0, treeString.length() - 1);

        } catch (Exception e) {
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
        if (project == null || editor == null || file == null || !FragmentCorrectnessChecker.isCorrect(project, file, text)) {
            return text;
        }

        String sub = text.replaceAll(" \n\t\r","");

        final ArrayList<Future<Integer>> tasks = new ArrayList<>();
        ProjectFileIndex.SERVICE.getInstance(project).iterateContent(new ContentIterator() {
            @Override
            public boolean processFile(@NotNull VirtualFile fileOrDir) {
                PsiFile file = PsiManager.getInstance(project).findFile(fileOrDir);
                if (file == null || !file.getName().endsWith(".java")) {
                    return true;
                }

                tasks.add(pool.submit(() -> {
                    String content = file.getText().replaceAll(" \n\t\r","");

                    return StringUtils.countMatches(content, sub);
                }));

                return true;
            }
        });

        int matches = 0;
        for (Future<Integer> future: tasks) {
            try {
                matches += future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        int muchMatches = Math.max(0, matches - 3);
        double pred_boost = 0.05 * muchMatches;

        IFeaturesVector featuresVector = calculateFeatures(project, file, text);

        try {
            List<Integer> prediction = model.predict(Collections.singletonList(featuresVector));
            int pred = prediction.get(0);
            List<String> featuresToLog = featuresVector.buildVector().stream().map(Object::toString).collect(Collectors.toList());

            ACPCollector.prediction(project, featuresToLog, (float)pred);
            pred += pred_boost;

            if (pred == 1) {
                int result = Messages.showOkCancelDialog(buildMessage(featuresVector), "Anti CP Recommendation", Messages.getWarningIcon());

                if (result == 0) {
                    scheduleExtraction(project, file, editor, text);
                }

                ACPCollector.userDecisionForRecommendation(project, featuresToLog, result == 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return text;
    }

    private IFeaturesVector calculateFeatures(Project project, PsiFile file, String text) {
        IFeaturesVector featuresVector = new FeaturesVector(117);

        int linesCount = StringUtils.countMatches(text, "\n") + 1;
        KeywordMetricsExtractor.calculate(text, linesCount, featuresVector);
        ConnectivityExtractor.calculate(file, text, linesCount, "", featuresVector);
        featuresVector.addFeature(new FeatureItem(Feature.TotalSymbolsInCodeFragment, text.length()));
        featuresVector.addFeature(new FeatureItem(Feature.AverageSymbolsInCodeLine, (double)text.length() / linesCount));

        int depthTotal = MethodDeclarationMetricsExtractor.totalDepth(text);

        featuresVector.addFeature(new FeatureItem(Feature.TotalLinesDepth, depthTotal));
        featuresVector.addFeature(new FeatureItem(Feature.AverageLinesDepth, (double)depthTotal / linesCount));

        //TODO: recalc
        featuresVector.addFeature(new FeatureItem(Feature.TotalCommitsInFragment, 1));
        featuresVector.addFeature(new FeatureItem(Feature.TotalAuthorsInFragment, 1));
        //
        featuresVector.addFeature(new FeatureItem(Feature.LiveTimeOfFragment, 1e6));
        featuresVector.addFeature(new FeatureItem(Feature.AverageLiveTimeOfLine, 1e6));
        //

        featuresVector.addFeature(new FeatureItem(Feature.TotalLinesOfCode, linesCount));

        //TODO: method decl metrics

        MethodDeclarationMetricsExtractor.calculate(project, file, text, featuresVector);

        return featuresVector;
    }

    private String buildMessage(final IFeaturesVector featuresVector) {
        String baseMsg = "Code is recommended to extract";
        try {
            DecisionPathBuilder dpb = new DecisionPathBuilder(treeString);

            baseMsg += " because ";
            baseMsg += dpb.collect(dpb.buildPath(featuresVector));
            baseMsg += ".";
        } catch (Exception e) {
            //skip
        }

        return baseMsg;
    }

    private void scheduleExtraction(Project project, PsiFile file, Editor editor, String text) {
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        ApplicationManager.getApplication().invokeLater(() -> {
                            int startOffset = editor.getSelectionModel().getSelectionStart();
                            editor.getSelectionModel().setSelection(Math.max(0, startOffset - text.length()), startOffset);
                            Language language = file.getLanguage();
                            PsiElement element = file.getOriginalElement();
                            List<RefactoringSupportProvider> providers = LanguageRefactoringSupport.INSTANCE.allForLanguage(language);
                            for (RefactoringSupportProvider provider : providers) {
                                if (provider.isAvailable(element)) {
                                    RefactoringActionHandler handler = provider.getExtractMethodHandler();
                                    DataContext dc = SimpleDataContext.getProjectContext(project);
                                    if (handler != null) {
                                        handler.invoke(project, editor, file, dc);
                                    }
                                }
                            }
                        });


                    }
                },
                300
        );
    }
}




/*project.getMessageBus().connect().subscribe(VirtualFileManager.VFS_CHANGES, new BulkFileListener() {
            @Override
            public void after(@NotNull List<? extends VFileEvent> events) {
                // handle the events
                for (VFileEvent event: events) {
                    if (event instanceof VFileContentChangeEvent) {
                        System.out.println("EVENT");
                        VFileContentChangeEvent typedEvent = (VFileContentChangeEvent)event;
                        VirtualFile newF = typedEvent.getFile();
                        try {
                            byte[] ff = newF.contentsToByteArray();
                            //System.out.println(ff);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //System.out.println(typedEvent.getFile().getModificationStamp());
                    }
                }

            }
        });*/