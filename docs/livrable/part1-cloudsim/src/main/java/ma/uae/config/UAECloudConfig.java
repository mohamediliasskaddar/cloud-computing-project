package ma.uae.config;

public final class UAECloudConfig {
    private UAECloudConfig() {}

    public static final String WEST_DATACENTER = "DC-TANGER";
    public static final String EAST_DATACENTER = "DC-TETOUAN";
    public static final double WEST_LOAD_SHARE = 0.55;

    public static final int WEST_HOSTS = 8;
    public static final int EAST_HOSTS = 6;
    public static final int HOST_PES = 16;
    public static final int HOST_MIPS_PER_PE = 10_000;
    public static final long HOST_RAM_MIB = 65_536;
    public static final long HOST_BW_MBPS = 10_000;
    public static final long HOST_STORAGE_MB = 1_000_000;

    public static final int VM_PES = 4;
    public static final int VM_MIPS_PER_PE = 2_500;
    public static final long VM_RAM_MIB = 8_192;
    public static final long VM_BW_MBPS = 1_000;
    public static final long VM_STORAGE_MB = 50_000;

    public static final int INITIAL_VMS = 10;
    public static final int MAX_VMS = 20;
    public static final double AUTOSCALING_CPU_THRESHOLD = 0.70;

    public static final int NORMAL_CLOUDLETS = 50;
    public static final int PEAK_CLOUDLETS = 300;
    public static final int ARRIVAL_BATCH_COUNT = 10;
    public static final double ARRIVAL_BATCH_INTERVAL_SECONDS = 5.0;

    public static final double COST_PER_VCPU_HOUR_USD = 0.04;
    public static final double COST_PER_GIB_RAM_HOUR_USD = 0.005;
    public static final double COST_PER_GIB_STORAGE_HOUR_USD = 0.0001;
}
