package ma.uae.scaling;

import ma.uae.config.UAECloudConfig;
import org.cloudbus.cloudsim.schedulers.cloudlet.CloudletScheduler;
import org.cloudbus.cloudsim.vms.Vm;
import org.cloudbus.cloudsim.vms.VmSimple;
import org.cloudsimplus.autoscaling.HorizontalVmScalingSimple;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class AutoScaler {
    private AutoScaler() {}

    public static Vm createVm(
            String datacenterName,
            Supplier<CloudletScheduler> schedulerSupplier,
            AtomicInteger totalVmCount,
            boolean enableAutoscaling) {
        Vm vm = new VmSimple(UAECloudConfig.VM_MIPS_PER_PE, UAECloudConfig.VM_PES)
                .setRam(UAECloudConfig.VM_RAM_MIB)
                .setBw(UAECloudConfig.VM_BW_MBPS)
                .setSize(UAECloudConfig.VM_STORAGE_MB)
                .setDescription(datacenterName)
                .setCloudletScheduler(schedulerSupplier.get());

        if (enableAutoscaling) {
            attachHorizontalScaling(vm, datacenterName, schedulerSupplier, totalVmCount);
        }
        return vm;
    }

    private static void attachHorizontalScaling(
            Vm vm,
            String datacenterName,
            Supplier<CloudletScheduler> schedulerSupplier,
            AtomicInteger totalVmCount) {
        HorizontalVmScalingSimple scaling = new HorizontalVmScalingSimple();
        scaling.setOverloadPredicate(candidate ->
                candidate.getCpuPercentUtilization() >= UAECloudConfig.AUTOSCALING_CPU_THRESHOLD
                        && totalVmCount.get() < UAECloudConfig.MAX_VMS);
        scaling.setVmSupplier(() -> {
            int count = totalVmCount.incrementAndGet();
            if (count > UAECloudConfig.MAX_VMS) {
                totalVmCount.decrementAndGet();
                throw new IllegalStateException("Autoscaling exceeded the configured VM limit");
            }
            System.out.printf(
                    "AUTOSCALE time=%.2fs dc=%s vm_count=%d cpu=%.1f%%%n",
                    vm.getSimulation().clock(),
                    datacenterName,
                    count,
                    vm.getCpuPercentUtilization() * 100);
            return createVm(datacenterName, schedulerSupplier, totalVmCount, true);
        });
        vm.setHorizontalScaling(scaling);
    }
}
