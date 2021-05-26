package org.jetbrains.research.anticopypaster.metrics.extractors;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.jetbrains.research.anticopypaster.models.features.feature.Feature;
import org.jetbrains.research.anticopypaster.models.features.feature.FeatureItem;
import org.jetbrains.research.anticopypaster.models.features.features_vector.IFeaturesVector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GitBlameAnalyzer {

    public static void calculateHistoricalFeatures(Repository repo,
                                                   int firstLine,
                                                   int lastLine,
                                                   String filePath,
                                                   IFeaturesVector featuresVector) throws GitAPIException {
        final BlameResult result = new Git(repo).blame().setFilePath(filePath.substring(filePath.indexOf("src")))
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
}
