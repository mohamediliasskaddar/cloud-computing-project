package ma.uae;

import ma.uae.config.UAECloudConfig;
import ma.uae.model.CloudletFactory;
import ma.uae.model.CloudletFactory.SiteWorkloads;
import ma.uae.model.CloudletFactory.WorkloadProfile;
import ma.uae.scaling.AutoScaler;
import ma.uae.util.BenchmarkExporter;
import ma.uae.util.BenchmarkResult;
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
import org.cloudbus.cloudsim.utilizationmodels.UtilizationModelFull;
import org.cloudbus.cloudsim.vms.Vm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class UAECloudSimulation {
    private static final List<Scenario> SCENARIOS = List.of(
            new Scenario("Normal_TimeShared", "NORMAL", WorkloadProfile.NORMAL,
                    CloudletSchedulerTimeShared::new, false),
            new Scenario("Peak_SpaceShared_NoScaling", "PEAK", WorkloadProfile.PEAK,
                    CloudletSchedulerSpaceShared::new, false),
            new Scenario("Peak_TimeShared_NoScaling", "PEAK", WorkloadProfile.PEAK,
                    CloudletSchedulerTimeShared::new, false),
            new Scenario("Peak_TimeShared_HorizontalScaling", "PEAK", WorkloadProfile.PEAK,
                    CloudletSchedulerTimeShared::new, true));

    private UAECloudSimulation() {}

    public static void main(String[] args) throws IOException {
        System.out.println("CloudSim Plus — simulation de l'architecture UAE");
        System.out.println("Les quatre scénarios utilisent les mêmes profils et paramètres déclarés.");
        List<BenchmarkResult> results = new ArrayList<>();
        for (Scenario scenario : SCENARIOS) {
            results.add(runScenario(scenario));
        }
        BenchmarkExporter.exportToCsv(results);
        printSummary(results);
    }

    private static BenchmarkResult runScenario(Scenario scenario) {
        System.out.printf("%n=== %s ===%n", scenario.name());
        CloudSim simulation = new CloudSim();
        List<Host> allHosts = new ArrayList<>();
        Datacenter westDc = createDatacenter(
                simulation, UAECloudConfig.WEST_DATACENTER, UAECloudConfig.WEST_HOSTS, allHosts);
        Datacenter eastDc = createDatacenter(
                simulation, UAECloudConfig.EAST_DATACENTER, UAECloudConfig.EAST_HOSTS, allHosts);
        List<Double> hostUtilizationSamples = new ArrayList<>();
        simulation.addOnClockTickListener(event -> hostUtilizationSamples.add(
                allHosts.stream().mapToDouble(Host::getCpuPercentUtilization).average().orElse(0)));

        AtomicInteger totalVmCount = new AtomicInteger(UAECloudConfig.INITIAL_VMS);
        int westInitialVms = (int) Math.round(
                UAECloudConfig.INITIAL_VMS * UAECloudConfig.WEST_LOAD_SHARE);
        int eastInitialVms = UAECloudConfig.INITIAL_VMS - westInitialVms;

        DatacenterBroker westBroker = createBroker(
                simulation, westDc, "Broker-" + UAECloudConfig.WEST_DATACENTER);
        DatacenterBroker eastBroker = createBroker(
                simulation, eastDc, "Broker-" + UAECloudConfig.EAST_DATACENTER);
        Map<Cloudlet, Double> cloudletArrivalTimes = new IdentityHashMap<>();
        submitVms(westBroker, UAECloudConfig.WEST_DATACENTER, westInitialVms,
                scenario, totalVmCount);
        submitVms(eastBroker, UAECloudConfig.EAST_DATACENTER, eastInitialVms,
                scenario, totalVmCount);

        int cloudletCount = scenario.period().equals("NORMAL")
                ? UAECloudConfig.NORMAL_CLOUDLETS
                : UAECloudConfig.PEAK_CLOUDLETS;
        SiteWorkloads workloads = CloudletFactory.createSiteWorkloads(
                cloudletCount, scenario.profile());
        submitCloudletBatches(simulation, westBroker, workloads.west(), cloudletArrivalTimes);
        submitCloudletBatches(simulation, eastBroker, workloads.east(), cloudletArrivalTimes);

        simulation.start();

        List<Cloudlet> finishedCloudlets = new ArrayList<>();
        finishedCloudlets.addAll(westBroker.getCloudletFinishedList());
        finishedCloudlets.addAll(eastBroker.getCloudletFinishedList());
        int finishedCount = finishedCloudlets.size();
        double averageCpuTime = average(finishedCloudlets, Cloudlet::getActualCpuTime);
        double averageQueueWait = average(finishedCloudlets, Cloudlet::getWaitingTime);
        double averageResponseTime = finishedCloudlets.stream()
                .mapToDouble(cloudlet -> cloudlet.getFinishTime()
                        - cloudletArrivalTimes.getOrDefault(
                                cloudlet, cloudlet.getSubmissionDelay()))
                .average()
                .orElse(0);
        double makespan = finishedCloudlets.stream()
                .mapToDouble(Cloudlet::getFinishTime)
                .max()
                .orElse(0);
        double hostUtilization = hostUtilizationSamples.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0) * 100;
        int createdVms = westBroker.getVmCreatedList().size() + eastBroker.getVmCreatedList().size();
        double estimatedCost = estimateCostUsd(
                westBroker.getVmCreatedList(), eastBroker.getVmCreatedList(), simulation.clock());
        int lightCloudlets = (int) finishedCloudlets.stream()
                .filter(cloudlet -> cloudlet.getLength() == 5_000)
                .count();
        int mediumCloudlets = (int) finishedCloudlets.stream()
                .filter(cloudlet -> cloudlet.getLength() == 20_000)
                .count();
        int heavyCloudlets = (int) finishedCloudlets.stream()
                .filter(cloudlet -> cloudlet.getLength() == 50_000)
                .count();

        if (finishedCount != cloudletCount) {
            throw new IllegalStateException(
                    scenario.name() + " completed " + finishedCount + " of "
                            + cloudletCount + " Cloudlets");
        }

        BenchmarkResult result = new BenchmarkResult(
                scenario.name(),
                scenario.period(),
                schedulerName(scenario.schedulerSupplier()),
                scenario.autoscaling(),
                cloudletCount,
                finishedCount,
                lightCloudlets,
                mediumCloudlets,
                heavyCloudlets,
                workloads.west().size(),
                workloads.east().size(),
                UAECloudConfig.INITIAL_VMS,
                createdVms,
                averageCpuTime,
                averageQueueWait,
                averageResponseTime,
                makespan,
                hostUtilization,
                estimatedCost);

        System.out.printf(
                "Cloudlets terminées: %d/%d | VM: %d/%d | CPU moyen: %.2f s | "
                        + "attente moyenne: %.2f s | réponse moyenne: %.2f s | "
                        + "makespan: %.2f s | utilisation moyenne hôtes: %.2f%% | coût estimé: $%.6f%n",
                finishedCount, cloudletCount, createdVms, UAECloudConfig.INITIAL_VMS,
                averageCpuTime, averageQueueWait, averageResponseTime, makespan,
                hostUtilization, estimatedCost);
        return result;
    }

    private static DatacenterBroker createBroker(
            CloudSim simulation, Datacenter target, String brokerName) {
        DatacenterBroker broker = new DatacenterBrokerSimple(simulation, brokerName);
        broker.setDatacenterMapper((candidate, vm) -> target);
        return broker;
    }

    private static void submitVms(
            DatacenterBroker broker,
            String datacenterName,
            int count,
            Scenario scenario,
            AtomicInteger totalVmCount) {
        List<Vm> vms = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            vms.add(AutoScaler.createVm(
                    datacenterName,
                    scenario.schedulerSupplier(),
                    totalVmCount,
                    scenario.autoscaling()));
        }
        broker.submitVmList(vms);
    }

    private static void submitCloudletBatches(
            CloudSim simulation,
            DatacenterBroker broker,
            List<Cloudlet> cloudlets,
            Map<Cloudlet, Double> cloudletArrivalTimes) {
        List<List<Cloudlet>> batches = CloudletFactory.splitIntoBatches(
                cloudlets, UAECloudConfig.ARRIVAL_BATCH_COUNT);
        if (batches.isEmpty()) {
            return;
        }
        submitBatch(simulation, broker, batches.get(0), cloudletArrivalTimes);
        int[] nextBatch = {1};
        simulation.addOnClockTickListener(event -> {
            while (nextBatch[0] < batches.size()
                    && event.getTime() >= nextBatch[0]
                    * UAECloudConfig.ARRIVAL_BATCH_INTERVAL_SECONDS) {
                submitBatch(simulation, broker, batches.get(nextBatch[0]), cloudletArrivalTimes);
                nextBatch[0]++;
            }
        });
    }

    private static void submitBatch(
            CloudSim simulation,
            DatacenterBroker broker,
            List<Cloudlet> cloudlets,
            Map<Cloudlet, Double> cloudletArrivalTimes) {
        cloudlets.forEach(cloudlet ->
                cloudlet.setUtilizationModelCpu(new UtilizationModelFull()));
        cloudlets.forEach(cloudlet -> cloudletArrivalTimes.put(cloudlet, simulation.clock()));
        broker.submitCloudletList(cloudlets);
    }

    private static Datacenter createDatacenter(
            CloudSim simulation, String name, int hostCount, List<Host> allHosts) {
        List<Host> hosts = new ArrayList<>(hostCount);
        for (int i = 0; i < hostCount; i++) {
            List<Pe> processingElements = new ArrayList<>(UAECloudConfig.HOST_PES);
            for (int j = 0; j < UAECloudConfig.HOST_PES; j++) {
                processingElements.add(new PeSimple(UAECloudConfig.HOST_MIPS_PER_PE));
            }
            Host host = new HostSimple(
                    UAECloudConfig.HOST_RAM_MIB,
                    UAECloudConfig.HOST_BW_MBPS,
                    UAECloudConfig.HOST_STORAGE_MB,
                    processingElements)
                    .setVmScheduler(new VmSchedulerTimeShared());
            hosts.add(host);
        }
        allHosts.addAll(hosts);
        DatacenterSimple datacenter =
                new DatacenterSimple(simulation, hosts, new VmAllocationPolicyBestFit());
        datacenter.setName(name);
        datacenter.setSchedulingInterval(1);
        return datacenter;
    }

    private static double estimateCostUsd(List<Vm> westVms, List<Vm> eastVms, double endTime) {
        List<Vm> allVms = new ArrayList<>(westVms);
        allVms.addAll(eastVms);
        return allVms.stream().mapToDouble(vm -> {
            double lifetimeHours = Math.max(0, endTime - vm.getStartTime()) / 3_600.0;
            double vcpuCost = vm.getNumberOfPes()
                    * UAECloudConfig.COST_PER_VCPU_HOUR_USD * lifetimeHours;
            double ramGib = vm.getRam().getCapacity() / 1_024.0;
            double ramCost = ramGib
                    * UAECloudConfig.COST_PER_GIB_RAM_HOUR_USD * lifetimeHours;
            double storageGib = vm.getStorage().getCapacity() / 1_024.0;
            double storageCost = storageGib
                    * UAECloudConfig.COST_PER_GIB_STORAGE_HOUR_USD * lifetimeHours;
            return vcpuCost + ramCost + storageCost;
        }).sum();
    }

    private static double average(
            List<Cloudlet> cloudlets, java.util.function.ToDoubleFunction<Cloudlet> metric) {
        return cloudlets.stream().mapToDouble(metric).average().orElse(0);
    }

    private static String schedulerName(Supplier<CloudletScheduler> supplier) {
        return supplier.get().getClass().getSimpleName();
    }

    private static void printSummary(List<BenchmarkResult> results) {
        System.out.println("\nRésumé de comparaison (voir benchmark_results.csv pour le détail) :");
        for (BenchmarkResult result : results) {
            System.out.printf("%-38s scheduler=%-29s VMs=%2d makespan=%8.2fs "
                            + "response=%8.2fs hostCPU=%6.2f%% cost=$%.6f%n",
                    result.scenario(), result.scheduler(), result.createdVms(),
                    result.makespanSeconds(), result.averageResponseTimeSeconds(),
                    result.averageHostCpuUtilizationPercent(), result.estimatedCostUsd());
        }
    }

    private record Scenario(
            String name,
            String period,
            WorkloadProfile profile,
            Supplier<CloudletScheduler> schedulerSupplier,
            boolean autoscaling) {}
}
