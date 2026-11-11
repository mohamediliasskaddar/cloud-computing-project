package ma.uae.scaling;

import ma.uae.config.UAECloudConfig;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;

import java.util.ArrayList;
import java.util.List;

public class AutoScaler {

    public static List<Vm> scaleUp(DatacenterBroker broker, int extraVmCount) {
        List<Vm> extraVms = new ArrayList<>();
        for (int i = 0; i < extraVmCount; i++) {
            Vm vm = new VmSimple(UAECloudConfig.VM_MIPS, UAECloudConfig.VM_PES)
                    .setRam(UAECloudConfig.VM_RAM)
                    .setBw(UAECloudConfig.VM_BW)
                    .setSize(UAECloudConfig.VM_SIZE)
                    .setCloudletScheduler(new CloudletSchedulerTimeShared());
            extraVms.add(vm);
        }
        broker.submitVmList(extraVms);
        return extraVms;
    }
}