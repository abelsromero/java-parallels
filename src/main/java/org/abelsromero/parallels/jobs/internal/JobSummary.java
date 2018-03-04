package org.abelsromero.parallels.jobs.internal;

import lombok.Getter;

/**
 * Contains the information related to the execution of a single job.
 */
@Getter
public class JobSummary {

    private final boolean successful;
    private final long executionTime;

    private JobSummary(final boolean successful, final long executionTime) {
        this.successful = successful;
        this.executionTime = executionTime;
    }

    public static JobSummary success(final long executionTime) {
        return new JobSummary(true, executionTime);
    }

    public static JobSummary failure(final long executionTime) {
        return new JobSummary(false, executionTime);
    }

}
