package ma.uae;

import ma.uae.config.UAECloudConfig;
import ma.uae.model.CloudletFactory;
import ma.uae.scaling.AutoScaler;
import ma.uae.util.BenchmarkExporter;
import org.cloudbus.cloudsim.allocationpolicies.VmAllocationPolicyBestFit;
import org.cloudbus.cloudsim.brokers.DatacenterBroker;
import org.cloudbus.cloudsim.brokers.DatacenterBrokerSimple;
import org.cloudbus.cloudsim.cloudlets.Cloudlet;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.datacenters.Datacenter;
import org.cloudbus.cloudsim.datacenters.DatacenterSimple;
import org.cloudbus.cloudsim.hosts.Host;
import org.cloudbus.cloudsim.hosts.HostSimple;
import org.cloudbus.cloudsim.resources.Pe;
import org.cloudbus.cloudsim.resources.PeSimple;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.schedulers.vm.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;

import java.util.ArrayList;
import java.util.List;

public class UAECloudSimulation {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println(" SIMULATION CLOUD SIM PLUS - UNIVERSITÉ ABDELMALEK ESSAÂDI (UAE)");
        System.out.println("========================================================================");

        runSimulation("Période Normale (TimeShared)", 
                UAECloudConfig.INITIAL_VMS, UAECloudConfig.NORMAL_CLOUDLETS, 
                new CloudletSchedulerTimeShared(), false);

        runSimulation("Période de Pointe (SpaceShared - Sans Scaling)", 
                UAECloudConfig.INITIAL_VMS, UAECloudConfig.PEAK_CLOUDLETS, 
                new CloudletSchedulerSpaceShared(), false);

        runSimulation("Période de Pointe (TimeShared + Auto-Scaling)", 
                UAECloudConfig.INITIAL_VMS, UAECloudConfig.PEAK_CLOUDLETS, 
                new CloudletSchedulerTimeShared(), true);
    }

    private static void runSimulation(String title, int initialVms, int cloudletCount, 
                                      CloudletScheduler scheduler, boolean enableAutoScaling) {
        System.out.println("\n------------------------------------------------------------------------");
        System.out.println(" SCÉNARIO : " + title);
        System.out.println("------------------------------------------------------------------------");

        CloudSim simulation = new CloudSim();

        createDatacenter(simulation, UAECloudConfig.DC1_HOSTS);
        createDatacenter(simulation, UAECloudConfig.DC2_HOSTS);

        DatacenterBroker broker = new DatacenterBrokerSimple(simulation);

        List<Vm> vmList = new ArrayList<>();
        for (int i = 0; i < initialVms; i++) {
            Vm vm = new VmSimple(UAECloudConfig.VM_MIPS, UAECloudConfig.VM_PES)
                    .setRam(UAECloudConfig.VM_RAM)
                    .setBw(UAECloudConfig.VM_BW)
                    .setSize(UAECloudConfig.VM_SIZE)
                    .setCloudletScheduler(scheduler);
            vmList.add(vm);
        }
        broker.submitVmList(vmList);

        int totalVms = initialVms;
        if (enableAutoScaling) {
            AutoScaler.scaleUp(broker, 10);
            totalVms += 10;
        }

        List<Cloudlet> cloudletList = CloudletFactory.createCloudlets(cloudletCount);
        broker.submitCloudletList(cloudletList);

        simulation.start();

        List<Cloudlet> finishedCloudlets = broker.getCloudletFinishedList();
        double totalExecTime = finishedCloudlets.stream().mapToDouble(Cloudlet::getActualCpuTime).sum();
        double avgCpuTime = totalExecTime / finishedCloudlets.size();

        System.out.printf(">> Temps moyen d'exécution CPU : %.2f secondes\n", avgCpuTime);

        // Sauvegarde automatique des métriques dans le fichier CSV
        // BenchmarkExporter.exportToCsv(title, finishedCloudlets, totalVms, avgCpuTime);
        BenchmarkExporter.exportToCsv(title, finishedCloudlets, totalVms);
    }

    private static Datacenter createDatacenter(CloudSim simulation, int hostCount) {
        List<Host> hostList = new ArrayList<>();
        for (int i = 0; i < hostCount; i++) {
            List<Pe> peList = new ArrayList<>();
            for (int j = 0; j < UAECloudConfig.HOST_PES; j++) {
                peList.add(new PeSimple(UAECloudConfig.HOST_MIPS));
            }
            Host host = new HostSimple(UAECloudConfig.HOST_RAM, UAECloudConfig.HOST_BW, 
                                       UAECloudConfig.HOST_STORAGE, peList)
                    .setVmScheduler(new VmSchedulerTimeShared());
            hostList.add(host);
        }
        return new DatacenterSimple(simulation, hostList, new VmAllocationPolicyBestFit());
    }
}