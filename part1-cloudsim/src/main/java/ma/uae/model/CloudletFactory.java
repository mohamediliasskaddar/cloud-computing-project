package ma.uae.model;

import ma.uae.config.UAECloudConfig;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.cloudlets.CloudletSimple;

import java.util.ArrayList;
import java.util.List;

public class CloudletFactory {

    public static List<Cloudlet> createCloudlets(int count) {
        List<Cloudlet> list = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            long length;
            // Répartition selon le profil de requêtes
            if (i % 10 < 7) {
                length = 5000;   // REQ-LIGHT (70%)
            } else if (i % 10 < 9) {
                length = 20000;  // REQ-MEDIUM (20%)
            } else {
                length = 50000;  // REQ-HEAVY (10%)
            }

            Cloudlet cloudlet = new CloudletSimple(length, UAECloudConfig.VM_PES)
                    .setFileSize(300)
                    .setOutputSize(300);
            list.add(cloudlet);
        }
        return list;
    }
}