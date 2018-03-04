package org.abelsromero.parallels.jobs;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class JobsDetails {

    private int count;

    private long minTime;
    private long maxTime;
    private long avgTime;

}
