package it.frunzioluigi.metricsCalculator;

import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKReport;
import com.github.mauricioaniche.ck.FileUtils;
import com.github.mauricioaniche.ck.MetricsExecutor;
import com.github.mauricioaniche.ck.metric.CBO;
import com.github.mauricioaniche.ck.metric.LCOM;
import com.github.mauricioaniche.ck.metric.Metric;
import com.github.mauricioaniche.ck.metric.WMC;
import com.google.common.collect.Lists;
import it.frunzioluigi.metricsCalculator.metrics.C3;
import it.frunzioluigi.metricsCalculator.metrics.CCBC;
import it.frunzioluigi.metricsCalculator.metrics.CD;
import it.frunzioluigi.metricsCalculator.metrics.CR;
import org.apache.log4j.Logger;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by giggiux on 3/8/17.
 */
public class ReducedCK extends CK {

	private static final int MAX_AT_ONCE;

	private static Logger log = Logger.getLogger(CK.class);

	static {
		String jdtMax = System.getProperty("jdt.max");
		if (jdtMax != null) {
			MAX_AT_ONCE = Integer.parseInt(jdtMax);
		} else {
			long maxMemory = Runtime.getRuntime().maxMemory() / (1 << 20); // in MiB

			if (maxMemory >= 2000) MAX_AT_ONCE = 400;
			else if (maxMemory >= 1500) MAX_AT_ONCE = 300;
			else if (maxMemory >= 1000) MAX_AT_ONCE = 200;
			else if (maxMemory >= 500) MAX_AT_ONCE = 100;
			else MAX_AT_ONCE = 25;
		}
	}

	public ReducedCK() {
	}

	@Override
	public CKReport calculate(String path) {
		File filePath = new File(path);
		String[] srcDirs = filePath.isDirectory() ? FileUtils.getAllDirs(path) : FileUtils.getAllDirs(filePath.getParent());
		String[] javaFiles = filePath.isFile() ? new String[] {path} : FileUtils.getAllJavaFiles(path);
		log.info("Found " + javaFiles.length + " java files");

		MetricsExecutor storage = new MetricsExecutor(() -> defaultMetrics());

		List<List<String>> partitions = Lists.partition(Arrays.asList(javaFiles), MAX_AT_ONCE);
		log.info("Max partition size: " + MAX_AT_ONCE + ", total partitions=" + partitions.size());


		for (List<String> partition : partitions) {
			log.info("Next partition");
			ASTParser parser = ASTParser.newParser(AST.JLS8);

			parser.setResolveBindings(true);
			parser.setBindingsRecovery(true);

			Map<?, ?> options = JavaCore.getOptions();
			JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
			parser.setCompilerOptions(options);
			parser.setEnvironment(null, srcDirs, null, true);
			parser.createASTs(partition.toArray(new String[partition.size()]), null, new String[0], storage, null);
		}

		log.info("Finished parsing");
		CKReport report = storage.getReport();

		new CCBC().calculate(report);

		return report;

	}

	private List<Metric> defaultMetrics() {
		return new ArrayList<>(Arrays.asList(new CR(), new CBO(), new LCOM(), new WMC(), new C3(), new CD()));
	}

}
