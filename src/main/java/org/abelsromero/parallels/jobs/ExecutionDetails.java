package org.abelsromero.parallels.jobs;

import lombok.Builder;

@Builder
public class ExecutionDetails {

    private final JobsDetails successfulJobs;
    private final JobsDetails failedJobs;

    // milliseconds
    private final long time;
    private final Double jobsPerSecond;

    public int getTotalJobsCount() {
        return successfulJobs.count() + failedJobs.count();
    }

    public JobsDetails getSuccessfulJobs() {
        return successfulJobs;
    }

    public JobsDetails getFailedJobs() {
        return failedJobs;
    }

    public long getTime() {
        return time;
    }

    public Double getJobsPerSecond() {
        return jobsPerSecond;
    }
}
