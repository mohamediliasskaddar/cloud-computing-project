package ma.uae.util;

public record BenchmarkResult(
        String scenario,
        String period,
        String scheduler,
        boolean autoscaling,
        int submittedCloudlets,
        int finishedCloudlets,
        int lightCloudlets,
        int mediumCloudlets,
        int heavyCloudlets,
        int westCloudlets,
        int eastCloudlets,
        int initialVms,
        int createdVms,
        double averageCpuTimeSeconds,
        double averageQueueWaitSeconds,
        double averageResponseTimeSeconds,
        double makespanSeconds,
        double averageHostCpuUtilizationPercent,
        double estimatedCostUsd) {}
