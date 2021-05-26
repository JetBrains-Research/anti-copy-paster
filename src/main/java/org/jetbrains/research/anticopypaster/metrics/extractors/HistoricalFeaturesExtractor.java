package org.jetbrains.research.anticopypaster.metrics.extractors;

import com.intellij.openapi.diagnostic.Logger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.research.anticopypaster.models.features.feature.Feature;
import org.jetbrains.research.anticopypaster.models.features.feature.FeatureItem;
import org.jetbrains.research.anticopypaster.models.features.features_vector.IFeaturesVector;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Extracts the historical features for the method using git blame command:
 * <li>Number of commits that make up the method;</li>
 * <li>Number of authors that edited the method;</li>
 * <li>Time from the writing of the oldest line of the method;</li>
 * <li>Average age of a line in the fragment.</li>
 */
public class HistoricalFeaturesExtractor {

    private static final Logger LOG = Logger.getInstance(HistoricalFeaturesExtractor.class);

    /**
     * Runs git blame command to retrieve the method's history.
     *
     * @param repoPath       path to the repository;
     * @param firstLine      the line where methods starts;
     * @param lastLine       the line where the method ends;
     * @param filePath       path to the file containing the method;
     * @param featuresVector feature vector;
     */
    public static void calculateHistoricalFeatures(String repoPath,
                                                   int firstLine,
                                                   int lastLine,
                                                   String filePath,
                                                   IFeaturesVector featuresVector) throws GitAPIException {
        Repository repository;
        try {
            repository = openRepository(repoPath);
        } catch (Exception e) {
            LOG.error("[ACP] Failed to open the project repository.");
            return;
        }

        final BlameResult result = new Git(repository).blame().setFilePath(filePath.substring(filePath.indexOf("src")))
            .setTextComparator(RawTextComparator.WS_IGNORE_ALL).call();

        ArrayList<Integer> creationDates = new ArrayList<>();
        Set<String> commits = new HashSet<>();
        Set<String> authors = new HashSet<>();
        final RawText rawText = result.getResultContents();
        for (int i = firstLine; i < Math.min(rawText.size(), lastLine + 1); i++) {
            final PersonIdent sourceAuthor = result.getSourceAuthor(i);
            final RevCommit sourceCommit = result.getSourceCommit(i);
            if (sourceCommit != null) {
                creationDates.add(sourceCommit.getCommitTime());
                commits.add(sourceCommit.getName());
                authors.add(sourceAuthor.getName());
            }
        }

        featuresVector.addFeature(
            new FeatureItem(Feature.TotalCommitsInFragment, commits.size()));
        featuresVector.addFeature(
            new FeatureItem(Feature.TotalAuthorsInFragment, authors.size()));

        int minTime = Integer.MAX_VALUE;
        int maxTime = Integer.MIN_VALUE;

        for (Integer time : creationDates) {
            if (minTime > time) {
                minTime = time;
            }
            if (maxTime < time) {
                maxTime = time;
            }
        }

        if (minTime != Integer.MAX_VALUE) {
            int totalTime = 0;
            for (Integer time : creationDates) {
                totalTime += time - minTime;
            }

            featuresVector.addFeature(new FeatureItem(Feature.LiveTimeOfFragment, maxTime - minTime));
            featuresVector.addFeature(
                new FeatureItem(Feature.AverageLiveTimeOfLine, (double) totalTime / creationDates.size()));
        }
    }

    private static Repository openRepository(String repositoryPath) throws Exception {
        File folder = new File(repositoryPath);
        Repository repository;
        if (folder.exists()) {
            RepositoryBuilder builder = new RepositoryBuilder();
            repository = builder
                .setGitDir(new File(folder, ".git"))
                .readEnvironment()
                .findGitDir()
                .build();
        } else {
            throw new FileNotFoundException(repositoryPath);
        }
        return repository;
    }
}
