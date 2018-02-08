package org.abelsromero.parallels.jobs;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ExecutionDetails {

    /**
     * In milliseconds
     */
    private long time;
    private long successfulOperations;
    private long failedOperations;

}
