```bash
cd part1-cloudsim
mvn clean package
mvn exec:java -Dexec.mainClass=ma.uae.UAECloudSimulation
```

La commande régénère `benchmark_results.csv` dans `part1-cloudsim/`. Les paramètres sont décrits dans [`guide.md`](./guide.md).