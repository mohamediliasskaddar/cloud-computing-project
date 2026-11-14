package ma.uae.model;

import ma.uae.config.UAECloudConfig;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;

import java.util.ArrayList;
import java.util.List;

public final class CloudletFactory {
    public enum WorkloadProfile {
        NORMAL(70, 20, 10),
        PEAK(50, 30, 20);

        private final int lightPercent;
        private final int mediumPercent;
        private final int heavyPercent;

        WorkloadProfile(int lightPercent, int mediumPercent, int heavyPercent) {
            this.lightPercent = lightPercent;
            this.mediumPercent = mediumPercent;
            this.heavyPercent = heavyPercent;
        }
    }

    public record SiteWorkloads(List<Cloudlet> west, List<Cloudlet> east) {}

    private record RequestType(long lengthMi, long inputKb, long outputKb) {}

    private static final RequestType LIGHT = new RequestType(5_000, 50, 150);
    private static final RequestType MEDIUM = new RequestType(20_000, 300, 300);
    private static final RequestType HEAVY = new RequestType(50_000, 1_000, 2_000);

    private CloudletFactory() {}

    public static SiteWorkloads createSiteWorkloads(int totalCount, WorkloadProfile profile) {
        int lightCount = (int) Math.round(totalCount * profile.lightPercent / 100.0);
        int mediumCount = (int) Math.round(totalCount * profile.mediumPercent / 100.0);
        int heavyCount = totalCount - lightCount - mediumCount;

        int[] westTypeCounts = allocateWestCounts(lightCount, mediumCount, heavyCount);
        List<Cloudlet> west = new ArrayList<>(
                (int) Math.round(totalCount * UAECloudConfig.WEST_LOAD_SHARE));
        List<Cloudlet> east = new ArrayList<>(totalCount - west.size());

        addRequests(west, westTypeCounts[0], LIGHT);
        addRequests(east, lightCount - westTypeCounts[0], LIGHT);
        addRequests(west, westTypeCounts[1], MEDIUM);
        addRequests(east, mediumCount - westTypeCounts[1], MEDIUM);
        addRequests(west, westTypeCounts[2], HEAVY);
        addRequests(east, heavyCount - westTypeCounts[2], HEAVY);

        return new SiteWorkloads(west, east);
    }

    public static List<List<Cloudlet>> splitIntoBatches(List<Cloudlet> cloudlets, int batchCount) {
        int actualBatchCount = Math.min(batchCount, cloudlets.size());
        List<List<Cloudlet>> batches = new ArrayList<>(actualBatchCount);
        for (int i = 0; i < actualBatchCount; i++) {
            batches.add(new ArrayList<>());
        }
        for (int i = 0; i < cloudlets.size(); i++) {
            batches.get(i % actualBatchCount).add(cloudlets.get(i));
        }
        return batches;
    }

    private static int[] allocateWestCounts(int light, int medium, int heavy) {
        int total = light + medium + heavy;
        int targetWest = (int) Math.round(total * UAECloudConfig.WEST_LOAD_SHARE);
        int[] counts = {
                (int) Math.floor(light * UAECloudConfig.WEST_LOAD_SHARE),
                (int) Math.floor(medium * UAECloudConfig.WEST_LOAD_SHARE),
                (int) Math.floor(heavy * UAECloudConfig.WEST_LOAD_SHARE)
        };
        double[] fractions = {
                light * UAECloudConfig.WEST_LOAD_SHARE - counts[0],
                medium * UAECloudConfig.WEST_LOAD_SHARE - counts[1],
                heavy * UAECloudConfig.WEST_LOAD_SHARE - counts[2]
        };
        int remaining = targetWest - counts[0] - counts[1] - counts[2];
        while (remaining-- > 0) {
            int largestRemainder = 0;
            for (int i = 1; i < fractions.length; i++) {
                if (fractions[i] > fractions[largestRemainder]) {
                    largestRemainder = i;
                }
            }
            counts[largestRemainder]++;
            fractions[largestRemainder] = -1;
        }
        return counts;
    }

    private static void addRequests(List<Cloudlet> target, int count, RequestType type) {
        for (int i = 0; i < count; i++) {
            target.add(new CloudletSimple(type.lengthMi, 1)
                    .setFileSize(type.inputKb * 1_024)
                    .setOutputSize(type.outputKb * 1_024));
        }
    }
}
