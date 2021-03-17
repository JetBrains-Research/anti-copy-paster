package org.jetbrains.research.anticopypaster.metrics.extractors;

/**
 * Represents the history of a method obtained from commits.
 */
public class MethodHistory {
    private final int totalCommitCount;
    private final int totalAuthorCount;
    private final long ageInDays;

    public MethodHistory(int totalCommitCount,
                         int totalAuthorCount,
                         long ageInDays) {
        this.totalCommitCount = totalCommitCount;
        this.totalAuthorCount = totalAuthorCount;
        this.ageInDays = ageInDays;
    }

    public int getTotalCommitCount() {
        return totalCommitCount;
    }

    public int getTotalAuthorCount() {
        return totalAuthorCount;
    }

    public long getAgeInDays() {
        return ageInDays;
    }

}
