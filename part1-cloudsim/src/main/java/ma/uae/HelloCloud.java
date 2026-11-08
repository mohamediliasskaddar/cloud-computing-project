package ma.uae;

import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.builders.tables.CloudletsTableBuilder;
import org.cloudsimplus.cloudlets.CloudletSimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.DatacenterSimple;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.utilizationmodels.UtilizationModelFull;
import org.cloudsimplus.vms.VmSimple;

import java.util.ArrayList;
import java.util.List;

public class HelloCloud {
    public static void main(String[] args) {
        var simulation = new CloudSimPlus();

        List<Pe> pes = new ArrayList<>();
        for (int i = 0; i < 4; i++) pes.add(new PeSimple(1000));
        var host = new HostSimple(8192, 10_000, 100_000, pes);
        new DatacenterSimple(simulation, List.of(host));

        var broker = new DatacenterBrokerSimple(simulation);
        var vm = new VmSimple(1000, 2);
        vm.setRam(1024).setBw(1000).setSize(10_000);
        broker.submitVm(vm);

        var cloudlets = new ArrayList<CloudletSimple>();
        for (int i = 0; i < 3; i++) {
            cloudlets.add(new CloudletSimple(10_000, 1, new UtilizationModelFull()));
        }
        broker.submitCloudletList(cloudlets);

        simulation.start();
        new CloudletsTableBuilder(broker.getCloudletFinishedList()).build();
    }
}
