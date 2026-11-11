package ma.uae.util;

import org.cloudbus.cloudsim.cloudlets.Cloudlet;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BenchmarkExporter {

    private static final String CSV_FILE = "benchmark_results.csv";

    public static void exportToCsv(String scenarioName, List<Cloudlet> cloudlets, int vmCount) {
        boolean fileExists = new java.io.File(CSV_FILE).exists();

        try (FileWriter fw = new FileWriter(CSV_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {

            if (!fileExists) {
                pw.println("Timestamp,Scenario,CloudletCount,VmCount,AvgCpuTime,AvgWaitTime,AvgFinishTime,MakeSpan");
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            // Temps moyen CPU pur
            double avgCpuTime = cloudlets.stream().mapToDouble(Cloudlet::getActualCpuTime).average().orElse(0);
            
            // Temps moyen d'attente dans la file
            double avgWaitTime = cloudlets.stream().mapToDouble(Cloudlet::getWaitingTime).average().orElse(0);
            
            // Temps moyen total ressenti par l'utilisateur (Attente + Exec)
            double avgFinishTime = cloudlets.stream().mapToDouble(Cloudlet::getFinishTime).average().orElse(0);

            // Temps total pour terminer tout le lot (Makespan)
            double maxFinishTime = cloudlets.stream().mapToDouble(Cloudlet::getFinishTime).max().orElse(0);

            pw.printf("%s,\"%s\",%d,%d,%.2f,%.2f,%.2f,%.2f\n",
                    timestamp, scenarioName, cloudlets.size(), vmCount, 
                    avgCpuTime, avgWaitTime, avgFinishTime, maxFinishTime);

            System.out.println(" Metric exportées dans " + CSV_FILE);

        } catch (IOException e) {
            System.err.println("Erreur d'écriture CSV : " + e.getMessage());
        }
    }
}