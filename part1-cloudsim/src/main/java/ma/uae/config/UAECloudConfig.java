package ma.uae.config;

public class UAECloudConfig {
    
    // Matériel des Data Centers (Tanger & Tétouan)
    public static final int HOST_PES = 16;             // 16 cœurs CPU par hôte
    public static final int HOST_MIPS = 10000;         // Capacité par cœur (MIPS)
    public static final int HOST_RAM = 64536;          // 64 GB RAM
    public static final long HOST_BW = 10000;          // 10 Gbps
    public static final long HOST_STORAGE = 1000000;   // 1 TB

    public static final int DC1_HOSTS = 8;             // Tanger (55% de la charge)
    public static final int DC2_HOSTS = 6;             // Tétouan (45% de la charge)

    // -------------------------------------------------------------
    // Configuration des VMs (SaaS Moodle / Inscriptions)
    // -------------------------------------------------------------
    public static final int VM_PES = 4;
    public static final int VM_MIPS = 2500;
    public static final int VM_RAM = 8192;             // 8 GB RAM
    public static final long VM_BW = 1000;
    public static final long VM_SIZE = 10000;

    // -------------------------------------------------------------
    // Volumes de charge (Simultanéité)
    // -------------------------------------------------------------
    public static final int INITIAL_VMS = 10;
    public static final int NORMAL_CLOUDLETS = 50;     // Période normale (5% d'activité)
    public static final int PEAK_CLOUDLETS = 300;      // Période de pointe (30% d'activité)
}