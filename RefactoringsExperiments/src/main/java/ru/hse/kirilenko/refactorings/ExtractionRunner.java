package ru.hse.kirilenko.refactorings;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ExtractionRunner {
    private List<String> repos = new ArrayList<>();
    private int current = 0;
    private int total = 0;
    public ExtractionRunner(List<String> repos) {
        this.repos = repos;
    }

    public void run(ProgressBar allBar, ProgressBar bar, Label all, Label repoL, Label totalRefactoringsCount) {
        total = 0;
        current = 0;
        total = Math.max(repos.size(), 1);

        for (String repo: repos) {
            current++;
            Platform.runLater(() -> {
                allBar.setProgress((double)current / total);
                all.setText(current + "/" + total);
            });
            String url = "https://github.com/" + repo + ".git";
            String pathToResult = repo;
            if (ExtractionConfig.noSubfolders) {
                String[] parts = repo.split(" ");
                if (parts.length >= 2) {
                    pathToResult = parts[1];
                }
            }
            String outputFileName = "results/" + pathToResult + "_results.txt";
            tryCreateFile(outputFileName);
            try(FileWriter fileWriter = new FileWriter(outputFileName)) {
                System.out.println("Run repo with URL: " + url);
                final PrintWriter printWriter = new PrintWriter(fileWriter);
                RefactoringsExtractor extractor = new RefactoringsExtractor(printWriter, url, repo);
                extractor.run(bar, repoL, totalRefactoringsCount);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        //CSVBuilder.shared.finish(true);
    }

    private void tryCreateFile(String name) {
        File file = new File(name);
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
