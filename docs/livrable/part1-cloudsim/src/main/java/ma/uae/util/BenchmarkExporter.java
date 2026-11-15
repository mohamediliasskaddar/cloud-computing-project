package ma.uae.util;

import ma.uae.config.UAECloudConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Locale;

public final class BenchmarkExporter {
    private static final String CSV_FILE = "benchmark_results.csv";
    private static final String HEADER = String.join(",",
            "RunTimestampUtc",
            "Scenario",
            "Period",
            "CloudletScheduler",
            "VmAllocationPolicy",
            "Autoscaling",
            "AutoscalingCpuThresholdPercent",
            "SubmittedCloudlets",
            "FinishedCloudlets",
            "LightCloudlets",
            "MediumCloudlets",
            "HeavyCloudlets",
            "WestCloudlets",
            "EastCloudlets",
            "InitialVms",
            "CreatedVms",
            "AverageCpuTimeSeconds",
            "AverageQueueWaitSeconds",
            "AverageResponseTimeSeconds",
            "MakespanSeconds",
            "AverageHostCpuUtilizationPercent",
            "EstimatedCostUsd");

    private BenchmarkExporter() {}

    public static void exportToCsv(List<BenchmarkResult> results) throws IOException {
        Path output = Path.of(CSV_FILE);
        try (var writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            writer.newLine();
            String timestamp = Instant.now().toString();
            for (BenchmarkResult result : results) {
                writer.write(String.format(Locale.ROOT,
                        "%s,%s,%s,%s,%s,%s,%.0f,%d,%d,%d,%d,%d,%d,%d,%d,%d,%.4f,%.4f,%.4f,%.4f,%.2f,%.8f",
                        timestamp,
                        result.scenario(),
                        result.period(),
                        result.scheduler(),
                        "VmAllocationPolicyBestFit",
                        result.autoscaling(),
                        result.autoscaling()
                                ? UAECloudConfig.AUTOSCALING_CPU_THRESHOLD * 100
                                : 0,
                        result.submittedCloudlets(),
                        result.finishedCloudlets(),
                        result.lightCloudlets(),
                        result.mediumCloudlets(),
                        result.heavyCloudlets(),
                        result.westCloudlets(),
                        result.eastCloudlets(),
                        result.initialVms(),
                        result.createdVms(),
                        result.averageCpuTimeSeconds(),
                        result.averageQueueWaitSeconds(),
                        result.averageResponseTimeSeconds(),
                        result.makespanSeconds(),
                        result.averageHostCpuUtilizationPercent(),
                        result.estimatedCostUsd()));
                writer.newLine();
            }
        }
        System.out.printf("Résultats CSV écrits dans %s (%d scénarios).%n",
                output.toAbsolutePath(), results.size());
    }
}
