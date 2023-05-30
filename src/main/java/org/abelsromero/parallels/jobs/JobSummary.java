package org.abelsromero.parallels.jobs;

/**
 * Contains the information related to the execution of a single job.
 */
record JobSummary(boolean successful, long executionTime) {

    public static JobSummary success(final long executionTime) {
        return new JobSummary(true, executionTime);
    }

    public static JobSummary failure(final long executionTime) {
        return new JobSummary(false, executionTime);
    }
}
