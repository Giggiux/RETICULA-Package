import com.github.mauricioaniche.ck.CKNumber;
import com.github.mauricioaniche.ck.CKReport;
import it.frunzioluigi.metricsCalculator.ReducedCK;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;

/**
 * Created by giggiux on 3/8/17.
 */


public class Main {
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Parameters: <Folder path> [? File Name]");
			System.exit(22);
		}

		String path = args[0];

		String file = null;
		if (args.length > 1) file = args[1];

		ReducedCK myCk = new ReducedCK();

		Instant startTime = Instant.now();
		CKReport report = myCk.calculate(path);
		Instant stopTime = Instant.now();

		Collection<CKNumber> allReport = report.all();

		double tot_c3 = 0, tot_cbo = 0, tot_lcom = 0, tot_ccbc = 0, tot_cr = 0, tot_loc = 0, tot_wmc = 0, tot_cd = 0;

		String fileComputation = "Metrics for files with the given name: \n";

		for (CKNumber result : allReport) {


			double c3 = ((double) result.getSpecific("C3")) / 1000;
			double cbo = result.getCbo();
			double lcom = result.getLcom();
			double ccbc = ((double) result.getSpecific("CCBC")) / 1000;
			double cr = ((double) result.getSpecific("CR")) / 1000;
			double loc = result.getLoc();
			double wmc = result.getWmc();
			double cd = ((double) result.getSpecific("CD")) / 1000;

			tot_c3 += c3;
			tot_cbo += cbo;
			tot_lcom += lcom;
			tot_ccbc += ccbc;
			tot_cd += cd;
			tot_cr += cr;
			tot_loc += loc;
			tot_wmc += wmc;

			if (file != null) {
				String actualFile = result.getFile();
				Path actualFilePath = Paths.get(actualFile);

				if (actualFilePath.getFileName().toString().equals(file)) {
					fileComputation += String.format("\t %s metrics: c3=%f cbo=%f lcom=%f ccbc=%f cr=%f loc=%f wmc=%f cd=%f\n", actualFile, c3, cbo, lcom, ccbc, cr, loc, wmc, cd);
				}

			}

		}

		int size = allReport.size();
		tot_c3 = tot_c3 / size;
		tot_cbo = tot_cbo / size;
		tot_lcom = tot_lcom / size;
		tot_ccbc = tot_ccbc / size;
		tot_cr = tot_cr / size;
		tot_loc = tot_loc / size;
		tot_wmc = tot_wmc / size;
		tot_cd = tot_cd / size;


		System.out.printf("project metrics: c3=%f cbo=%f lcom=%f ccbc=%f cr=%f loc=%f wmc=%f cd=%f\n", tot_c3, tot_cbo, tot_lcom, tot_ccbc, tot_cr, tot_loc, tot_wmc, tot_cd);

		System.out.println(fileComputation);

		System.out.printf("Analyzed %d classes in %s ns \n", report.all().size(), Duration.between(startTime, stopTime).toNanos());

	}
}
