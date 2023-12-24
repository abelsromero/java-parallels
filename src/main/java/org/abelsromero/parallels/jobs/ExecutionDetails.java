package org.abelsromero.parallels.jobs;

import lombok.Builder;

@Builder
public class ExecutionDetails {

    private final TasksDetails successfulTasks;
    private final TasksDetails failedTasks;

    // milliseconds
    private final long time;
    private final Double tasksPerSecond;

    public int getTotalTasksCount() {
        return successfulTasks.count() + failedTasks.count();
    }

    public TasksDetails getSuccessfulTasks() {
        return successfulTasks;
    }

    public TasksDetails getFailedTasks() {
        return failedTasks;
    }

    public long getTime() {
        return time;
    }

    public Double getTasksPerSecond() {
        return tasksPerSecond;
    }
}
