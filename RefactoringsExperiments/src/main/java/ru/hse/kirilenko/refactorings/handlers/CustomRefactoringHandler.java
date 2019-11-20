package ru.hse.kirilenko.refactorings.handlers;

import gr.uom.java.xmi.LocationInfo;
import gr.uom.java.xmi.decomposition.AbstractCodeFragment;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.api.RefactoringType;
import org.refactoringminer.util.GitServiceImpl;
import ru.hse.kirilenko.refactorings.ExtractionConfig;
import ru.hse.kirilenko.refactorings.collectors.CSVBuilder;
import ru.hse.kirilenko.refactorings.collectors.LocCollector;
import ru.hse.kirilenko.refactorings.utils.GitBlameAnalyzer;

import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import static ru.hse.kirilenko.refactorings.utils.OutputUtils.printCompositeStatement;
import static ru.hse.kirilenko.refactorings.utils.OutputUtils.printLn;

public class CustomRefactoringHandler extends RefactoringHandler {
    private final PrintWriter out;
    private final String repoURL;
    private final String repoName;
    private final MetadataExtractor metadataExtractor;
    private int current = 0;
    private int total;
    private ProgressBar bar;
    private Label repoL;
    private Label totalRefactoringsCount;

    public CustomRefactoringHandler(final PrintWriter out,
                                    final String repoURL,
                                    final String repoName,
                                    final MetadataExtractor metadataExtractor,
                                    int total,
                                    final ProgressBar bar,
                                    final Label repoL,
                                    final Label totalRefactoringsCount) {
        this.out = out;
        this.repoURL = repoURL;
        this.repoName = repoName;
        this.metadataExtractor = metadataExtractor;
        this.total = total;
        this.totalRefactoringsCount = totalRefactoringsCount;
        this.bar = bar;
        this.repoL = repoL;
    }

    @Override
    public boolean skipCommit(String commitId) {
        return false;
    }

    public void handle(String commitId, List<Refactoring> refactorings) {
        current++;
        Platform.runLater(() -> {
            bar.setProgress((double)current / total);
            repoL.setText(current + "/" + total);
        });
        handleCommit(commitId, refactorings, out, totalRefactoringsCount);
    }

    public void handleException(String commitId, Exception e) {
        System.err.println("Cannot handle commit with ID: " + commitId);
        //e.printStackTrace();
    }

    public void onFinish(int refactoringsCount, int commitsCount, int errorCommitsCount) {
        printLn(String.valueOf(errorCommitsCount), out);
    }

    private void handleCommit(String commitId, List<Refactoring> refactorings, PrintWriter pw, Label l) {
        boolean hasExtractMethod = false;
        for (Refactoring ref : refactorings) {
            if (ref.getRefactoringType() == RefactoringType.EXTRACT_OPERATION) {
                hasExtractMethod = true;
                Platform.runLater(() -> {
                    int curCount = Integer.parseInt(l.getText()) + 1;
                    l.setText(Integer.toString(curCount));
                });
            }
        }

        String commonURL = repoURL.substring(0, repoURL.length() - 4) + "/commit/" + commitId;
        String blobURL = repoURL.substring(0, repoURL.length() - 4) + "/blob/" + commitId;

        if (hasExtractMethod) {
            System.err.println("COMMIT ID: " + commitId);
            printLn("COMMIT ID: " + commitId, pw);
            printLn("URL: " + commonURL, pw);
        }

        boolean hasEMRefactorings = false;

        for (Refactoring ref : refactorings) {

            if (ref.getRefactoringType() == RefactoringType.EXTRACT_OPERATION) {
                if (!hasEMRefactorings) {
                    printLn("-----REFACTORINGS_BEGIN-----", pw);
                    hasEMRefactorings = true;
                }
                printLn("---REFACTORING_START---", pw);
                CSVBuilder.shared.completeLine(true);
                //CSVBuilder.shared.addStr(commitId);
                printLn("DESCRIPTION: " + ref.toString(), pw);
                ExtractOperationRefactoring refactoring = (ExtractOperationRefactoring)ref;
                LocationInfo locInfo = refactoring.getExtractedOperation().getLocationInfo();
                //CSVBuilder.shared.addStr(locInfo.getFilePath());
                //CSVBuilder.shared.addStr(locInfo.getStartLine());
                //CSVBuilder.shared.addStr(locInfo.getStartColumn());
                //CSVBuilder.shared.addStr(locInfo.getEndLine());
                //CSVBuilder.shared.addStr(locInfo.getEndColumn());
                printLn("REFACTORING FILE DIFF URL: " + commonURL + "/" + locInfo.getFilePath(), pw);
                printLn("REFACTORING URL: " + blobURL + "/" + locInfo.getFilePath() + "#L" + locInfo.getStartLine(), pw);
                handleRefactoring(commitId, refactoring, pw);
            }

        }
        if (hasEMRefactorings) {
            printLn("-----REFACTORINGS_END-----", pw);
        }
    }

    private void handleRefactoring(String commitId, ExtractOperationRefactoring refactoring, PrintWriter pw) {
        LocationInfo locInfo = refactoring.getExtractedOperation().getLocationInfo();
        LocCollector.accept(locInfo.getEndLine() - locInfo.getStartLine() + 1);
        String extractedOperationLocation = locInfo.getFilePath();
        if (ExtractionConfig.extractDirectly) {
            printLn("DIRECTLY EXTRACTED OPERATION:", pw);
            try {
                metadataExtractor.extractFragment(commitId,
                        extractedOperationLocation,
                        locInfo.getStartLine(),
                        locInfo.getEndLine(),
                        locInfo.getStartColumn(),
                        locInfo.getEndColumn(),
                        false);

                metadataExtractor.extractLineAuthorAndCreationDate(locInfo.getStartLine(),
                        locInfo.getEndLine(),
                        extractedOperationLocation);
                printLn("NUMBER OF LINES IN FRAGMENT: " + (locInfo.getEndLine() - locInfo.getStartLine() + 1), pw);
                CSVBuilder.shared.addStr(locInfo.getEndLine() - locInfo.getStartLine() + 1);
            } catch (Exception e) {
                System.err.println("Cannot extract refactoring in commit: " + commitId);
                e.printStackTrace();
            }
        }

        if (!ExtractionConfig.onlyExtractedOperation) {
            printLn("SOURCE BEFORE EXTRACTION:", pw);
            printCompositeStatement(refactoring.getSourceOperationBeforeExtraction().getBody().getCompositeStatement(),  0, pw);

            printLn("SOURCE AFTER EXTRACTION:", pw);
            printCompositeStatement(refactoring.getSourceOperationAfterExtraction().getBody().getCompositeStatement(),  0, pw);
        }

        if (!ExtractionConfig.extractDirectly) {
            printLn("EXTRACTED OPERATION:", pw);
            printCompositeStatement(refactoring.getExtractedOperation().getBody().getCompositeStatement(), 0, pw);
        }

        printLn("---REFACTORING_FINISH---", pw);
        //CSVBuilder.shared.completeLine();
    }
}
