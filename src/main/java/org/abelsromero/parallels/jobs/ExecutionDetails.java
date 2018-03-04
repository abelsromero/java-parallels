package org.abelsromero.parallels.jobs;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ExecutionDetails {

    private JobsDetails successfulJobs;
    private JobsDetails failedJobs;

    // milliseconds
    private long time;
    private Double jobsPerSecond;

    public int getTotalJobsCount() {
        return successfulJobs.getCount() + failedJobs.getCount();
    }

}
