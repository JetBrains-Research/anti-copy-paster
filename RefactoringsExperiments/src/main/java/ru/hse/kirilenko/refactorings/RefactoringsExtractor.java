package ru.hse.kirilenko.refactorings;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.*;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;
import ru.hse.kirilenko.refactorings.handlers.CustomRefactoringHandler;
import ru.hse.kirilenko.refactorings.handlers.MetadataExtractor;

import java.io.PrintWriter;

public class RefactoringsExtractor {
    private PrintWriter out;
    private String repoURL;
    private String repoName;
    private int total = 1;
    private int current = 0;
    public RefactoringsExtractor(PrintWriter out, String repoURL, String repoName) {
        this.out = out;
        this.repoURL = repoURL;
        this.repoName = repoName;
    }

    public void run(ProgressBar bar, Label repoL, Label totalRefactoringsCount) throws Exception {
        GitService gitService = new GitServiceImpl();
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

        Repository repo = gitService.cloneIfNotExists(repoName, repoURL);
        MetadataExtractor me = new MetadataExtractor(repo, out);

        try (Git git = new Git(repo)) {
            Iterable<RevCommit> commits = git.log().all().call();
            int count = 0;
            for (RevCommit commit : commits) {
                count++;
            }
            System.out.println(count);
            this.total = count;
        }

        Platform.runLater(() -> {
            bar.setProgress((double)current / total);
            repoL.setText(current + "/" + total);
        });

        miner.detectAll(repo, "master", new CustomRefactoringHandler(out, repoURL, repoName, me, total, bar, repoL, totalRefactoringsCount));
        out.close();
    }
}
