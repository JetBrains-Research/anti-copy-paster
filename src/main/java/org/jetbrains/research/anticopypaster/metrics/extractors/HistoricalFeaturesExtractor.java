package org.jetbrains.research.anticopypaster.metrics.extractors;

//import com.felixgrund.codeshovel.execution.ShovelExecution;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;

import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Extracts the historical features from the method:
 * <li>Number of commits that make up the method;</li>
 * <li>Number of authors that edited the method;</li>
 * <li>Time from the writing of the oldest line of the method.</li>
 */
public class HistoricalFeaturesExtractor {
    private static final Logger LOG = Logger.getInstance(HistoricalFeaturesExtractor.class);

    /**
     * Runs CodeShovel to retrieve the commits the method was modified in.
     *
     * @param repoPath   path to the repository;
     * @param filePath   path to the file;
     * @param methodName name of the method;
     * @param lineCount  line the method starts in;
     * @return method history.
     */
    public static MethodHistory run(String repoPath, String filePath, String methodName, int lineCount) {
        ArrayList<String> commitsInfo = new ArrayList<>();

        try {
            //commitsInfo = ShovelExecution.getCommitHistoryForMethod(repoPath, filePath, methodName, lineCount);
        } catch (Exception e) {
            LOG.error("[ACP] Failed to retrieve the historical features.", e.getMessage());
        }

        HashSet<String> authorNames = new HashSet<>();
        ArrayList<String> commitDates = new ArrayList<>();

        //Extract all authors that modified the method and all dates the method was modified in.
        for (String commitEntry : commitsInfo) {
            String[] parts = commitEntry.split(" : ");
            JsonElement commitDetails = new JsonParser().parse(parts[1]);
            JsonObject jsonObject = commitDetails.getAsJsonObject();
            authorNames.add(jsonObject.get("commitAuthor").getAsString());
            commitDates.add(jsonObject.get("commitDate").getAsString());
        }

        return new MethodHistory(commitsInfo.size(),
                                 authorNames.size(),
                                 calculateMethodAgeInDays(commitDates)
        );
    }

    /**
     * Calculates the count of days passed from the day the method was introduced.
     *
     * @param dates the dates the method was modified in;
     * @return the age of the method in days.
     */
    private static long calculateMethodAgeInDays(ArrayList<String> dates) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yy, h:mm a");
        dates.sort(Comparator.comparing(s -> LocalDateTime.parse(s, formatter)));

        LocalDate dayTheMethodWasIntroduced = LocalDate.parse(dates.get(0), formatter);
        LocalDate currentDate = LocalDate.parse(LocalDateTime.now().format(formatter), formatter);
        return DAYS.between(dayTheMethodWasIntroduced, currentDate);
    }

}
