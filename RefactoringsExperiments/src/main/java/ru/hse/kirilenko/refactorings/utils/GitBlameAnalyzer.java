package ru.hse.kirilenko.refactorings.utils;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.diff.RawText;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import ru.hse.kirilenko.refactorings.collectors.CSVBuilder;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static ru.hse.kirilenko.refactorings.utils.OutputUtils.printLn;

public class GitBlameAnalyzer {

    public static void extractLineAuthorAndCreationDate(Repository repo,
                                                 int firstLine,
                                                 int lastLine,
                                                 String filePath) throws GitAPIException {
        final BlameResult result = new Git(repo).blame().setFilePath(filePath)
                .setTextComparator(RawTextComparator.WS_IGNORE_ALL).call();

        if (result == null) {
            CSVBuilder.shared.addStr(0);
            CSVBuilder.shared.addStr(0);
            CSVBuilder.shared.addStr(0);
            CSVBuilder.shared.addStr(0);
            return;
        }

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

        CSVBuilder.shared.addStr(commits.size());
        CSVBuilder.shared.addStr(authors.size());

        int minTime = Integer.MAX_VALUE;
        int maxTime = Integer.MIN_VALUE;

        for (Integer time: creationDates) {
            if (minTime > time) {
                minTime = time;
            }
            if (maxTime < time) {
                maxTime = time;
            }
        }

        if (minTime != Integer.MAX_VALUE) {
            CSVBuilder.shared.addStr((maxTime - minTime));
            int totalTime = 0;
            for (Integer time: creationDates) {
                totalTime += time - minTime;
            }

            CSVBuilder.shared.addStr((double)totalTime / creationDates.size());
        } else {
            CSVBuilder.shared.addStr(0);
            CSVBuilder.shared.addStr(0);
        }
    }
}
